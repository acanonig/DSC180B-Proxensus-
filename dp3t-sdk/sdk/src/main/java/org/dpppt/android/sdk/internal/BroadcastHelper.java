package org.dpppt.android.sdk.internal;

import android.content.Context;
import android.content.Intent;
import org.dpppt.android.sdk.Messaging;

public class BroadcastHelper {

	public static final String ACTION_UPDATE_ERRORS = "org.dpppt.android.sdk.internal.ACTION_UPDATE_ERRORS";

	public static void sendUpdateBroadcast(Context context) {
		Intent intent = new Intent(Messaging.UPDATE_INTENT_ACTION);
		context.sendBroadcast(intent);
	}

	public static void sendErrorUpdateBroadcast(Context context) {
		sendUpdateBroadcast(context);

		Intent intent = new Intent(ACTION_UPDATE_ERRORS);
		context.sendBroadcast(intent);
	}

}
