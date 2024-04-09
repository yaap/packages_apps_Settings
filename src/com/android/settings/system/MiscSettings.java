/*
 * Copyright (C) 2024 Yet Another AOSP Project
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

import android.app.backup.IBackupManager;
import android.app.backup.SelectBackupTransportCallback;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.widget.Toast;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;

import java.util.Arrays;

@SearchIndexable
public class MiscSettings extends DashboardFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "MiscSettings";

    private static final String TRANSPORT_SELECTOR_KEY = "transport_selector";

    private IBackupManager mBackupManager;
    private Toast mToast;
    private ListPreference mTransportSelector;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mBackupManager = IBackupManager.Stub.asInterface(
                ServiceManager.getService(Context.BACKUP_SERVICE));
        mTransportSelector = findPreference(TRANSPORT_SELECTOR_KEY);
        try {
            final String current = mBackupManager.getCurrentTransportForUser(UserHandle.USER_SYSTEM);
            final String[] transports = mBackupManager.listAllTransportsForUser(UserHandle.USER_SYSTEM);
            final String[] entries = new String[transports.length];
            for (int i = 0; i < transports.length; i++) {
                final String[] segments = transports[i].split("/");
                if (segments.length < 2) {
                    entries[i] = transports[i];
                    continue;
                }
                final String[] activitySegments = segments[1].split("\\.");
                entries[i] = activitySegments[activitySegments.length - 1] + " (" + segments[0] + ")";
            }
            mTransportSelector.setEntries(entries);
            mTransportSelector.setEntryValues(transports);
            mTransportSelector.setValue(current);
            mTransportSelector.setSummary(mTransportSelector.getEntry());
            mTransportSelector.setOnPreferenceChangeListener(this);
        } catch (RemoteException e) {
            e.printStackTrace();
            mTransportSelector.setVisible(false);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mTransportSelector) {
            final String value = (String) objValue;
            try {
                final String old = mBackupManager.selectBackupTransportForUser(
                        UserHandle.USER_SYSTEM, value);
                if (old == null) {
                    showToast(R.string.failure);
                    return false;
                }
                int index = mTransportSelector.findIndexOfValue(value);
                mTransportSelector.setSummary(mTransportSelector.getEntries()[index]);
            } catch (RemoteException e) {
                e.printStackTrace();
                showToast(R.string.failure);
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.yaap_settings_misc;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.YASP;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    private synchronized void showToast(int msgId) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(getContext(), msgId, Toast.LENGTH_LONG);
        mToast.show();
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.yaap_settings_misc);
}
