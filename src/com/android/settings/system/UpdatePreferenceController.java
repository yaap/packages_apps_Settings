/*
 * Copyright (C) 2017 The Android Open Source Project
 * Copyright (C) 2017 The LineageOS Project
 * Copyright (C) 2022 Yet Another AOSP Project
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
package com.android.settings.deviceinfo;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;

public class UpdatePreferenceController extends BasePreferenceController {

    private static final String KEY_UPDATE_SETTING = "update_settings";

    public UpdatePreferenceController(Context context) {
        super(context, KEY_UPDATE_SETTING);
    }

    @Override
    public int getAvailabilityStatus() {
        String packagename = mContext.getResources().getString(
                com.android.settings.R.string.update_package);
        try {
            ApplicationInfo ai = mContext.getPackageManager().getApplicationInfo(packagename, 0);
            return ai.enabled ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
        } catch (PackageManager.NameNotFoundException e) { }
        return UNSUPPORTED_ON_DEVICE;
    }
}
