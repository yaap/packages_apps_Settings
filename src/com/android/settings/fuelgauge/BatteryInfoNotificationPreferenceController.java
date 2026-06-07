/*
 * Copyright (C) 2026 VoltageOS
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

package com.android.settings.fuelgauge;

import android.content.Context;
import android.provider.Settings;

import com.android.settings.core.TogglePreferenceController;

public class BatteryInfoNotificationPreferenceController extends TogglePreferenceController {

    public BatteryInfoNotificationPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean isChecked() {
        return Settings.System.getInt(
                mContext.getContentResolver(),
                Settings.System.BATTERY_INFO_NOTIFICATION, 0) == 1;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return Settings.System.putInt(
                mContext.getContentResolver(),
                Settings.System.BATTERY_INFO_NOTIFICATION, isChecked ? 1 : 0);
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return NO_RES;
    }
}
