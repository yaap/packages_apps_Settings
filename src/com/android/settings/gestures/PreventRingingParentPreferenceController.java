/*
 * Copyright (C) 2018 The Android Open Source Project
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

import static android.provider.Settings.Secure.VOLUME_HUSH_GESTURE;
import static android.provider.Settings.Secure.YAAP_VOLUME_HUSH_MUTE;
import static android.provider.Settings.Secure.YAAP_VOLUME_HUSH_NORMAL;
import static android.provider.Settings.Secure.YAAP_VOLUME_HUSH_OFF;
import static android.provider.Settings.Secure.YAAP_VOLUME_HUSH_VIBRATE;

import android.content.Context;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class PreventRingingParentPreferenceController extends BasePreferenceController {

    final String SECURE_KEY = VOLUME_HUSH_GESTURE;

    public PreventRingingParentPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_volumeHushGestureEnabled)
                ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public CharSequence getSummary() {
        String settingsValue = Settings.Secure.getString(
                mContext.getContentResolver(), SECURE_KEY);
        if (settingsValue == null) settingsValue = YAAP_VOLUME_HUSH_OFF;
        String[] value = settingsValue.split(",", 0);

        if (value[0].equals(YAAP_VOLUME_HUSH_OFF))
            return mContext.getText(R.string.prevent_ringing_option_none_summary);

        String summary = null;
        for (String str : value) {
            if (summary == null) {
                summary = mContext.getText(R.string.switch_on_text)
                        + " (" + getStringForMode(str);
                continue;
            }
            summary += ", " + getStringForMode(str);
        }
        summary += ")";
        return summary;
    }

    private String getStringForMode(String mode) {
        switch (mode) {
            case YAAP_VOLUME_HUSH_VIBRATE:
                return mContext.getText(R.string.prevent_ringing_option_vibrate).toString();
            case YAAP_VOLUME_HUSH_MUTE:
                return mContext.getText(R.string.prevent_ringing_option_mute).toString();
        }
        return mContext.getText(R.string.prevent_ringing_option_normal).toString();
    }
}
