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

package com.android.settings.display.darkmode

import android.content.Context
import com.android.settings.R
import com.android.settings.accessibility.TooltipSliderPreference
import com.android.settingslib.datastore.KeyValueStore
import com.android.settingslib.datastore.SettingsSecureStore
import com.android.settingslib.metadata.IntRangeValuePreference
import com.android.settingslib.widget.SliderPreferenceBinding

class DimWallpaperAmountPreference :
    IntRangeValuePreference,
    SliderPreferenceBinding {

    override val key = KEY

    override val purpose = R.string.ui_night_mode_dim_wall_amount_title

    override val title = R.string.ui_night_mode_dim_wall_amount_title

    override val supportsWrite = true

    override fun storage(context: Context): KeyValueStore =
        SettingsSecureStore.get(context).also {
            it.setDefaultValue(key, DEF_VALUE)
        }

    override fun isEnabled(context: Context): Boolean =
        SettingsSecureStore.get(context).getValue(
            DimWallpaperPreference.KEY, Boolean::class.javaObjectType,
        ) ?: false

    override fun getMinValue(context: Context) = MIN_VALUE

    override fun getMaxValue(context: Context) = MAX_VALUE

    override fun getIncrementStep(context: Context) = STEP

    override fun dependencies(context: Context) =
        arrayOf(DimWallpaperPreference.KEY)

    override fun createWidget(context: Context) =
        TooltipSliderPreference(context).apply {
            setIconStart(R.drawable.ic_remove_24dp)
            setIconEnd(R.drawable.ic_add_24dp)
            setTickVisible(true)
            setShowSliderValue(true)
            setDefaultValue(DEF_VALUE)
            setMin(MIN_VALUE)
            setMax(MAX_VALUE)
            setSliderIncrement(STEP)
        }

    companion object {
        const val KEY = "ui_night_mode_dim_wall_amount"
        const val DEF_VALUE = 40
        const val MIN_VALUE = 10
        const val MAX_VALUE = 90
        const val STEP = 5
    }
}
