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

package com.android.settings.spa.app.appinfo

import android.content.Context
import android.content.pm.ApplicationInfo
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.settings.R
import com.android.settingslib.spa.framework.compose.OverridableFlow
import com.android.settingslib.spa.widget.preference.SwitchPreference
import com.android.settingslib.spa.widget.preference.SwitchPreferenceModel
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

@Composable
fun GameMacroSwitchPreference(
    app: ApplicationInfo
) {
    val context = LocalContext.current
    val presenter = remember(app) { GameMacroSwitchPresenter(context, app) }

    val isSwitchEnabledStateFlow = MutableStateFlow(false)
    val isEligibleState by presenter.isEligibleFlow.collectAsStateWithLifecycle(initialValue = false)
    val isCheckedState = presenter.isCheckedFlow.collectAsStateWithLifecycle(initialValue = null)
    SwitchPreference(remember {
        object : SwitchPreferenceModel {
            override val title = context.getString(R.string.gaming_mode_tile_title)
            override val summary = { context.getString(R.string.gaming_macro_switch_summary) }
            override val changeable = { isEligibleState }
            override val checked = {
                val result = if (changeable()) isCheckedState.value else false
                result.also { isChecked ->
                    isChecked?.let {
                        isSwitchEnabledStateFlow.value = it
                    }
                }
            }
            override val onCheckedChange = presenter::onCheckedChange
        }
    })
}

private class GameMacroSwitchPresenter(private val context: Context, private val app: ApplicationInfo) {
    private val appName = app.packageName

    val isEligibleFlow = flow {
        if (app.isArchived) {
            emit(false)
            return@flow
        }
        emit(true)
    }

    private val isChecked = OverridableFlow(flow {
        val value = Settings.System.getString(context.getContentResolver(),
            Settings.System.GAMING_MODE_APPS)
        emit(appName != null && !value.isNullOrEmpty() && value.contains(appName))
    })

    val isCheckedFlow = isChecked.flow

    fun onCheckedChange(newChecked: Boolean) {
        if (appName == null) {
            return
        }
        var value = Settings.System.getString(context.getContentResolver(),
            Settings.System.GAMING_MODE_APPS)

        // the setting is empty
        // deal with checked & !checked
        if (value.isNullOrEmpty()) {
            if (newChecked) {
                Settings.System.putString(context.getContentResolver(),
                    Settings.System.GAMING_MODE_APPS, appName)
            }
            isChecked.override(newChecked)
            return
        }

        // the setting is not empty
        // deal with checked
        if (newChecked) {
            if (!value.contains(appName)) {
                value = value + "," + appName
                Settings.System.putString(context.getContentResolver(),
                    Settings.System.GAMING_MODE_APPS, value)
            }
            isChecked.override(newChecked)
            return
        }

        // deal with unchecked
        if (!value.contains(appName)) {
            isChecked.override(newChecked)
            return
        }
        val apps = value.split(",")
        var saveVal = ""
        var first = true
        for (app in apps) {
            if (app.equals(appName)) {
                // do not add the disabled app
                continue
            }

            if (first) {
                saveVal = saveVal + app
                first = false
                continue
            }

            saveVal = saveVal + "," + app
        }

        Settings.System.putString(context.getContentResolver(),
            Settings.System.GAMING_MODE_APPS, saveVal)
        isChecked.override(newChecked)
    }

}
