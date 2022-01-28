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

package com.android.settings.gestures;

import static android.provider.Settings.Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED;
import static android.provider.Settings.System.TORCH_POWER_BUTTON_GESTURE;
import static com.android.settings.gestures.PowerMenuSettingsUtils.LONG_PRESS_POWER_ASSISTANT_VALUE;
import static com.android.settings.gestures.PowerMenuSettingsUtils.LONG_PRESS_POWER_GLOBAL_ACTIONS;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

import java.util.ArrayList;

public class PowerMenuPreferenceController extends BasePreferenceController {

    public PowerMenuPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public CharSequence getSummary() {
        final ContentResolver resolver = mContext.getContentResolver();
        // default value for summary if nothing is enabled
        String summary = mContext.getString(R.string.power_menu_setting_summary);
        ArrayList<String> enabledStrings = new ArrayList<>();
        int value = Settings.System.getInt(resolver, TORCH_POWER_BUTTON_GESTURE, 0);
        if (value != 0) enabledStrings.add(mContext.getString(R.string.torch_power_button_gesture_title));
        value = Settings.Secure.getInt(resolver, CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED, 0);
        if (value == 0) enabledStrings.add(mContext.getString(R.string.double_tap_power_for_camera_title));
        value = PowerMenuSettingsUtils.getPowerButtonSettingValue(mContext);
        if (value == LONG_PRESS_POWER_ASSISTANT_VALUE)
            enabledStrings.add(mContext.getString(R.string.power_menu_long_press_for_assist));
        if (!enabledStrings.isEmpty()) {
            summary = enabledStrings.remove(0);
            for (String str : enabledStrings) summary += (", " + str);
        }
        return summary;
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }
}
