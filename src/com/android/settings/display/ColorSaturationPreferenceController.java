/*
 * Copyright (C) 2018 The Android Open Source Project
 * Copyright (C) 2025 Paranoid Android
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
import android.hardware.display.ColorDisplayManager;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.core.SliderPreferenceController;
import com.yasp.settings.preferences.CustomSeekBarPreference;

public class ColorSaturationPreferenceController extends SliderPreferenceController {

    private static final int MIN_SATURATION = 0;
    private static final int MAX_SATURATION = 200;

    private final ColorDisplayManager mColorDisplayManager;

    public ColorSaturationPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        mColorDisplayManager = context.getSystemService(ColorDisplayManager.class);
    }

    @Override
    public int getAvailabilityStatus() {
        return ColorDisplayManager.isColorTransformAccelerated(mContext)
                ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public boolean isSliceable() {
        return true;
    }

    @Override
    public boolean isPublicSlice() {
        return true;
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
        return mColorDisplayManager.getUserSaturationLevel();
    }

    @Override
    public boolean setSliderPosition(int position) {
        return mColorDisplayManager.setUserSaturationLevel(
                Math.max(MIN_SATURATION, Math.min(MAX_SATURATION, position)));
    }

    @Override
    public int getMax() {
        return MAX_SATURATION;
    }

    @Override
    public int getMin() {
        return MIN_SATURATION;
    }
}
