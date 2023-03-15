package org.dpppt.android.calibration;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import org.dpppt.android.calibration.util.NotificationUtil;
import org.dpppt.android.sdk.Messaging;
import org.dpppt.android.sdk.internal.logger.LogLevel;
import org.dpppt.android.sdk.internal.logger.Logger;
import org.dpppt.android.sdk.internal.util.ProcessUtil;

public class MainApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		if (ProcessUtil.isMainProcess(this)) {
			initDP3T(this);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationUtil.createNotificationChannel(this);
		}
		Logger.init(getApplicationContext(), LogLevel.DEBUG);
	}

	public static void initDP3T(Context context) {
		Messaging.init(context);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}
}