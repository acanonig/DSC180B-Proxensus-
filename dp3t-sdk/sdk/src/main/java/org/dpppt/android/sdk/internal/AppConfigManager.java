package org.dpppt.android.sdk.internal;

import android.content.Context;
import android.content.SharedPreferences;

public class AppConfigManager {
	private static AppConfigManager instance;

	public static synchronized AppConfigManager getInstance(Context context) {
		if (instance == null) {
			instance = new AppConfigManager(context);
		}
		return instance;
	}
	// 1 minute break between scanning
	public static final long DEFAULT_SCAN_INTERVAL = 1 * 60 * 1000L;
	// 1 hour scan duration, to match server team's blacklist update
	public static final long DEFAULT_SCAN_DURATION = 60 * 60 * 1000L;
	private static final String PREFS_NAME = "dp3t_sdk_preferences";
	private static final String PREF_ADVERTISING_ENABLED = "advertisingEnabled";
	private static final String PREF_RECEIVING_ENABLED = "receivingEnabled";

	private SharedPreferences sharedPrefs;

	private AppConfigManager(Context context) {
		sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}

	public void setAdvertisingEnabled(boolean enabled) {
		sharedPrefs.edit().putBoolean(PREF_ADVERTISING_ENABLED, enabled).apply();
	}

	public boolean isAdvertisingEnabled() {
		return sharedPrefs.getBoolean(PREF_ADVERTISING_ENABLED, false);
	}

	public void setReceivingEnabled(boolean enabled) {
		sharedPrefs.edit().putBoolean(PREF_RECEIVING_ENABLED, enabled).apply();
	}

	public boolean isReceivingEnabled() {
		return sharedPrefs.getBoolean(PREF_RECEIVING_ENABLED, false);
	}

	public void clearPreferences() {
		sharedPrefs.edit().clear().apply();
	}

	public void setZip(String zip) {
		sharedPrefs.edit().putString("Zip code", zip).apply();
	}
	public String getZip() {
		return sharedPrefs.getString("Zip code", "00000");
	}

	public String name = "Default";
	public void setName(String name) {
		this.name = name;
	}

	public void setID(String id) {
		sharedPrefs.edit().putString("Ephemeral ID", id).apply();
	}

	public String getID() {
		return sharedPrefs.getString("Ephemeral ID", null);
	}

	public void setNameShare(String name) {
		sharedPrefs.edit().putString("Name_eph_id", name).apply();
	}

	public String getName() {
		return sharedPrefs.getString("Name_eph_id", null);
	}

    public boolean getBlacklist() {
		return sharedPrefs.getBoolean("blacklist", false);
    }

	public void setBlacklist(boolean b) {
		sharedPrefs.edit().putBoolean("blacklist", b).apply();
	}

	public boolean getlocation() {
		return sharedPrefs.getBoolean("location-permission", false);
	}

	public void setlocation(boolean b) {
		sharedPrefs.edit().putBoolean("location-permission", b).apply();
	}
}
