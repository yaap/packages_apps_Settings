/*
 * Copyright (C) 2022 Project Kaleidoscope
 * Copyright (C) 2022 AOSP-Krypton Project
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

package ink.kscope.settings.wifi.tether;

import static android.net.wifi.SoftApConfiguration.DEFAULT_TIMEOUT;

import android.content.Context;
import android.net.wifi.SoftApConfiguration;
import android.net.wifi.WifiManager;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.core.BasePreferenceController;

public class WifiTetherAutoOffPreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private final WifiManager mWifiManager;

    private ListPreference mPreference;

    public WifiTetherAutoOffPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        mWifiManager = context.getSystemService(WifiManager.class);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = screen.findPreference(getPreferenceKey());
        updateDisplay();
    }

    private long getAutoOffTimeout() {
        final SoftApConfiguration softApConfiguration = mWifiManager.getSoftApConfiguration();
        boolean settingsOn = softApConfiguration.isAutoShutdownEnabled();
        final long timeout = softApConfiguration.getShutdownTimeoutMillis();
        if (timeout == 0 || timeout / 1000 == 0) settingsOn = false;
        return settingsOn ? (timeout / 1000) : DEFAULT_TIMEOUT;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        long timeout;
        try {
            timeout = Long.parseLong((String) newValue);
        } catch (NumberFormatException e) {
            return false;
        }
        final boolean defTimeout = timeout == 0;
        if (defTimeout) timeout = DEFAULT_TIMEOUT;
        else timeout *= 1000;
        final SoftApConfiguration softApConfiguration = mWifiManager.getSoftApConfiguration();
        final SoftApConfiguration newSoftApConfiguration =
                new SoftApConfiguration.Builder(softApConfiguration)
                        .setAutoShutdownEnabled(!defTimeout)
                        .setShutdownTimeoutMillis(timeout)
                        .build();
        return mWifiManager.setSoftApConfiguration(newSoftApConfiguration);
    }

    public void updateConfig(SoftApConfiguration.Builder builder) {
        if (builder == null) return;
        final long timeout = getAutoOffTimeout();
        builder.setAutoShutdownEnabled(timeout != DEFAULT_TIMEOUT)
                .setShutdownTimeoutMillis(timeout);
    }

    public void updateDisplay() {
        if (mPreference != null) {
            final long timeout = getAutoOffTimeout();
            mPreference.setValue(String.valueOf(
                    timeout == DEFAULT_TIMEOUT ? 0 : timeout));
        }
    }
}
