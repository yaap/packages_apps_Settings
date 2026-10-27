/*
 * Copyright (C) 2026 The YAAP Project
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

package com.android.settings.display;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.SliderPreferenceController;
import com.yasp.settings.preferences.CustomSeekBarPreference;

public class EdgeGripSuppressionPreferenceController extends SliderPreferenceController {

    private static final String SETTINGS_KEY = Settings.Secure.EDGE_GRIP_SUPPRESSION;
    private static final String PROP_NAME = "debug.edge_grip_suppression";
    private static final int MIN_VALUE = 0;
    private static final int MAX_VALUE = 5;
    private static final int DEFAULT_VALUE = 3;

    public EdgeGripSuppressionPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return mContext.getResources().getBoolean(R.bool.config_supportEdgeGripSuppression)
                ? AVAILABLE
                : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        final CustomSeekBarPreference preference = screen.findPreference(getPreferenceKey());
        if (preference != null) {
            preference.setValue(getSliderPosition());
            preference.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public void updateState(Preference preference) {
        if (preference instanceof CustomSeekBarPreference) {
            ((CustomSeekBarPreference) preference).setValue(getSliderPosition());
        }
    }

    @Override
    public int getSliderPosition() {
        return clamp(Settings.Secure.getInt(
                mContext.getContentResolver(), SETTINGS_KEY, DEFAULT_VALUE));
    }

    @Override
    public boolean setSliderPosition(int position) {
        final int value = clamp(position);
        Settings.Secure.putInt(mContext.getContentResolver(), SETTINGS_KEY, value);
        SystemProperties.set(PROP_NAME, String.valueOf(value));
        return true;
    }

    @Override
    public int getMax() {
        return MAX_VALUE;
    }

    @Override
    public int getMin() {
        return MIN_VALUE;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_display;
    }

    private static int clamp(int value) {
        return Math.max(MIN_VALUE, Math.min(MAX_VALUE, value));
    }
}
