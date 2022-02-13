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

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settingslib.core.AbstractPreferenceController;

import com.yasp.settings.preferences.CustomSeekBarPreference;
import com.yasp.settings.preferences.SystemSettingListPreference;

public class GamingModeController extends AbstractPreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY = "gaming_mode";
    private static final String GAMING_MODE_MEDIA_KEY = "gaming_mode_media";
    private static final String GAMING_MODE_BRIGHTNESS_KEY = "gaming_mode_brightness";
    private static final String GAMING_MODE_RINGER_KEY = "gaming_mode_ringer";

    CustomSeekBarPreference mMediaVolume;
    CustomSeekBarPreference mBrightnessLevel;
    SystemSettingListPreference mRingerMode;

    public GamingModeController(Context context) {
        super(context);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        final ContentResolver resolver = mContext.getContentResolver();

        mMediaVolume = screen.findPreference(GAMING_MODE_MEDIA_KEY);
        int value = Settings.System.getInt(resolver, GAMING_MODE_MEDIA_KEY, 80);
        mMediaVolume.setValue(value);
        mMediaVolume.setOnPreferenceChangeListener(this);

        mBrightnessLevel = screen.findPreference(GAMING_MODE_BRIGHTNESS_KEY);
        value = Settings.System.getInt(resolver, GAMING_MODE_BRIGHTNESS_KEY, 80);
        mBrightnessLevel.setValue(value);
        mBrightnessLevel.setOnPreferenceChangeListener(this);

        mRingerMode = screen.findPreference(GAMING_MODE_RINGER_KEY);
        value = Settings.System.getInt(resolver, GAMING_MODE_RINGER_KEY, 0);
        mRingerMode.setValue(Integer.toString(value));
        mRingerMode.setSummary(mRingerMode.getEntry());
        mRingerMode.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final ContentResolver resolver = mContext.getContentResolver();
        if (preference == mMediaVolume) {
            int value = (Integer) newValue;
            Settings.System.putInt(resolver, GAMING_MODE_MEDIA_KEY, value);
            return true;
        } else if (preference == mBrightnessLevel) {
            int value = (Integer) newValue;
            Settings.System.putInt(resolver, GAMING_MODE_BRIGHTNESS_KEY, value);
            return true;
        } else if (preference == mRingerMode) {
            int value = Integer.parseInt((String) newValue);
            int index = mRingerMode.findIndexOfValue((String) newValue);
            mRingerMode.setSummary(mRingerMode.getEntries()[index]);
            Settings.System.putInt(resolver, GAMING_MODE_RINGER_KEY, value);
            return true;
        }
        return false;
    }
}