/*
 * Copyright (C) 2023 Yet Another AOSP Project
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

package com.android.settings.fuelgauge.batterysaver;

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.text.TextUtils;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.List;

/**
 * Advanced battery saver settings page
 */
@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class BatterySaverAdvancedSettings extends DashboardFragment {
    private static final String TAG = "BatteryAdvancedSaverSettings";

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.OPEN_BATTERY_SAVER;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.battery_saver_advanced_settings;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        final BatterySaverAdvancedSettingsPreferenceController advancedSettingsController =
                new BatterySaverAdvancedSettingsPreferenceController(context);
        controllers.add(advancedSettingsController);
        return controllers;
    }

    /**
     * For Search.
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.battery_saver_advanced_settings) {
        @Override
        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            final List<AbstractPreferenceController> controllers = new ArrayList<>();
            final BatterySaverAdvancedSettingsPreferenceController advancedSettingsController =
                    new BatterySaverAdvancedSettingsPreferenceController(context);
            controllers.add(advancedSettingsController);
            return controllers;
        }
    };
}
