/*
 * Copyright (C) 2025 The YAAP Project
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

package com.android.settings.network;

import android.app.AlertDialog;
import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;

public class EsimPowerPreferenceController extends TogglePreferenceController
        implements LifecycleObserver {
    private static final String TAG = "EsimPowerPrefController";
    private static final String SETTING_ESIM_POWER_DISABLED = "esim_power_disabled";
    private static final String PROP_ESIM_DISABLED = "debug.disable_esim";

    private SwitchPreferenceCompat mPreference;

    public EsimPowerPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
    }

    @Override
    public int getAvailabilityStatus() {
        return mContext.getResources().getBoolean(R.bool.config_supportEsimPowerControl)
                ? AVAILABLE : CONDITIONALLY_UNAVAILABLE;
    }

    @Override
    public boolean isChecked() {
        return Settings.Global.getInt(mContext.getContentResolver(),
                SETTING_ESIM_POWER_DISABLED, 0) == 1;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        if (isChecked) {
            showDisableConfirmationDialog();
            return false;
        } else {
            showEnableConfirmationDialog();
            return false;
        }
    }

    private void showDisableConfirmationDialog() {
        new AlertDialog.Builder(mContext)
                .setMessage(R.string.esim_power_disable_dialog_message)
                .setNegativeButton(R.string.esim_power_dialog_cancel,
                        (dialog, which) -> {
                            dialog.dismiss();
                            if (mPreference != null) {
                                mPreference.setChecked(false);
                            }
                        })
                .setPositiveButton(R.string.esim_power_dialog_disable,
                        (dialog, which) -> {
                            dialog.dismiss();
                            if (performToggle(true)) {
                                if (mPreference != null) {
                                    mPreference.setChecked(true);
                                }
                            }
                        })
                .show();
    }

    private void showEnableConfirmationDialog() {
        new AlertDialog.Builder(mContext)
                .setMessage(R.string.esim_power_enable_dialog_message)
                .setNegativeButton(R.string.esim_power_dialog_cancel,
                        (dialog, which) -> {
                            dialog.dismiss();
                            if (mPreference != null) {
                                mPreference.setChecked(true);
                            }
                        })
                .setPositiveButton(R.string.esim_power_dialog_enable,
                        (dialog, which) -> {
                            dialog.dismiss();
                            if (performToggle(false)) {
                                if (mPreference != null) {
                                    mPreference.setChecked(false);
                                }
                            }
                        })
                .show();
    }

    private boolean performToggle(boolean disable) {
        try {
            Settings.Global.putInt(mContext.getContentResolver(),
                    SETTING_ESIM_POWER_DISABLED, disable ? 1 : 0);
            SystemProperties.set(PROP_ESIM_DISABLED, disable ? "1" : "0");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to toggle eSIM power", e);
            return false;
        }
    }

    @Override
    public int getSliceHighlightMenuRes() {
        return 0;
    }
}
