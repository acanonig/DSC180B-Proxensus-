package org.dpppt.android.sdk;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.core.content.ContextCompat;
import java.util.Collection;
import org.dpppt.android.sdk.internal.AppConfigManager;
import org.dpppt.android.sdk.internal.BroadcastHelper;
import org.dpppt.android.sdk.internal.ErrorHelper;
import org.dpppt.android.sdk.internal.TracingService;
import org.dpppt.android.sdk.internal.logger.Logger;
import org.dpppt.android.sdk.internal.util.ProcessUtil;

public class Messaging {

	private static final String TAG = "Tracing Interface";
	public static final String UPDATE_INTENT_ACTION = "org.dpppt.android.sdk.UPDATE_ACTION";
	public static void init(Context context) {
		if (ProcessUtil.isMainProcess(context)) {
			executeInit(context);
		}
	}
	private static void executeInit(Context context) {
		AppConfigManager appConfigManager = AppConfigManager.getInstance(context);
		boolean advertising = appConfigManager.isAdvertisingEnabled();
		boolean receiving = appConfigManager.isReceivingEnabled();
		if (advertising || receiving) {
			start(context, advertising, receiving);
		}
	}

	public static void start(Context context) {
		start(context, true, true);
	}
	public static void start(Context context, boolean advertise, boolean receive) {
		AppConfigManager appConfigManager = AppConfigManager.getInstance(context);
		appConfigManager.setAdvertisingEnabled(advertise);
		appConfigManager.setReceivingEnabled(receive);
		long scanInterval = appConfigManager.DEFAULT_SCAN_INTERVAL;
		long scanDuration = appConfigManager.DEFAULT_SCAN_DURATION;
		Intent intent = new Intent(context, TracingService.class).setAction(TracingService.ACTION_START);
		intent.putExtra(TracingService.EXTRA_ADVERTISE, advertise);
		intent.putExtra(TracingService.EXTRA_RECEIVE, receive);
		intent.putExtra(TracingService.EXTRA_SCAN_INTERVAL, scanInterval);
		intent.putExtra(TracingService.EXTRA_SCAN_DURATION, scanDuration);
		ContextCompat.startForegroundService(context, intent);
		BroadcastHelper.sendUpdateBroadcast(context);
	}

	public static TracingStatus getStatus(Context context) {

		AppConfigManager appConfigManager = AppConfigManager.getInstance(context);
		Collection<TracingStatus.ErrorState> errorStates = ErrorHelper.checkTracingErrorStatus(context);

		return new TracingStatus(
				appConfigManager.isAdvertisingEnabled(),
				appConfigManager.isReceivingEnabled(),
				errorStates
		);
	}

	public static void stop(Context context) {
		AppConfigManager appConfigManager = AppConfigManager.getInstance(context);
		appConfigManager.setAdvertisingEnabled(false);
		appConfigManager.setReceivingEnabled(false);

		Intent intent = new Intent(context, TracingService.class).setAction(TracingService.ACTION_STOP);
		context.startService(intent);
		BroadcastHelper.sendUpdateBroadcast(context);
	}
	public static IntentFilter getUpdateIntentFilter() {
		return new IntentFilter(Messaging.UPDATE_INTENT_ACTION);
	}
	public static void clearData(Context context, Runnable onDeleteListener) {
		AppConfigManager appConfigManager = AppConfigManager.getInstance(context);
		if (appConfigManager.isAdvertisingEnabled() || appConfigManager.isReceivingEnabled()) {
			throw new IllegalStateException("Tracking must be stopped for clearing the local data");
		}
		appConfigManager.clearPreferences();
		appConfigManager.setlocation(true);
		Logger.clear();
		Logger.i(TAG, "You have deleted all your data");
	}
}
