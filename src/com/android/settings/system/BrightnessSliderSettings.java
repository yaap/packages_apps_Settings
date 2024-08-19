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
import android.provider.Settings;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;
import com.android.settingslib.widget.MainSwitchPreference;

@SearchIndexable
public class BrightnessSliderSettings extends DashboardFragment {

    private static final String TAG = "BrightnessSliderSettings";
    private static final String KEY_MASTER = "qs_show_brightness";
    private static final String SLIDER_HAPTICS_KEY = "brightness_slider_haptics";

    private MainSwitchPreference mMasterSwitch;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mMasterSwitch = findPreference(KEY_MASTER);
        boolean enabled = Settings.Secure.getInt(
                getContentResolver(), KEY_MASTER, 1) == 1;
        mMasterSwitch.setChecked(enabled);
        mMasterSwitch.addOnSwitchChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Settings.Secure.putInt(getContentResolver(),
                        KEY_MASTER, isChecked ? 1 : 0);
                updateMasterEnablement(isChecked);
            }
        });
        updateMasterEnablement();

        if (!isSliderHapticsSupported()) {
            Preference sliderHaptics = findPreference(SLIDER_HAPTICS_KEY);
            if (sliderHaptics != null) sliderHaptics.setVisible(false);
        }
    }

    private void updateMasterEnablement() {
        final boolean enabled = Settings.Secure.getInt(
                getContentResolver(), KEY_MASTER, 1) == 1;
        updateMasterEnablement(enabled);
    }

    private void updateMasterEnablement(boolean enabled) {
        final PreferenceScreen screen = getPreferenceScreen();
        for (int i = 0; i < screen.getPreferenceCount(); i++) {
            Preference pref = screen.getPreference(i);
            if (KEY_MASTER.equals(pref.getKey()))
                continue;
            pref.setEnabled(enabled);
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
