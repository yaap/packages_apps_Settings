/*
 * Copyright (C) 2024 Yet Another AOSP Project
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

package com.android.settings.applications.appinfo;

import android.content.Context;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.TwoStatePreference;

import java.lang.StringBuilder;

/**
 * A PreferenceController handling the logic for enabling gaming macro for an app
 */
public final class GamingMacroAppSwitchPreferenceController extends AppInfoPreferenceControllerBase
        implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "GamingMacroAppSwitchPreferenceController";
    private String mPackageName;

    public GamingMacroAppSwitchPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    /**
     * Set the package name. This method should only be called to initialize the controller.
     * @param packageName The name of the package whose hibernation state to be managed.
     * @return this preference for chaining
     */
    GamingMacroAppSwitchPreferenceController setPackageName(@NonNull String packageName) {
        mPackageName = packageName;
        return this;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        String value = Settings.System.getString(mContext.getContentResolver(),
                Settings.System.GAMING_MODE_APPS);
        boolean isEnabled = mPackageName != null && value != null && !value.isEmpty()
                && value.contains(mPackageName);
        ((TwoStatePreference) preference).setChecked(isEnabled);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object isChecked) {
        if (mPackageName == null) {
            return false;
        }

        final boolean checked = (boolean) isChecked;
        String value = Settings.System.getString(mContext.getContentResolver(),
                Settings.System.GAMING_MODE_APPS);

        // the setting is empty
        // deal with checked & !checked
        if (value == null || value.isEmpty()) {
            if (checked) {
                Settings.System.putString(mContext.getContentResolver(),
                        Settings.System.GAMING_MODE_APPS, mPackageName);
            }
            return true;
        }

        // the setting is not empty
        // deal with checked
        if (checked) {
            if (!value.contains(mPackageName)) {
                value = value + "," + mPackageName;
                Settings.System.putString(mContext.getContentResolver(),
                        Settings.System.GAMING_MODE_APPS, value);
            }
            return true;
        }

        // deal with !checked
        if (!value.contains(mPackageName)) {
            return true;
        }
        String[] apps = value.split(",");
        StringBuilder saveVal = new StringBuilder();
        boolean first = true;
        for (String app : apps) {
            if (app.equals(mPackageName)) {
                // do not add the disabled app
                continue;
            }

            if (first) {
                saveVal.append(app);
                first = false;
                continue;
            }

            saveVal.append("," + app);
        }
        Settings.System.putString(mContext.getContentResolver(),
                Settings.System.GAMING_MODE_APPS, saveVal.toString());
        return true;
    }
}
