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

import static com.android.internal.display.RefreshRateSettingsUtils.DEFAULT_REFRESH_RATE;
import static com.android.internal.display.RefreshRateSettingsUtils.findHighestRefreshRateAmongAllDisplays;

import android.content.ContentResolver;
import android.content.Context;
import android.hardware.display.ColorDisplayManager;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.display.ColorModeUtils;
import com.android.settingslib.core.AbstractPreferenceController;

import com.yasp.settings.preferences.CustomSeekBarPreference;
import com.yasp.settings.preferences.SystemSettingListPreference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GamingModeController extends AbstractPreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY = "gaming_mode";
    private static final String GAMING_MODE_MEDIA_KEY = "gaming_mode_media";
    private static final String GAMING_MODE_BRIGHTNESS_KEY = "gaming_mode_brightness";
    private static final String GAMING_MODE_RINGER_KEY = "gaming_mode_ringer";
    public static final String GAMING_MODE_SMOOTH_DISPLAY_KEY = "gaming_mode_smooth_display";
    public static final String GAMING_MODE_TOUCH_SENSITIVITY_KEY = "gaming_mode_touch_sensitivity";
    public static final String GAMING_MODE_HIGH_TOUCH_RATE_KEY = "gaming_mode_high_touch_rate";
    public static final String GAMING_MODE_LTPO_FEATURES_KEY = "gaming_mode_ltpo_features";
    public static final String GAMING_MODE_COLOR_KEY = "gaming_mode_color_mode";

    private CustomSeekBarPreference mMediaVolume;
    private CustomSeekBarPreference mBrightnessLevel;
    private SystemSettingListPreference mRingerMode;
    private SystemSettingListPreference mColorMode;

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

        if (!isTouchSensitivityAvailable(mContext)) {
            Preference pref = screen.findPreference(GAMING_MODE_TOUCH_SENSITIVITY_KEY);
            pref.setVisible(false);
        }

        if (!isHighTouchRateAvailable(mContext)) {
            Preference pref = screen.findPreference(GAMING_MODE_HIGH_TOUCH_RATE_KEY);
            pref.setVisible(false);
        }

        if (!isLtpoFeaturesAvailable(mContext)) {
            Preference pref = screen.findPreference(GAMING_MODE_LTPO_FEATURES_KEY);
            pref.setVisible(false);
        }

        if (!isSmoothDisplayAvailable(mContext)) {
            Preference pref = screen.findPreference(GAMING_MODE_SMOOTH_DISPLAY_KEY);
            pref.setVisible(false);
        }

        mColorMode = screen.findPreference(GAMING_MODE_COLOR_KEY);
        if (isColorModeAvailable(mContext)) {
            List<Integer> availableColorModes = Arrays.stream(
                    ColorModeUtils.getAvailableColorModes(mContext)).boxed().toList();
            Map<Integer, String> colorModeMapping = ColorModeUtils.getColorModeMapping(
                    mContext.getResources());

            List<String> entries = new ArrayList<>();
            entries.add(mContext.getString(R.string.gaming_mode_ringer_disabled));
            List<String> values = new ArrayList<>();
            values.add("-1");
            for (Map.Entry<Integer, String> entry : colorModeMapping.entrySet()) {
                if (!availableColorModes.contains(entry.getKey())) {
                    continue;
                }
                // entries are values and values are keys
                entries.add(entry.getValue());
                values.add(String.valueOf(entry.getKey()));
            }

            CharSequence[] entriesArr = new CharSequence[entries.size()];
            entriesArr = entries.toArray(entriesArr);
            mColorMode.setEntries(entriesArr);

            CharSequence[] entryValuesArr = new CharSequence[values.size()];
            entryValuesArr = values.toArray(entryValuesArr);
            mColorMode.setEntryValues(entryValuesArr);

            value = Settings.System.getInt(resolver, GAMING_MODE_COLOR_KEY, -1);
            mColorMode.setValue(String.valueOf(value));
            mColorMode.setSummary(value == -1
                    ? mContext.getString(R.string.gaming_mode_ringer_disabled)
                    : mColorMode.getEntries()[value]);
            mColorMode.setOnPreferenceChangeListener(this);
        } else {
            mColorMode.setVisible(false);
        }
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
        } else if (preference == mColorMode) {
            int value = Integer.parseInt((String) newValue);
            int index = mColorMode.findIndexOfValue((String) newValue);
            mColorMode.setSummary(mColorMode.getEntries()[index]);
            Settings.System.putInt(resolver, GAMING_MODE_COLOR_KEY, value);
            return true;
        }
        return false;
    }

    public static boolean isTouchSensitivityAvailable(Context context) {
        return context.getResources().getBoolean(com.android.internal.R.bool.config_supportGloveMode);
    }

    public static boolean isHighTouchRateAvailable(Context context) {
	    return context.getResources().getBoolean(R.bool.config_supportHighTouchRate);
    }

    public static boolean isLtpoFeaturesAvailable(Context context) {
	    return context.getResources().getBoolean(R.bool.config_supportLtpoFeatures);
    }

    public static boolean isColorModeAvailable(Context context) {
        final int[] availableColorModes = ColorModeUtils.getAvailableColorModes(context);
        if (availableColorModes.length == 0) {
            return false;
        }
        if (!context.getSystemService(ColorDisplayManager.class)
                .isDeviceColorManaged()) {
            return false;
        }
        if (ColorDisplayManager.areAccessibilityTransformsEnabled(context)) {
            return false;
        }
        return true;
    }

    public static boolean isSmoothDisplayAvailable(Context context) {
        if (!context.getResources().getBoolean(R.bool.config_show_smooth_display)) {
            return false;
        }
        final int peak = Math.round(findHighestRefreshRateAmongAllDisplays(context));
        return peak > DEFAULT_REFRESH_RATE;
    }
}
