package org.dpppt.android.sdk.internal.gatt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.dpppt.android.sdk.internal.AppConfigManager;
import org.dpppt.android.sdk.internal.BroadcastHelper;
import org.dpppt.android.sdk.internal.logger.Logger;

import org.json.JSONObject;

import static org.dpppt.android.sdk.internal.gatt.BleServer.SERVICE_UUID;

public class BleClient {

	private static final String TAG = "BleClient";

	private final Context context;
	private BluetoothLeScanner bleScanner;
	private ScanCallback bleScanCallback;

	private static ArrayList<String> diff_location_ephid = new ArrayList<>();

	// constructor
	public BleClient(Context context) {this.context = context;}

	public BluetoothState start() {
		final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// check for bluetooth
		if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
			BroadcastHelper.sendErrorUpdateBroadcast(context);
			return bluetoothAdapter == null ? BluetoothState.NOT_SUPPORTED : BluetoothState.DISABLED;
		}
		// Bluetooth low energy
		bleScanner = bluetoothAdapter.getBluetoothLeScanner();
		if (bleScanner == null) {
			return BluetoothState.NOT_SUPPORTED;
		}

		// uuid scanfilter
		List<ScanFilter> scanFilters = new ArrayList<>();
		scanFilters.add(new ScanFilter.Builder()
				.setServiceUuid(new ParcelUuid(SERVICE_UUID))
				.build());

		// Settings for bluetooth scan
		ScanSettings.Builder settingsBuilder = new ScanSettings.Builder()
				.setScanMode(0)
				.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
				.setReportDelay(0)
				.setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
				.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			settingsBuilder
					.setPhy(ScanSettings.PHY_LE_ALL_SUPPORTED)
					.setLegacy(true);
		}

		ScanSettings scanSettings = settingsBuilder.build();
		BluetoothServiceStatus bluetoothServiceStatus = BluetoothServiceStatus.getInstance(context);

		// Callback function will retrieve data
		bleScanCallback = new ScanCallback() {
			private static final String TAG = "ScanCallback";

			public void onScanResult(int callbackType, ScanResult result) {
				bluetoothServiceStatus.updateScanStatus(BluetoothServiceStatus.SCAN_OK);
				if (result.getScanRecord() != null) {
					onDeviceFound(result);
				}
			}

			@Override
			public void onBatchScanResults(List<ScanResult> results) {
				bluetoothServiceStatus.updateScanStatus(BluetoothServiceStatus.SCAN_OK);
				Logger.d(TAG, "Batch size " + results.size());
				for (ScanResult result : results) {
					onScanResult(0, result);
				}
			}

			public void onScanFailed(int errorCode) {
				bluetoothServiceStatus.updateScanStatus(errorCode);
				Logger.e(TAG, "error: " + errorCode);
			}
		};

		bleScanner.startScan(scanFilters, scanSettings, bleScanCallback);
		Logger.i(TAG, "started BLE scanner, scanMode: " + scanSettings.getScanMode() + " scanFilters: " + scanFilters.size());

		return BluetoothState.ENABLED;
	}

	private void onDeviceFound(ScanResult scanResult) {
		try {
			// get the EphId payload
			byte[] payload = scanResult.getScanRecord().getServiceData(new ParcelUuid(SERVICE_UUID));
			boolean correctPayload = payload != null;
			Logger.d(TAG, "found device with correct payload");
			if (correctPayload) {
				// zip is after zip
				byte[] ephID = Arrays.copyOfRange(payload, 5, payload.length);
				// zip is the first 5 bytes
				byte[] zip_byte = Arrays.copyOfRange(payload, 0, 5);
				String zip = new String(zip_byte, "UTF-8");
				String name = new String(ephID, "UTF-8");

				AppConfigManager appConfigManager = AppConfigManager.getInstance(context);
				String zipcoders = appConfigManager.getZip();

				Logger.d(TAG, zip + ": received. list size is " + diff_location_ephid.size());

				if (!zipcoders.equals(zip)) {
					Boolean notInLst = true;
					for (String i : diff_location_ephid) {
						String id = i;
						if (id.equals(name)) {
							notInLst = false;
							break;
						}
					}
					if (notInLst) {
						// 00000 is the default value during initialization
						if (!zip.equals("00000")) {
							diff_location_ephid.add(name);
							Logger.i(TAG, "interaction with device confirmed.");
						}
						else {
							Logger.d(TAG, "Default zip was received, not added to list");
						}
					}
				}
			}

		} catch (Exception e) {
			Logger.e(TAG, e);
		}
	}

	// stop within intervals or manual
	public synchronized void stopScan() {
		final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
			bleScanner = null;
			BroadcastHelper.sendErrorUpdateBroadcast(context);
			return;
		}
		if (bleScanner != null) {
			Logger.i(TAG, "stopping BLE scanner");
			bleScanner.stopScan(bleScanCallback);
			bleScanner = null;
		}
	}

	public synchronized void stop() {
		stopScan();
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				AppConfigManager appConfigManager = AppConfigManager.getInstance(context);
					try {
						// converting data for posting
						JSONObject jObj = new JSONObject();
						jObj.put("from_user", appConfigManager.getID());
						jObj.put("spotted_users", diff_location_ephid.toString());
						String jsonStr = jObj.toString();

						//// Adding interactions to server
						URL url = new URL("https://dsc180-decentralized-location.herokuapp.com/locationConsensus/interactions/");
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod("POST");

						// JSON format data
						connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

						// Set the request body with the JSON data
						byte[] postData = jsonStr.getBytes("UTF-8");
						connection.setDoOutput(true);
						OutputStream outputStream1 = connection.getOutputStream();
						outputStream1.write(postData);outputStream1.flush();outputStream1.close();

						// Response from the server
						BufferedReader reader1 = new BufferedReader(new InputStreamReader(connection.getInputStream()));
						String line1;
						StringBuilder response1 = new StringBuilder();
						while ((line1 = reader1.readLine()) != null) {response1.append(line1);}
						reader1.close();
						Log.d("POST Response interactions", response1.toString());
						diff_location_ephid.clear();
					} catch (Exception e) {
						e.printStackTrace();
					}
			}
		});
		if (diff_location_ephid.size() != 0) {
			thread.start();
		}


	}

}
