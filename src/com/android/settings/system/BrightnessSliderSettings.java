/*
 * Copyright (C) 2020 Yet Another AOSP Project
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
package com.android.settings.system;

import android.content.Context;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.preference.Preference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

@SearchIndexable
public class BrightnessSliderSettings extends DashboardFragment {

    private static final String TAG = "BrightnessSliderSettings";
    private static final String SLIDER_HAPTICS_KEY = "brightness_slider_haptics";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        if (!isSliderHapticsSupported()) {
            Preference sliderHaptics = findPreference(SLIDER_HAPTICS_KEY);
            if (sliderHaptics != null) sliderHaptics.setVisible(false);
        }
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.yaap_settings_brightness_slider;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.YASP;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    private boolean isSliderHapticsSupported() {
        Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null || !vibrator.hasVibrator()) {
            return false; // device has no vibrator
        }
        if (vibrator.areAllPrimitivesSupported(
                VibrationEffect.Composition.PRIMITIVE_LOW_TICK,
                VibrationEffect.Composition.PRIMITIVE_CLICK)) {
            return true; // device supports primitives
        }
        int max = getContext().getResources().getInteger(
                com.android.internal.R.integer.config_sliderVibFallbackDuration);
        if (max <= 0) {
            return false; // fallbacks are not set
        }
        return true; // does not support primitives but fallbacks are set
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.yaap_settings_brightness_slider);
}
