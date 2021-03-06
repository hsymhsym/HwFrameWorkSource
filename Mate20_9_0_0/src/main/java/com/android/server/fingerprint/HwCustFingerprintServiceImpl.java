package com.android.server.fingerprint;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.Slog;

public class HwCustFingerprintServiceImpl extends HwCustFingerprintService {
    private static final int DEFAULT_MAX_FAILED_TIMES = 5;
    private static final boolean IS_ATT;
    private static final String TAG = "FingerprintService";

    static {
        boolean z = SystemProperties.get("ro.config.hw_opta", "0").equals("07") && SystemProperties.get("ro.config.hw_optb", "0").equals("840");
        IS_ATT = z;
    }

    public boolean isAtt() {
        return IS_ATT;
    }

    private int getMaxFailedAttemps(Context context) {
        if (context != null) {
            return System.getIntForUser(context.getContentResolver(), "finger_authentication_threshold", DEFAULT_MAX_FAILED_TIMES, -2);
        }
        return DEFAULT_MAX_FAILED_TIMES;
    }

    public int getRemainingNum(int failedAttempts, Context context) {
        int maxFailedAttemps = getMaxFailedAttemps(context);
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" Remaining Num Attempts By user settings= ");
        stringBuilder.append(maxFailedAttemps - failedAttempts);
        Slog.d(str, stringBuilder.toString());
        return maxFailedAttemps - failedAttempts;
    }

    public boolean inLockoutMode(int failedAttempts, Context context) {
        return failedAttempts >= getMaxFailedAttemps(context);
    }

    public boolean isLockoutMode(int failedAttempts, Context context) {
        return failedAttempts % getMaxFailedAttemps(context) == 0;
    }
}
