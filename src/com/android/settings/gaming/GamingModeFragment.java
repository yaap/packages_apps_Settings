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
package com.android.settings.gaming;

import android.content.Context;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class GamingModeFragment extends DashboardFragment {

    private static final String LOG_TAG = "GamingModeFragment";

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.YASP;
    }

    @Override
    protected String getLogTag() {
        return LOG_TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.gaming_mode;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new GamingModeController(context));
        return controllers;
    }

    /**
     * For Search.
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.gaming_mode) {

                @Override
                public List<AbstractPreferenceController> createPreferenceControllers(
                        Context context) {
                    return buildPreferenceControllers(context);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    final List<String> keys = super.getNonIndexableKeys(context);
                    if (!GamingModeController.isTouchSensitivityAvailable(context)) {
                        keys.add(GamingModeController.GAMING_MODE_TOUCH_SENSITIVITY_KEY);
                    }
                    if (!GamingModeController.isHighTouchRateAvailable(context)) {
                        keys.add(GamingModeController.GAMING_MODE_HIGH_TOUCH_RATE_KEY);
                    }
                    if (!GamingModeController.isEdgeGripSuppressionAvailable(context)) {
                        keys.add(GamingModeController.GAMING_MODE_EDGE_GRIP_SUPPRESSION_KEY);
                    }
                    if (!GamingModeController.isLtpoFeaturesAvailable(context)) {
                        keys.add(GamingModeController.GAMING_MODE_LTPO_FEATURES_KEY);
                    }
                    if (!GamingModeController.isColorModeAvailable(context)) {
                        keys.add(GamingModeController.GAMING_MODE_COLOR_KEY);
                    }
                    if (!GamingModeController.isSmoothDisplayAvailable(context)) {
                        keys.add(GamingModeController.GAMING_MODE_SMOOTH_DISPLAY_KEY);
                    }
                    return keys;
                }
            };

}
