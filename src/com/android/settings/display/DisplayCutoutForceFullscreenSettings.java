/**
 * Copyright (C) 2018 The LineageOS Project
 * Copyright (C) 2019 PixelExperience
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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.custom.cutout.CutoutFullscreenController;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.R;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SearchIndexable
public class DisplayCutoutForceFullscreenSettings extends DashboardFragment
        implements ApplicationsState.Callbacks {

    private static final String TAG = "DisplayCutoutForceFullscreenSettings";
    private static final String[] HIDE_APPS = {
            "com.android.settings", "com.android.documentsui",
            "com.android.fmradio", "com.caf.fmradio", "com.android.stk",
            "com.google.android.calculator", "com.google.android.calendar",
            "com.google.android.deskclock", "com.google.android.contacts",
            "com.google.android.apps.messaging", "com.google.android.googlequicksearchbox",
            "com.android.vending", "com.google.android.dialer",
            "com.google.android.apps.wallpaper", "com.google.android.as"
        };

    private ApplicationsState mApplicationsState;
    private ApplicationsState.Session mSession;
    private ActivityFilter mActivityFilter;
    private CutoutFullscreenController mCutoutForceFullscreenSettings;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.DISPLAY_CUTOUT_FORCE_FULLSCREEN;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.display_cutout_force_fullscreen_settings;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mApplicationsState = ApplicationsState.getInstance(getActivity().getApplication());
        mSession = mApplicationsState.newSession(this);
        mSession.onResume();
        mActivityFilter = new ActivityFilter(getActivity().getPackageManager());
        mCutoutForceFullscreenSettings = new CutoutFullscreenController(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        rebuild();
    }

    @Override
    public void onDestroy() {
        mSession.onPause();
        mSession.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onPackageListChanged() {
        mActivityFilter.updateLauncherInfoList();
        rebuild();
    }

    @Override
    public void onRebuildComplete(ArrayList<ApplicationsState.AppEntry> entries) {
        if (entries != null) {
            addAllAppPrefs(entries);
        }
    }

    @Override
    public void onLoadEntriesCompleted() {
        rebuild();
    }

    @Override
    public void onAllSizesComputed() {}

    @Override
    public void onLauncherInfoChanged() {}

    @Override
    public void onPackageIconChanged() {}

    @Override
    public void onPackageSizeChanged(String packageName) {}

    @Override
    public void onRunningStateChanged(boolean running) {}

    private void rebuild() {
        mSession.rebuild(mActivityFilter, ApplicationsState.ALPHA_COMPARATOR);
    }

    private void addAllAppPrefs(List<ApplicationsState.AppEntry> entries) {
        getPreferenceScreen().removeAll();
        for (ApplicationsState.AppEntry entry : entries) {
            final String packageName = entry.info.packageName;
            mApplicationsState.ensureIcon(entry);
            SwitchPreferenceCompat pref = new SwitchPreferenceCompat(getContext());
            pref.setTitle(entry.label);
            pref.setIcon(entry.icon);
            pref.setChecked(mCutoutForceFullscreenSettings.shouldForceCutoutFullscreen(packageName));
            pref.setOnPreferenceChangeListener((preference, newValue) -> {
                final boolean value = (Boolean) newValue;
                if (value) {
                    mCutoutForceFullscreenSettings.addApp(packageName);
                } else {
                    mCutoutForceFullscreenSettings.removeApp(packageName);
                }
                Toast.makeText(getActivity(),
                        getActivity().getString(R.string.display_cutout_force_fullscreen_restart_app),
                        Toast.LENGTH_SHORT).show();
                return true;
            });
            getPreferenceScreen().addPreference(pref);
        }
    }

    private class ActivityFilter implements ApplicationsState.AppFilter {

        private final PackageManager mPackageManager;
        private final List<String> mLauncherResolveInfoList = new ArrayList<>();

        private ActivityFilter(PackageManager packageManager) {
            this.mPackageManager = packageManager;

            updateLauncherInfoList();
        }

        public void updateLauncherInfoList() {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> resolveInfoList = mPackageManager.queryIntentActivities(i, 0);

            synchronized (mLauncherResolveInfoList) {
                mLauncherResolveInfoList.clear();
                for (ResolveInfo ri : resolveInfoList) {
                    mLauncherResolveInfoList.add(ri.activityInfo.packageName);
                }
            }
        }

        @Override
        public void init() {}

        @Override
        public boolean filterApp(ApplicationsState.AppEntry entry) {
            return mLauncherResolveInfoList.contains(entry.info.packageName)
                    && !Arrays.asList(HIDE_APPS).contains(entry.info.packageName);
        }
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.display_cutout_force_fullscreen_settings);
}
