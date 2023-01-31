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

package com.android.settings.network;

import static android.provider.Settings.Global.CAPTIVE_PORTAL_MODE_IGNORE;
import static android.provider.Settings.Global.CAPTIVE_PORTAL_MODE_PROMPT;

import static com.android.settings.core.BasePreferenceController.AVAILABLE;
import static com.android.settings.core.BasePreferenceController.DISABLED_FOR_USER;

import android.content.ContentResolver;
import android.content.Context;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.RestrictedLockUtilsInternal;

import java.util.ArrayList;
import java.util.List;

public class ConnectivityCheckPreferenceController extends BasePreferenceController
        implements PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String KEY = "connectivity_check_settings";

    private static final int HTTPS_INDEX = 0;
    private static final int HTTP_INDEX = 1;
    private static final int FALLBACK_INDEX = 2;
    private static final int OTHER_FALLBACK_INDEX = 3;

    // imported defaults from AOSP NetworkStack
    private static final String[] DEFAULT_PORTAL = {
        "https://www.google.com/generate_204",
        "http://connectivitycheck.gstatic.com/generate_204",
        "http://www.google.com/gen_204",
        "http://play.googleapis.com/generate_204"
    };

    private static final String[] GRAPHENEOS_PORTAL = {
        "https://connectivitycheck.grapheneos.network/generate_204",
        "http://connectivitycheck.grapheneos.network/generate_204",
        "http://grapheneos.online/gen_204",
        "http://grapheneos.online/generate_204"
    };

    // 204 servers for chinese users
    private static final String[] CHINA_PORTAL = {
        "https://204.ustclug.org",
        "http://204.ustclug.org",
        "http://connect.rom.miui.com/generate_204",
        "http://wifi.vivo.com.cn/generate_204"
    };

    // These values should match the preference's entry values by index
    private static final ArrayList<String[]> PORTAL_BY_INDEX = new ArrayList<>(List.of(
        GRAPHENEOS_PORTAL,
        DEFAULT_PORTAL,
        CHINA_PORTAL
    ));

    private ListPreference mPreference;

    public ConnectivityCheckPreferenceController(Context context) {
        super(context, KEY);
    }

    @Override
    public int getAvailabilityStatus() {
        return isDisabledByAdmin() ? DISABLED_FOR_USER : AVAILABLE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mPreference = (ListPreference) screen.findPreference(KEY);
        if (getAvailabilityStatus() == AVAILABLE && mPreference != null) {
            updatePreferenceState();
            return;
        }
        mPreference.setVisible(false);
    }

    @Override
    public String getPreferenceKey() {
        return KEY;
    }

    private void updatePreferenceState() {
        final boolean disabled = Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.CAPTIVE_PORTAL_MODE, CAPTIVE_PORTAL_MODE_PROMPT)
                == CAPTIVE_PORTAL_MODE_IGNORE;
        if (disabled) {
            mPreference.setValueIndex(PORTAL_BY_INDEX.size());
            return;
        }

        final String value = Settings.Global.getString(mContext.getContentResolver(),
                Settings.Global.CAPTIVE_PORTAL_HTTP_URL);
        int index = -1;
        for (int i = 0; i < PORTAL_BY_INDEX.size(); i++) {
            if (PORTAL_BY_INDEX.get(i)[HTTP_INDEX].equals(value)) {
                index = i;
                break;
            }
        }
        mPreference.setValueIndex(index != -1 ? index : PORTAL_BY_INDEX.size());
    }

    private void setCaptivePortalURLs(int mode) {
        final ContentResolver resolver = mContext.getContentResolver();

        final boolean enabled = mode < PORTAL_BY_INDEX.size();
        final String[] portal = enabled ? PORTAL_BY_INDEX.get(mode) : null;
        String https = DEFAULT_PORTAL[HTTPS_INDEX];
        String http = DEFAULT_PORTAL[HTTP_INDEX];
        String fallback = DEFAULT_PORTAL[FALLBACK_INDEX];
        String anotherFallback = DEFAULT_PORTAL[OTHER_FALLBACK_INDEX];
        if (enabled) {
            https = portal[HTTPS_INDEX];
            http = portal[HTTP_INDEX];
            fallback = portal[FALLBACK_INDEX];
            anotherFallback = portal[OTHER_FALLBACK_INDEX];
        }

        Settings.Global.putString(resolver,
                Settings.Global.CAPTIVE_PORTAL_HTTP_URL, http);
        Settings.Global.putString(resolver,
                Settings.Global.CAPTIVE_PORTAL_HTTPS_URL, https);
        Settings.Global.putString(resolver,
                Settings.Global.CAPTIVE_PORTAL_FALLBACK_URL, fallback);
        Settings.Global.putString(resolver,
                Settings.Global.CAPTIVE_PORTAL_OTHER_FALLBACK_URLS, anotherFallback);
        Settings.Global.putInt(resolver, Settings.Global.CAPTIVE_PORTAL_MODE,
                enabled ? CAPTIVE_PORTAL_MODE_PROMPT : CAPTIVE_PORTAL_MODE_IGNORE);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        if (preference == mPreference) {
            setCaptivePortalURLs(Integer.parseInt((String)value));
            return true;
        }
        return false;
    }

    private boolean isDisabledByAdmin() {
        return RestrictedLockUtilsInternal.checkIfRestrictionEnforced(
                mContext, UserManager.DISALLOW_CONFIG_PRIVATE_DNS,
                UserHandle.myUserId()) != null;
    }
}
