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
package com.android.settings.deviceinfo;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SearchIndexable
public class AboutYaap extends DashboardFragment {

    private static final String TAG = "AboutYaap";
    private static final String PREF_MAINTAINER = "maintainer";
    private static final String PREF_KERNEL = "kernel";

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.yaap_settings_about;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        final Resources res = getResources();
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        Preference maintainerName = (Preference) findPreference(PREF_MAINTAINER);
        if (maintainerName.getTitle().equals("")) {
            maintainerName.setVisible(false);
        } else if (res.getString(R.string.maintainer_telegram).equals("")) {
            maintainerName.setEnabled(false);
        }

        Preference kernelName = (Preference) findPreference(PREF_KERNEL);
        if (kernelName.getTitle().equals("")) {
            kernelName.setVisible(false);
        } else if (res.getString(R.string.kernel_telegram).equals("")) {
            kernelName.setEnabled(false);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.YASP;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.yaap_settings_about);
}
