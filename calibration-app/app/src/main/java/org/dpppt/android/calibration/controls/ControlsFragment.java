package org.dpppt.android.calibration.controls;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.dpppt.android.calibration.MainApplication;
import org.dpppt.android.calibration.R;
import org.dpppt.android.calibration.util.DialogUtil;
import org.dpppt.android.calibration.util.RequirementsUtil;
import org.dpppt.android.sdk.Messaging;


import org.dpppt.android.sdk.TracingStatus;
import org.dpppt.android.sdk.internal.AppConfigManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ControlsFragment extends Fragment {


	private static final int REQUEST_CODE_PERMISSION_LOCATION = 1;

	private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				checkPermissionRequirements();
				updateSdkStatus();
			}
		}
	};

	private BroadcastReceiver sdkReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateSdkStatus();
		}
	};

	public static ControlsFragment newInstance() {
		return new ControlsFragment();
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_home, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setupUi(view);
	}

	@Override
	public void onResume() {
		super.onResume();
		getContext().registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
		getContext().registerReceiver(sdkReceiver, Messaging.getUpdateIntentFilter());
		checkPermissionRequirements();
		updateSdkStatus();
	}

	@Override
	public void onPause() {
		super.onPause();
		getContext().unregisterReceiver(bluetoothReceiver);
		getContext().unregisterReceiver(sdkReceiver);
	}


	private void setupUi(View view) {
		AppConfigManager appConfigManager = AppConfigManager.getInstance(getContext().getApplicationContext());
		TextView statusText = view.findViewById(R.id.home_status_text);
		statusText.setText(appConfigManager.getName());

		// location permissions
		Button locationButton = view.findViewById(R.id.home_button_location);
		locationButton.setOnClickListener(
				v -> {
					requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
						REQUEST_CODE_PERMISSION_LOCATION);
					appConfigManager.setlocation(true);
				});

		// battery optimization
		Button batteryButton = view.findViewById(R.id.home_button_battery_optimization);
		batteryButton.setOnClickListener(
				v -> startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
						Uri.parse("package:" + getContext().getPackageName()))));

		// Button Activating Bluetooth
		Button bluetoothButton = view.findViewById(R.id.home_button_bluetooth);
		bluetoothButton.setOnClickListener(v -> {
			if (BluetoothAdapter.getDefaultAdapter() != null) {
				BluetoothAdapter.getDefaultAdapter().enable();
			} else {
				Toast.makeText(getContext(), "No BluetoothAdapter found!", Toast.LENGTH_LONG).show();
			}
		});

		EditText editText = view.findViewById(R.id.editText);

		// Updates Button to clear data
		Button buttonClearData = view.findViewById(R.id.home_button_clear_data);
		buttonClearData.setOnClickListener(v -> {
			DialogUtil.showConfirmDialog(v.getContext(), R.string.dialog_clear_data_title,
					(dialog, which) -> {
						Messaging.clearData(v.getContext(), () ->
								new Handler(getContext().getMainLooper()).post(this::updateSdkStatus));
						MainApplication.initDP3T(v.getContext());
						statusText.setText("Default");
					});
		});

		Button buttonSetName = view.findViewById(R.id.set_name);

		buttonSetName.setOnClickListener(v -> {
			InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

			// Set name
			String name = editText.getText().toString();
			appConfigManager.setNameShare(name);
			statusText.setText(name);
			editText.setText("");
			editText.setCursorVisible(false);

		});
		editText.setOnClickListener(v -> {
			editText.setCursorVisible(true);
		});

		Button buttonCheckBlacklist = view.findViewById(R.id.blacklist);
		buttonCheckBlacklist.setOnClickListener(v -> {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						URL url = new URL("https://dsc180-decentralized-location.herokuapp.com/locationConsensus/blacklist/");
						HttpURLConnection conn = (HttpURLConnection) url.openConnection();
						conn.setRequestMethod("GET");
						conn.setRequestProperty("Accept", "application/json");

						BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

						String output;
						appConfigManager.setBlacklist(false);

						while ((output = br.readLine()) != null) {
							System.out.println(output);
							JSONArray jsonArray = new JSONArray(output);
							for (int i = 0; i < jsonArray.length(); i++) {
								JSONObject jsonObject = jsonArray.getJSONObject(i);
								String userID = jsonObject.getString("userID");
								if (userID.equals(appConfigManager.getID())) {
									appConfigManager.setBlacklist(true);
									break;
								}
							}
						}
						conn.disconnect();
						Handler handler = new Handler(Looper.getMainLooper());
						handler.post(new Runnable() {
							@Override
							public void run() {
								System.out.println("bruh " + appConfigManager.getBlacklist());
								if (appConfigManager.getBlacklist()) {
									Toast.makeText(getContext(), "You're in the blacklist!", Toast.LENGTH_LONG).show();
								}
								else {
									Toast.makeText(getContext(), "You're not in the blacklist!", Toast.LENGTH_LONG).show();
								}
							}
						});
					}
					catch (Exception e) {
						e.printStackTrace();
						Toast.makeText(getContext(), "Did not get a response from the server!", Toast.LENGTH_LONG).show();
					}
				}
			});
			thread.start();
		});
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_CODE_PERMISSION_LOCATION) {
			checkPermissionRequirements();
			updateSdkStatus();
		}
	}

	private void checkPermissionRequirements() {
		View view = getView();
		Context context = getContext();
		if (view == null || context == null) return;

		boolean locationGranted = RequirementsUtil.isLocationPermissionGranted(context);
		Button locationButton = view.findViewById(R.id.home_button_location);
		locationButton.setEnabled(!locationGranted);
		locationButton.setText(locationGranted ? R.string.req_location_permission_granted
											   : R.string.req_location_permission_ungranted);

		boolean batteryOptDeactivated = RequirementsUtil.isBatteryOptimizationDeactivated(context);
		Button batteryButton = view.findViewById(R.id.home_button_battery_optimization);
		batteryButton.setEnabled(!batteryOptDeactivated);
		batteryButton.setText(batteryOptDeactivated ? R.string.req_battery_deactivated
													: R.string.req_battery_deactivated);

		boolean bluetoothActivated = RequirementsUtil.isBluetoothEnabled();
		Button bluetoothButton = view.findViewById(R.id.home_button_bluetooth);
		bluetoothButton.setEnabled(!bluetoothActivated);
		bluetoothButton.setText(bluetoothActivated ? R.string.req_bluetooth_active
												   : R.string.req_bluetooth_inactive);


	}

	private void updateSdkStatus() {
		View view = getView();
		Context context = getContext();
		if (context == null || view == null) return;

		TracingStatus status = Messaging.getStatus(context);

		// Button to stop and start tracking (both receive and advertise)
		Button buttonStartStopTracking = view.findViewById(R.id.home_button_start_stop_tracking);
		boolean isRunning = status.isAdvertising() || status.isReceiving();
		buttonStartStopTracking.setSelected(isRunning);
		buttonStartStopTracking.setText(getString(isRunning ? R.string.button_tracking_stop
															: R.string.button_tracking_start));

		AppConfigManager appConfigManager = AppConfigManager.getInstance(getContext().getApplicationContext());
		buttonStartStopTracking.setOnClickListener(v -> {
			if (appConfigManager.getlocation()) {
				if (appConfigManager.getBlacklist()) {
					Toast.makeText(getContext(), "You are prohibited from tracking!", Toast.LENGTH_LONG).show();
				}
				else {
					if (isRunning) {
						Messaging.stop(v.getContext());
					} else {
						Messaging.start(v.getContext());
					}
					updateSdkStatus();
				}

			}
			else {
				Toast.makeText(getContext(), "Please approve location permissions.", Toast.LENGTH_LONG).show();
			}
		});

		// Updates Button to clear data
		Button buttonClearData = view.findViewById(R.id.home_button_clear_data);
		buttonClearData.setEnabled(!isRunning);
	}

}
