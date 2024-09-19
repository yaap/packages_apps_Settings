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
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.webkit.URLUtil;

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

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.yaap_settings_about;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        final Resources res = getResources();
        final ContentResolver resolver = getActivity().getContentResolver();

        // starting from 10 (inclusive) and end at order 100 at max
        int order = 10;

        // adding maintainers
        final String maintainers[] = res.getString(R.string.maintainer_name).split(",");
        final String maintainerLinks[] = res.getString(R.string.maintainer_telegram).split(",");
        for (int i = 0; i < maintainers.length && order < 100; i++) {
            final String name = maintainers[i];
            final String link = i < maintainerLinks.length ? maintainerLinks[i] : "";
            if (name == null || name.isEmpty())
                continue;
            addMaintainerPref(name, link, order++);
        }

        // adding kernel devs
        final String kernels[] = res.getString(R.string.kernel_name).split(",");
        final String kernelLinks[] = res.getString(R.string.kernel_telegram).split(",");
        for (int i = 0; i < kernels.length && order < 100; i++) {
            final String name = kernels[i];
            final String link = i < kernelLinks.length ? kernelLinks[i] : "";
            if (name == null || name.isEmpty())
                continue;
            addMaintainerPref(name, link, true, order++);
        }
    }

    private void addMaintainerPref(String name, String link, int order) {
        addMaintainerPref(name, link, false, order);
    }

    private void addMaintainerPref(String name, String link, boolean kernel, int order) {
        final String sum = getResources().getString(
                kernel ? R.string.kernel : R.string.maintainer);
        Preference pref = new Preference(getContext());
        pref.setTitle(name);
        pref.setSummary(sum);
        pref.setOrder(order);
        pref.setCopyingEnabled(false);
        if (link != null && !link.isEmpty() &&
                URLUtil.isValidUrl(link) && URLUtil.isHttpsUrl(link)) {
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(link));
            pref.setIntent(intent);
        }
        getPreferenceScreen().addPreference(pref);
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
