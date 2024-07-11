/*
 * Copyright (C) 2024 Yet Another AOSP Project
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
package com.android.settings.deviceinfo.firmwareversion;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.ApplicationInfoFlags;
import android.content.pm.PackageManager.PackageInfoFlags;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class YAAPChangelogPreferenceController extends BasePreferenceController {

    public YAAPChangelogPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        String updaterPackage = mContext.getString(R.string.update_package);
        if (updaterPackage == null || updaterPackage.isEmpty())
                return UNSUPPORTED_ON_DEVICE;
        try {
            PackageManager pm = mContext.getPackageManager();
            if (!pm.getApplicationInfo(updaterPackage,
                    ApplicationInfoFlags.of(PackageManager.MATCH_SYSTEM_ONLY)).enabled)
                return UNSUPPORTED_ON_DEVICE;

            String activityStr = mContext.getString(R.string.changelog_activity);
            if (activityStr == null || activityStr.isEmpty())
                return UNSUPPORTED_ON_DEVICE;

            String activity = activityStr.substring(activityStr.lastIndexOf('.'));
            ActivityInfo[] infos = pm.getPackageInfo(updaterPackage,
                    PackageInfoFlags.of(PackageManager.GET_ACTIVITIES)).activities;
            if (infos == null)
                return UNSUPPORTED_ON_DEVICE;

            for (ActivityInfo info : infos) {
                if (!info.enabled)
                    continue;
                if (info.name.endsWith(activity))
                    return AVAILABLE;
            }
        } catch (PackageManager.NameNotFoundException ignored) { }

        return UNSUPPORTED_ON_DEVICE;
    }
}
