package com.android.settings.security;

import android.content.Context;
import android.os.UserHandle;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.core.BasePreferenceController;

public class DuressPasswordPreferenceController extends BasePreferenceController {

    private final LockPatternUtils mLockPatternUtils;

    public DuressPasswordPreferenceController(Context context, String key) {
        super(context, key);
        mLockPatternUtils = new LockPatternUtils(context);
    }

    @Override
    public int getAvailabilityStatus() {
        if (mLockPatternUtils.isSecure(UserHandle.myUserId())) {
            return AVAILABLE;
        } else {
            return DISABLED_FOR_USER;
        }
    }
}
