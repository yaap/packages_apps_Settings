/*
 * Copyright (C) 2026 Yet Another AOSP Project
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

package com.android.settings.display.ambient

import android.content.Context
import android.hardware.display.AmbientDisplayConfiguration
import android.os.UserHandle
import android.provider.Settings
import androidx.preference.Preference
import com.android.internal.util.yaap.AutoSettingConsts.MODE_DISABLED
import com.android.internal.util.yaap.AutoSettingConsts.MODE_NIGHT
import com.android.internal.util.yaap.AutoSettingConsts.MODE_TIME
import com.android.internal.util.yaap.AutoSettingConsts.MODE_MIXED_SUNSET
import com.android.internal.util.yaap.AutoSettingConsts.MODE_MIXED_SUNRISE
import com.android.settings.R
import com.android.settings.display.AODSchedule
import com.android.settingslib.metadata.PreferenceAvailabilityProvider
import com.android.settingslib.metadata.preferencesapi.preconditions.PreconditionStability
import com.android.settingslib.metadata.PreferenceLifecycleContext
import com.android.settingslib.metadata.PreferenceLifecycleProvider
import com.android.settingslib.metadata.PreferenceMetadata
import com.android.settingslib.metadata.PreferenceSummaryProvider
import com.android.settingslib.metadata.PreferenceTitleProvider
import com.android.settingslib.preference.PreferenceBinding
import com.android.settingslib.preference.PreferenceBindingPlaceholder

class AmbientDisplaySchedulePreference(
    private val context: Context
) :
    PreferenceMetadata,
    PreferenceBinding,
    PreferenceBindingPlaceholder,
    PreferenceLifecycleProvider,
    PreferenceTitleProvider,
    PreferenceSummaryProvider,
    PreferenceAvailabilityProvider {

    private val config: AmbientDisplayConfiguration by lazy {
        AmbientDisplayConfiguration(context)
    }

    override val key: String
        get() = "always_on_display_schedule"

    override val purpose: Int
        get() = R.string.always_on_display_schedule_purpose

    override val availabilityDescription =
        "The device must support always-on display for the current user."

    override fun getAvailabilityStability() = PreconditionStability.UNSTABLE

    override fun isAvailable(context: Context): Boolean =
        config.alwaysOnAvailableForUser(UserHandle.myUserId())

    override fun getTitle(context: Context): CharSequence? =
        context.getString(R.string.always_on_display_schedule_title)

    override fun getSummary(context: Context): CharSequence? {
        val mode = Settings.Secure.getIntForUser(
            context.getContentResolver(),
            Settings.Secure.DOZE_ALWAYS_ON_AUTO_MODE,
            0,
            UserHandle.USER_CURRENT
        )

        val resID = when (mode) {
            MODE_NIGHT -> R.string.night_display_auto_mode_twilight
            MODE_TIME -> R.string.night_display_auto_mode_custom
            MODE_MIXED_SUNSET -> R.string.always_on_display_schedule_mixed_sunset
            MODE_MIXED_SUNRISE -> R.string.always_on_display_schedule_mixed_sunrise
            else -> R.string.disabled // MODE_DISABLED
        }

        return context.getText(resID)
    }

    override fun bind(preference: Preference, metadata: PreferenceMetadata) {
        super.bind(preference, metadata)
        preference.isCopyingEnabled = false
        preference.fragment = AODSchedule::class.java.name
    }

    override fun onResume(context: PreferenceLifecycleContext) {
        context.notifyPreferenceChange(key)
    }
}
