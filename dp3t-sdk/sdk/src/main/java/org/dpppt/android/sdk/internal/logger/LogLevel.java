package org.dpppt.android.sdk.internal.logger;

import android.util.Log;

public enum LogLevel {
	DEBUG("d", "debug", 1, Log::d),
	INFO("i", "info", 2, Log::i),
	WARNING("w", "warning", 3, Log::w),
	ERROR("e", "error", 4, Log::e),
	OFF("-", "off", Integer.MAX_VALUE, null);

	private final String key;
	private final String value;
	private final int importance;
	private final LogFunction logcat;

	LogLevel(String key, String value, int importance, LogFunction logcat) {
		this.key = key;
		this.value = value;
		this.importance = importance;
		this.logcat = logcat;
	}

	public static LogLevel byKey(String key) {
		for (LogLevel value : LogLevel.values()) {
			if (value.getKey().equals(key)) {
				return value;
			}
		}
		return null;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	public int getImportance() {
		return importance;
	}

	public LogFunction getLogcat() {
		return logcat;
	}
}
