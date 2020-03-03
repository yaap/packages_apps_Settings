/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.security;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;

import com.android.settings.core.TogglePreferenceController;

public class PanicButtonPreferenceController extends TogglePreferenceController {

    private static final String[] PANIC_PACKAGES =
            new String[]{"info.guardianproject.ripple", "org.calyxos.ripple"};
    private static final String PANIC_ACTIVITY = "org.calyxos.ripple.CountDownActivity";

    public PanicButtonPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        for (String panicPackage : PANIC_PACKAGES) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(panicPackage, PANIC_ACTIVITY));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            if (mContext.getPackageManager().resolveActivity(intent,
                    PackageManager.MATCH_SYSTEM_ONLY) != null) {
                return AVAILABLE;
            }
        }
        return CONDITIONALLY_UNAVAILABLE;
    }

    @Override
    public boolean isChecked() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.PANIC_IN_POWER_MENU, 0) != 0;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        Settings.Secure.putInt(mContext.getContentResolver(),
                Settings.Secure.PANIC_IN_POWER_MENU, isChecked ? 1 : 0);
        return true;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return 0; // TODO Actual res
    }
}
