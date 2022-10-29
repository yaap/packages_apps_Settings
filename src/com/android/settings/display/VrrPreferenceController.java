/*
 * Copyright (C) 2020 The Android Open Source Project
 *               2022 Yet Another AOSP Project
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
import android.hardware.display.DisplayManager;
import android.provider.DeviceConfig;
import android.sysprop.SurfaceFlingerProperties;

import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;

public class VrrPreferenceController extends TogglePreferenceController {
    private static final int DISABLE_VALUE = Integer.MAX_VALUE;
    private static final int DISABLE_VALUE_AMBIENT = -1;
    private static final String CONFIG_NAMESPACE = DeviceConfig.NAMESPACE_DISPLAY_MANAGER;
    private static final String CONFIG_KEY =
            DisplayManager.DeviceConfig.KEY_FIXED_REFRESH_RATE_LOW_DISPLAY_BRIGHTNESS_THRESHOLDS;
    private static final String CONFIG_KEY_AMBIENT =
            DisplayManager.DeviceConfig.KEY_FIXED_REFRESH_RATE_LOW_AMBIENT_BRIGHTNESS_THRESHOLDS;

    public VrrPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
    }

    @Override
    public int getAvailabilityStatus() {
        boolean hasVRR = false;
        try {
            hasVRR = SurfaceFlingerProperties.use_content_detection_for_refresh_rate().orElseThrow();
        } catch (Exception ignored) { }
        return hasVRR ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public boolean isChecked() {
        int[] arr = getIntArrayProperty(CONFIG_KEY);
        final boolean disabled =
                arr != null &&
                arr.length == 1 &&
                arr[0] == DISABLE_VALUE;

        arr = getIntArrayProperty(CONFIG_KEY_AMBIENT);
        final boolean disabledAmbient =
                arr != null &&
                arr.length == 1 &&
                arr[0] == DISABLE_VALUE_AMBIENT;

        return disabled && disabledAmbient;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        setIntArrayProperty(isChecked);
        return true;
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return R.string.menu_key_display;
    }

    private int[] getIntArrayProperty(String key) {
        String strArray = DeviceConfig.getString(CONFIG_NAMESPACE, key, null);
        if (strArray != null) return parseIntArray(strArray);
        return null;
    }

    private int[] parseIntArray(String strArray) {
        String[] items = strArray.split(",");
        int[] array = new int[items.length];

        try {
            for (int i = 0; i < array.length; i++)
                array[i] = Integer.parseInt(items[i]);
        } catch (NumberFormatException e) {
            array = null;
        }

        return array;
    }

    private void setIntArrayProperty(boolean disableVRR) {
        if (disableVRR) {
            DeviceConfig.setProperty(
                CONFIG_NAMESPACE,
                CONFIG_KEY,
                String.valueOf(DISABLE_VALUE),
                false /* make default */
            );
            DeviceConfig.setProperty(
                CONFIG_NAMESPACE,
                CONFIG_KEY_AMBIENT,
                String.valueOf(DISABLE_VALUE_AMBIENT),
                false /* make default */
            );
            return;
        }
        DeviceConfig.deleteProperty(CONFIG_NAMESPACE, CONFIG_KEY);
        DeviceConfig.deleteProperty(CONFIG_NAMESPACE, CONFIG_KEY_AMBIENT);
    }
}
