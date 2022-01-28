/*
 * Copyright (C) 2016 The Android Open Source Project
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

import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.settings.core.TogglePreferenceController;
import com.android.settings.R;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

public class DoubleTapPowerPreferenceController extends TogglePreferenceController
        implements LifecycleObserver, OnStart, OnStop {

    @VisibleForTesting
    static final int ON = 0;
    @VisibleForTesting
    static final int OFF = 1;

    private static final String PREF_KEY = "gesture_double_tap_power";
    private final String SECURE_KEY = CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED;

    private SwitchPreference mPreference;

    private final ContentObserver mTorchObserver = new ContentObserver(Handler.getMain()) {
        @Override
        public void onChange(boolean selfChange) {
            if (mPreference != null)
                updateState(mPreference);
        }
    };

    public DoubleTapPowerPreferenceController(Context context, String key) {
        super(context, key);
    }

    public static boolean isSuggestionComplete(Context context, SharedPreferences prefs) {
        return !isGestureAvailable(context)
                || prefs.getBoolean(PowerMenuSettings.PREF_KEY_SUGGESTION_COMPLETE, false);
    }

    private static boolean isGestureAvailable(Context context) {
        return context.getResources()
                .getBoolean(com.android.internal.R.bool.config_cameraDoubleTapPowerGestureEnabled);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(PREF_KEY);
    }

    @Override
    public void onStart() {
        mContext.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(TORCH_POWER_BUTTON_GESTURE),
                false, mTorchObserver);
    }

    @Override
    public void onStop() {
        mContext.getContentResolver().unregisterContentObserver(mTorchObserver);
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setEnabled(getAvailabilityStatus() != DISABLED_DEPENDENT_SETTING);
    }

    @Override
    public int getAvailabilityStatus() {
        if (!isGestureAvailable(mContext)) return UNSUPPORTED_ON_DEVICE;
        final int value = Settings.System.getInt(
                mContext.getContentResolver(), TORCH_POWER_BUTTON_GESTURE, 0);
        if (value == 1) return DISABLED_DEPENDENT_SETTING;
        return AVAILABLE;
    }

    @Override
    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), "gesture_double_tap_power");
    }

    @Override
    public boolean isPublicSlice() {
        return true;
    }

    @Override
    public boolean isChecked() {
        final int cameraDisabled = Settings.Secure.getInt(mContext.getContentResolver(),
                SECURE_KEY, ON);
        return cameraDisabled == ON;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        return Settings.Secure.putInt(mContext.getContentResolver(), SECURE_KEY,
                isChecked ? ON : OFF);
    }
}
