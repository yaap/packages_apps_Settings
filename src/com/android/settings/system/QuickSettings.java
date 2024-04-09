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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.yasp.settings.preferences.SecureSettingMasterSwitchPreference;
import com.yasp.settings.preferences.SystemSettingEditTextPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class QuickSettings extends DashboardFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "QuickSettings";
    private static final String QS_FOOTER_TEXT_STRING = "qs_footer_text_string";
    private static final String BRIGHTNESS_SLIDER = "qs_show_brightness";

    private SystemSettingEditTextPreference mFooterString;
    private SecureSettingMasterSwitchPreference mBrightnessSlider;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.yaap_settings_quicksettings;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mBrightnessSlider = (SecureSettingMasterSwitchPreference)
                findPreference(BRIGHTNESS_SLIDER);
        mBrightnessSlider.setOnPreferenceChangeListener(this);
        boolean enabled = Settings.Secure.getInt(resolver,
                BRIGHTNESS_SLIDER, 1) == 1;
        mBrightnessSlider.setChecked(enabled);

        mFooterString = (SystemSettingEditTextPreference) findPreference(QS_FOOTER_TEXT_STRING);
        mFooterString.setOnPreferenceChangeListener(this);
        String footerString = Settings.System.getString(resolver,
                QS_FOOTER_TEXT_STRING);
        if (footerString != null && !footerString.isEmpty())
            mFooterString.setText(footerString);
        else {
            mFooterString.setText("YAAP");
            Settings.System.putString(resolver,
                    Settings.System.QS_FOOTER_TEXT_STRING, "YAAP");
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mFooterString) {
            String value = (String) newValue;
            if (value != null && !value.isEmpty())
                Settings.System.putString(resolver,
                        Settings.System.QS_FOOTER_TEXT_STRING, value);
            else {
                mFooterString.setText("YAAP");
                Settings.System.putString(resolver,
                        Settings.System.QS_FOOTER_TEXT_STRING, "YAAP");
            }
            return true;
        } else if (preference == mBrightnessSlider) {
            Boolean value = (Boolean) newValue;
            Settings.Secure.putInt(resolver,
                    BRIGHTNESS_SLIDER, value ? 1 : 0);
            return true;
        }
        return false;
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
            new BaseSearchIndexProvider(R.xml.yaap_settings_quicksettings);
}
