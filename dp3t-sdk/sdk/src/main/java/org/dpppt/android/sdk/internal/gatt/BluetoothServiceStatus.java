package org.dpppt.android.sdk.internal.gatt;

import android.annotation.SuppressLint;
import android.content.Context;
import org.dpppt.android.sdk.internal.BroadcastHelper;

public final class BluetoothServiceStatus {
	public static final int SCAN_OK = 0;
	public static final int ADVERTISE_OK = 0;

	@SuppressLint("StaticFieldLeak")
	private static BluetoothServiceStatus instance;

	private Context context;

	private int scanStatus = SCAN_OK;
	private int advertiseStatus = ADVERTISE_OK;

	private BluetoothServiceStatus(Context context) {
		this.context = context;
	}

	public static synchronized BluetoothServiceStatus getInstance(Context context) {
		if (instance == null) {
			instance = new BluetoothServiceStatus(context.getApplicationContext());
		}
		return instance;
	}

	public static synchronized void resetInstance() {
		instance = null;
	}

	void updateScanStatus(int scanStatus) {
		if (this.scanStatus != scanStatus) {
			this.scanStatus = scanStatus;
			BroadcastHelper.sendErrorUpdateBroadcast(context);
		}
	}

	public int getScanStatus() {
		return scanStatus;
	}

	void updateAdvertiseStatus(int advertiseStatus) {
		if (this.advertiseStatus != advertiseStatus) {
			this.advertiseStatus = advertiseStatus;
			BroadcastHelper.sendErrorUpdateBroadcast(context);
		}
	}

	public int getAdvertiseStatus() {
		return advertiseStatus;
	}

}
