package org.dpppt.android.sdk.internal.crypto;
import android.content.Context;
import org.dpppt.android.sdk.internal.AppConfigManager;
import org.dpppt.android.sdk.internal.logger.Logger;

import java.util.Random;

public class EphemeralIdGenerator {
    public static byte[] generateEphemeralId(Context context) {
        AppConfigManager appConfigManager = AppConfigManager.getInstance(context);
        String id = appConfigManager.getID();
        if (id == null) {
            Random random = new Random();
            int randomInt = random.nextInt(90000000) + 10000000;
            String str = String.valueOf(randomInt);
            Logger.d("ID",str + " : New!");
            appConfigManager.setID(str);
            return str.getBytes();
        }
        else {
            Logger.d("ID",id + " : Stored!");
            return id.getBytes();
        }
    }
}

