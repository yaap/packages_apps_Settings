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

import android.content.Context;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;

import com.yasp.settings.preferences.CustomSeekBarPreference;

import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BatterySaverAdvancedSettingsPreferenceController extends AbstractPreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "BatterySaverAdvancedSettingsPreferenceController";

    private static final String KEY_ENABLE_BRIGHTNESS_ADJUSTMENT = "enable_brightness_adjustment";
    private static final String KEY_ADJUST_BRIGHTNESS_FACTOR = "adjust_brightness_factor";
    private static final String KEY_DISABLE_AOD = "disable_aod";
    private static final String KEY_ENABLE_NIGHT_MODE = "enable_night_mode";
    private static final String KEY_DISABLE_ANIMATION = "disable_animation";
    private static final String KEY_DISABLE_LAUNCH_BOOST = "disable_launch_boost";
    private static final String KEY_ENABLE_QUICK_DOZE = "enable_quick_doze";
    private static final String KEY_FORCE_ALL_APPS_STANDBY = "force_all_apps_standby";
    private static final String KEY_FORCE_BACKGROUND_CHECK = "force_background_check";
    private static final String KEY_DEFER_FULL_BACKUP = "defer_full_backup";
    private static final String KEY_DEFER_KEYVALUE_BACKUP = "defer_keyvalue_backup";
    private static final String KEY_ENABLE_DATASAVER = "enable_datasaver";
    private static final String KEY_DISABLE_VIBRATION = "disable_vibration";
    private static final String KEY_DISABLE_OPTIONAL_SENSORS = "disable_optional_sensors";
    private static final String KEY_LOCATION_MODE = "location_mode";

    private CustomSeekBarPreference mBrightnessFactor;
    private SwitchPreferenceCompat mEnableBrightness;

    private static final Map<String, String> sDefaultsMap = new HashMap<>();
    static {
        sDefaultsMap.put(KEY_ENABLE_BRIGHTNESS_ADJUSTMENT, "false");
        sDefaultsMap.put(KEY_ADJUST_BRIGHTNESS_FACTOR, "0.5");
        sDefaultsMap.put(KEY_DISABLE_AOD, "true");
        sDefaultsMap.put(KEY_ENABLE_NIGHT_MODE, "true");
        sDefaultsMap.put(KEY_DISABLE_ANIMATION, "false");
        sDefaultsMap.put(KEY_DISABLE_LAUNCH_BOOST, "true");
        sDefaultsMap.put(KEY_ENABLE_QUICK_DOZE, "true");
        sDefaultsMap.put(KEY_FORCE_ALL_APPS_STANDBY, "true");
        sDefaultsMap.put(KEY_FORCE_BACKGROUND_CHECK, "true");
        sDefaultsMap.put(KEY_DEFER_FULL_BACKUP, "true");
        sDefaultsMap.put(KEY_DEFER_KEYVALUE_BACKUP, "true");
        sDefaultsMap.put(KEY_ENABLE_DATASAVER, "false");
        sDefaultsMap.put(KEY_DISABLE_VIBRATION, "false");
        sDefaultsMap.put(KEY_DISABLE_OPTIONAL_SENSORS, "true");
        sDefaultsMap.put(KEY_LOCATION_MODE, "3");
    }

    public BatterySaverAdvancedSettingsPreferenceController(Context context) {
        super(context);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);

        for (String key : sDefaultsMap.keySet()) {
            final Preference pref = screen.findPreference(key);
            if (pref == null) continue;
            final String val = getValueForKey(key);
            if (pref instanceof SwitchPreferenceCompat) {
                ((SwitchPreferenceCompat) pref).setChecked(Boolean.valueOf(val));
            } else if (pref instanceof CustomSeekBarPreference) {
                ((CustomSeekBarPreference) pref).setValue(Math.round(Float.valueOf(val) * 100f));
            } else if (pref instanceof ListPreference) {
                ((ListPreference) pref).setValueIndex(Integer.valueOf(val));
            }
            pref.setOnPreferenceChangeListener(this);
        }

        mBrightnessFactor = screen.findPreference(KEY_ADJUST_BRIGHTNESS_FACTOR);
        mEnableBrightness = screen.findPreference(KEY_ENABLE_BRIGHTNESS_ADJUSTMENT);
        mBrightnessFactor.setEnabled(mEnableBrightness.isChecked());
    }

    private String getValueForKey(String key) {
        final String setting = Settings.Global.getString(mContext.getContentResolver(),
                Settings.Global.BATTERY_SAVER_CONSTANTS);
        final String def = sDefaultsMap.get(key);
        if (setting == null) return def;
        final String[] pairs = setting.split(",");
        for (String str : pairs) {
            final String[] pair = str.split("=");
            if (pair[0].equals(key))
                return pair[1];
        }
        return def;
    }

    private void setValueForKey(String key, String value) {
        final String setting = Settings.Global.getString(mContext.getContentResolver(),
                Settings.Global.BATTERY_SAVER_CONSTANTS);
        final String def = sDefaultsMap.get(key);
        final boolean isDefault = value.equals(def);
        final List<String> pairsList = new ArrayList<>();
        if (setting == null) {
            if (!isDefault)
                pairsList.add(key + "=" + value);
            saveSetting(pairsList);
            return;
        }
        final String[] pairs = setting.split(",");
        boolean found = false;
        for (String str : pairs) {
            final String[] pair = str.split("=");
            if (!found && key.equals(pair[0])) {
                found = true;
                if (!isDefault)
                    pairsList.add(key + "=" + value);
                continue;
            }
            pairsList.add(str);
        }
        if (!isDefault && !found)
            pairsList.add(key + "=" + value);
        saveSetting(pairsList);
    }

    private void saveSetting(List<String> pairs) {
        if (pairs == null || pairs.size() == 0) {
            Settings.Global.putString(mContext.getContentResolver(),
                    Settings.Global.BATTERY_SAVER_CONSTANTS, null);
            return;
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(pairs.get(0));
        for (int i = 1; i < pairs.size(); i++) {
            sb.append("," + pairs.get(i));
        }
        Settings.Global.putString(mContext.getContentResolver(),
                    Settings.Global.BATTERY_SAVER_CONSTANTS, sb.toString());
    }

    @Override
    public String getPreferenceKey() {
        return null;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object newValue) {
        final String key = pref.getKey();
        if (KEY_ENABLE_BRIGHTNESS_ADJUSTMENT.equals(key)) {
            mBrightnessFactor.setEnabled((Boolean) newValue);
        }
        if (pref instanceof SwitchPreferenceCompat) {
            setValueForKey(key, ((Boolean) newValue).toString());
        } else if (pref == mBrightnessFactor) {
            setValueForKey(key, String.valueOf(((Integer) newValue) / 100f));
        } else if (KEY_LOCATION_MODE.equals(key)) {
            setValueForKey(key, (String) newValue);
        }
        return true;
    }
}
