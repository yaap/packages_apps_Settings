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
package com.android.settings.display

import android.content.Context
import android.provider.Settings
import com.android.settings.R
import com.android.settings.Utils
import com.android.settingslib.datastore.KeyValueStore
import com.android.settingslib.datastore.KeyValueStoreDelegate
import com.android.settingslib.datastore.SettingsSystemStore
import com.android.settingslib.metadata.PreferenceAvailabilityProvider
import com.android.settingslib.metadata.preferencesapi.preconditions.PreconditionStability
import com.android.settingslib.metadata.ReadWritePermit
import com.android.settingslib.metadata.SensitivityLevel
import com.android.settingslib.metadata.SwitchPreference

// LINT.IfChange
class BatteryTextOnlySwitchPreference :
    SwitchPreference(
        KEY,
        R.string.battery_text_only_purpose,
        R.string.battery_text_only_title,
        R.string.battery_text_only_summary,
    ),
    PreferenceAvailabilityProvider {

    override fun storage(context: Context): KeyValueStore =
        BatteryTextOnlyStorage(SettingsSystemStore.get(context))

    override fun isAvailable(context: Context): Boolean = Utils.isBatteryPresent(context)

    override val availabilityDescription = "The device must have a battery present."

    override fun getAvailabilityStability() = PreconditionStability.UNSTABLE

    override fun getReadPermissions(context: Context) = SettingsSystemStore.getReadPermissions()

    override fun getReadPermit(context: Context, callingPid: Int, callingUid: Int) =
        ReadWritePermit.ALLOW

    override fun getWritePermissions(context: Context) = SettingsSystemStore.getWritePermissions()

    override fun getWritePermit(
        context: Context,
        value: Boolean?,
        callingPid: Int,
        callingUid: Int,
    ) = ReadWritePermit.ALLOW

    override val sensitivityLevel
        get() = SensitivityLevel.NO_SENSITIVITY

    @Suppress("UNCHECKED_CAST")
    private class BatteryTextOnlyStorage(
        private val settingsStore: KeyValueStore,
    ) : KeyValueStoreDelegate {

        override val keyValueStoreDelegate
            get() = settingsStore

        override fun <T : Any> getDefaultValue(key: String, valueType: Class<T>) = false as T
    }

    companion object {
        const val KEY = Settings.System.BATTERY_TEXT_ONLY
    }
}
// LINT.ThenChange(BatteryTextOnlyPreferenceController.java)
