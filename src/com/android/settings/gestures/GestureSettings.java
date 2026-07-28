/*
 * Copyright (C) 2017 The Android Open Source Project
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

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.hardware.display.AmbientDisplayConfiguration;
import android.os.Bundle;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.display.TapToWakePreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.List;

// LINT.IfChange
@SearchIndexable
public class GestureSettings extends DashboardFragment {

    private static final String TAG = "GestureSettings";
    private static final String PREF_KEY_PREVENT_RINGING = "gesture_prevent_ringing_summary";
    private static final String PREF_KEY_SCREENSHOT_KEYCOMBO = "screenshot_key_gesture_enabled";

    private AmbientDisplayConfiguration mAmbientDisplayConfig;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        if (!isScreenshotKeyGestureAvailable(getContext())) {
            Preference screenshotKeyPref = findPreference(PREF_KEY_SCREENSHOT_KEYCOMBO);
            if (screenshotKeyPref != null) {
                screenshotKeyPref.setVisible(false);
            }
        }
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.SETTINGS_GESTURES;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.gestures;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        use(PickupGesturePreferenceController.class).setConfig(getConfig(context));
        use(DoubleTapScreenPreferenceController.class).setConfig(getConfig(context));
        use(ScreenOffUdfpsPreferenceController.class).setConfig(getConfig(context));
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new TapToWakePreferenceController(context));
        return controllers;
    }

    private AmbientDisplayConfiguration getConfig(Context context) {
        if (mAmbientDisplayConfig == null) {
            mAmbientDisplayConfig = new AmbientDisplayConfiguration(context);
        }
        return mAmbientDisplayConfig;
    }

    private static boolean isScreenshotKeyGestureAvailable(Context context) {
        return context.getResources().getBoolean(
                com.android.internal.R.bool.config_enableScreenshotChord);
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.gestures) {
                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    final List<String> keys = super.getNonIndexableKeys(context);
                    // de-duplicated due to another same entry in Sound page
                    keys.add(PREF_KEY_PREVENT_RINGING);
                    if (!isScreenshotKeyGestureAvailable(context)) {
                        keys.add(PREF_KEY_SCREENSHOT_KEYCOMBO);
                    }
                    if (!TapToWakePreferenceController.isAvailable(context)) {
                        keys.add(TapToWakePreferenceController.KEY_TAP_TO_WAKE);
                    }
                    if (!DoubleTapScreenPreferenceController.isAvailable(context)) {
                        keys.add(DoubleTapScreenPreferenceController.KEY);
                    }
                    if (!TapScreenGesturePreferenceController.isAvailable(context)) {
                        keys.add(TapScreenGesturePreferenceController.KEY);
                    }
                    if (!PickupGesturePreferenceController.isAvailable(context)) {
                        keys.add(PickupGesturePreferenceController.KEY);
                    }
                    return keys;
                }
            };
}
// LINT.ThenChange(GestureSettingsApiScreen.kt)
