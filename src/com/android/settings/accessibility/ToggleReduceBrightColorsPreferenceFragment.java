/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.settings.accessibility;

import static com.android.internal.util.yaap.AutoSettingConsts.MODE_DISABLED;
import static com.android.internal.util.yaap.AutoSettingConsts.MODE_NIGHT;
import static com.android.internal.util.yaap.AutoSettingConsts.MODE_TIME;
import static com.android.internal.util.yaap.AutoSettingConsts.MODE_MIXED_SUNSET;
import static com.android.internal.util.yaap.AutoSettingConsts.MODE_MIXED_SUNRISE;
import static com.android.settings.core.BasePreferenceController.AVAILABLE;

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.preference.Preference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.settings.R;
import com.android.settings.accessibility.extradim.ui.ExtraDimScreen;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

/** Settings for reducing brightness. */
@SearchIndexable
public class ToggleReduceBrightColorsPreferenceFragment extends DashboardFragment {
    private static final String TAG = "ToggleReduceBrightColorsPreferenceFragment";
    private static final String KEY_SCHEDULE = "extra_dim_schedule";

    private Preference mSchedulePref;

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mSchedulePref = new Preference(getPrefContext());
        mSchedulePref.setKey(KEY_SCHEDULE);
        mSchedulePref.setTitle(getText(R.string.extra_dim_schedule_title));
        mSchedulePref.setFragment("com.android.settings.accessibility.ExtraDimScheduleFragment");

        updateSchedulePreference();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void updateSchedulePreference() {
        if (mSchedulePref == null) return;
        int mode = Settings.Secure.getIntForUser(getActivity().getContentResolver(),
                Settings.Secure.EXTRA_DIM_AUTO_MODE, 0, UserHandle.USER_CURRENT);
        switch (mode) {
            default:
            case MODE_DISABLED:
                mSchedulePref.setSummary(R.string.disabled);
                break;
            case MODE_NIGHT:
                mSchedulePref.setSummary(R.string.night_display_auto_mode_twilight);
                break;
            case MODE_TIME:
                mSchedulePref.setSummary(R.string.night_display_auto_mode_custom);
                break;
            case MODE_MIXED_SUNSET:
                mSchedulePref.setSummary(R.string.always_on_display_schedule_mixed_sunset);
                break;
            case MODE_MIXED_SUNRISE:
                mSchedulePref.setSummary(R.string.always_on_display_schedule_mixed_sunrise);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSchedulePreference();
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.REDUCE_BRIGHT_COLORS_SETTINGS;
    }

    @Override
    public int getHelpResource() {
        // TODO(b/170973645): Link to help support page
        return 0;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return 0;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public @Nullable String getPreferenceScreenBindingKey(@NonNull Context context) {
        return ExtraDimScreen.KEY;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                protected boolean isPageSearchEnabled(Context context) {
                    return ReduceBrightColorsExtensionsKt.getReduceBrightColorsAvailabilityStatus(
                            context) == AVAILABLE;
                }
            };
}
