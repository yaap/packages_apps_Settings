/*
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

package com.android.settings.gestures;

import static android.provider.Settings.Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED;

import android.content.Context;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settingslib.core.AbstractPreferenceController;

public class TorchPowerPreferenceController extends AbstractPreferenceController
        implements Preference.OnPreferenceChangeListener {
    private static final String KEY = "torch_power_button_gesture";

    private ListPreference mPreference;

    public TorchPowerPreferenceController(Context context) {
        super(context);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(KEY);
        final int value = Settings.System.getInt(
                mContext.getContentResolver(), KEY, 0);
        mPreference.setValueIndex(value);
        mPreference.setSummary(mPreference.getEntries()[value]);
        mPreference.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mPreference) {
            final int value = Integer.valueOf((String) newValue);
            Settings.System.putInt(
                    mContext.getContentResolver(), KEY, value);
            mPreference.setSummary(mPreference.getEntries()[value]);
            return true;
        }
        return false;
    }
}
