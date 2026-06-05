/*
 * Copyright (C) 2026 VoltageOS
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

package com.android.settings.fuelgauge.batteryusage;

import android.content.SharedPreferences;
import android.app.AlertDialog;
import android.app.settings.SettingsEnums;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AggregateBatteryConsumer;
import android.os.BatteryConsumer;
import android.os.BatteryStatsManager;
import android.os.BatterySummaryStats;
import android.os.BatteryUsageStats;
import android.os.SuspendStats;
import android.os.UidAlarmStats;
import android.os.UidWakelockStats;
import android.os.WakeupSourceStats;
import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.fuelgauge.BatteryUtils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.search.SearchIndexable;
import com.android.settingslib.utils.ThreadUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
 
/** Detailed since-last-charge battery, wakeup, and suspend stats. */
@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class DetailedBatteryStats extends DashboardFragment {
    private static final String TAG = "DetailedBatteryStats";
    private static final int MENU_RESET_STATS = 1;
    private static final String SESSION_PREFS = "detailed_battery_session";
    private static final String PREF_SCREEN_ON_MS       = "prev_screen_on_ms";
    private static final String PREF_SCREEN_ON_PCT      = "prev_screen_on_pct";
    private static final String PREF_SCREEN_ON_MAH      = "prev_screen_on_mah";
    private static final String PREF_SCREEN_OFF_MS      = "prev_screen_off_ms";
    private static final String PREF_SCREEN_OFF_PCT     = "prev_screen_off_pct";
    private static final String PREF_SCREEN_OFF_MAH     = "prev_screen_off_mah";
    private static final String PREF_DEEP_SLEEP_MS      = "prev_deep_sleep_ms";
    private static final String PREF_RESET_TIME         = "prev_reset_time_ms";

    private static final String KEY_SCREEN_ON_DURATION = "screen_on_duration";
    private static final String KEY_SCREEN_ON_DRAIN = "screen_on_drain";
    private static final String KEY_SCREEN_ON_DRAIN_RATE = "screen_on_drain_rate";
    private static final String KEY_SCREEN_OFF_DURATION = "screen_off_duration";
    private static final String KEY_SCREEN_OFF_DRAIN = "screen_off_drain";
    private static final String KEY_SCREEN_OFF_DEEP_SLEEP = "screen_off_deep_sleep";
    private static final String KEY_SCREEN_OFF_AWAKE = "screen_off_awake";
    private static final String KEY_SCREEN_OFF_DRAIN_RATE = "screen_off_drain_rate";
    private static final String KEY_LEARNED_CAPACITY = "learned_capacity";
    private static final String KEY_ESTIMATED_CAPACITY = "estimated_capacity";
    private static final String KEY_TIME_ON_BATTERY = "time_on_battery";
    private static final String KEY_BATTERY_UPTIME = "battery_uptime";
    private static final String KEY_COMPONENT_POWER_STATS = "component_power_stats";
    private static final String KEY_KERNEL_WAKEUP_STATS = "kernel_wakeup_stats";
    private static final String KEY_UID_WAKELOCK_STATS = "uid_wakelock_stats";
    private static final String KEY_UID_ALARM_STATS = "uid_alarm_stats";
    private static final String KEY_WAKEUP_REASON_STATS = "wakeup_reason_stats";
    private static final String KEY_SUSPEND_ATTEMPTS = "suspend_attempts";
    private static final String KEY_SUSPEND_TIME = "suspend_time";
    private static final String KEY_SUSPEND_OVERHEAD = "suspend_overhead";
    private static final String KEY_SUSPEND_SLEEP_TIME = "suspend_sleep_time";

    private static final String KEY_RAW_STATS = "raw_stats";
    private static final String KEY_SUSPEND_STATS = "suspend_stats";
    private static final String KEY_COMPONENT_POWER_HEADER = "component_power_header";
    private static final String KEY_KERNEL_WAKEUP_HEADER = "kernel_wakeup_header";
    private static final String KEY_UID_WAKELOCK_HEADER = "uid_wakelock_header";
    private static final String KEY_UID_ALARM_HEADER = "uid_alarm_header";
    private static final String KEY_WAKEUP_REASON_HEADER = "wakeup_reason_header";
    private static final String KEY_RAW_STATS_HEADER = "raw_stats_header";
    private static final String KEY_SUSPEND_STATS_HEADER = "suspend_stats_header";

    private static final String KEY_PREV_SESSION_HEADER      = "prev_session_header";
    private static final String KEY_PREV_SESSION_STATS       = "prev_session_stats";
    private static final String KEY_PREV_SESSION_SCREEN_ON   = "prev_session_screen_on";
    private static final String KEY_PREV_SESSION_SCREEN_OFF  = "prev_session_screen_off";
    private static final String KEY_PREV_SESSION_RECORDED_AT = "prev_session_recorded_at";

    private final ArrayMap<String, String> mHeaderToCategory = new ArrayMap<>();
    private final ArrayMap<String, CharSequence> mHeaderAggregates = new ArrayMap<>();
    private boolean mHeadersBound;

    private BatteryStatsManager mBatteryStatsManager;
    private PackageManager mPackageManager;
    private volatile BatterySummaryStats mLastSummary;

    private final AtomicBoolean mRefreshInFlight = new AtomicBoolean(false);

    private final MenuProvider mMenuProvider = new MenuProvider() {
        @Override
        public void onCreateMenu(@NonNull android.view.Menu menu,
                @NonNull android.view.MenuInflater inflater) {
            menu.add(android.view.Menu.NONE, MENU_RESET_STATS,
                    android.view.Menu.NONE, R.string.battery_stats_reset)
                    .setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_NEVER);
        }

        @Override
        public boolean onMenuItemSelected(@NonNull android.view.MenuItem item) {
            if (item.getItemId() == MENU_RESET_STATS) {
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.battery_stats_reset)
                        .setMessage(R.string.battery_stats_reset_confirm)
                        .setPositiveButton(android.R.string.ok, (d, w) ->
                                ThreadUtils.postOnBackgroundThread(() -> {
                                    saveCurrentSessionToPrefs();
                                    if (mBatteryStatsManager != null) {
                                        mBatteryStatsManager.resetStatistics();
                                    }
                                    ThreadUtils.postOnMainThread(() -> {
                                        if (isAdded()) {
                                            loadPreviousSession();
                                            refreshStats();
                                        }
                                    });
                                }))
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return true;
            }
            return false;
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mBatteryStatsManager = context.getSystemService(BatteryStatsManager.class);
        mPackageManager = context.getPackageManager();
    }

    @Override
    public void onViewCreated(@NonNull android.view.View view,
            @Nullable android.os.Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().addMenuProvider(mMenuProvider, getViewLifecycleOwner(),
                Lifecycle.State.RESUMED);
    }

    @Override
    public void onResume() {
        super.onResume();
        setupCollapsibleHeaders();
        loadPreviousSession();
        refreshStats();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHeadersBound = false;
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.FUELGAUGE_BATTERY_HISTORY_DETAIL;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.detailed_battery_stats;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return new ArrayList<>();
    }

    private void refreshStats() {
        if (mBatteryStatsManager == null) {
            showUnavailable();
            return;
        }
        if (!mRefreshInFlight.compareAndSet(false, true)) {
            return;
        }

        final List<Runnable> fetches = Arrays.asList(
                this::fetchSummary,
                this::fetchComponentStats,
                this::fetchKernelWakeupStats,
                this::fetchUidWakelockStats,
                this::fetchUidAlarmStats,
                this::fetchWakeupReasonStats,
                this::fetchSuspendStats
        );
        final AtomicBoolean allDone = new AtomicBoolean(false);
        final int[] remaining = {fetches.size()}; // access only from background threads via countdown
        final java.util.concurrent.atomic.AtomicInteger countdown =
                new java.util.concurrent.atomic.AtomicInteger(fetches.size());
        final Runnable onFetchDone = () -> {
            if (countdown.decrementAndGet() == 0) {
                mRefreshInFlight.set(false);
            }
        };

        for (Runnable fetch : fetches) {
            ThreadUtils.postOnBackgroundThread(() -> { fetch.run(); onFetchDone.run(); });
        }
    }

    private void setupCollapsibleHeaders() {
        if (mHeadersBound) return;
        mHeadersBound = true;
        bindCollapsibleHeader(KEY_COMPONENT_POWER_HEADER, KEY_COMPONENT_POWER_STATS);
        bindCollapsibleHeader(KEY_KERNEL_WAKEUP_HEADER, KEY_KERNEL_WAKEUP_STATS);
        bindCollapsibleHeader(KEY_UID_WAKELOCK_HEADER, KEY_UID_WAKELOCK_STATS);
        bindCollapsibleHeader(KEY_UID_ALARM_HEADER, KEY_UID_ALARM_STATS);
        bindCollapsibleHeader(KEY_WAKEUP_REASON_HEADER, KEY_WAKEUP_REASON_STATS);
        bindCollapsibleHeader(KEY_RAW_STATS_HEADER, KEY_RAW_STATS);
        bindCollapsibleHeader(KEY_SUSPEND_STATS_HEADER, KEY_SUSPEND_STATS);
        bindCollapsibleHeader(KEY_PREV_SESSION_HEADER, KEY_PREV_SESSION_STATS);
    }

    private void bindCollapsibleHeader(String headerKey, String categoryKey) {
        final Preference header = findPreference(headerKey);
        final PreferenceCategory category = findPreference(categoryKey);
        if (header == null || category == null) return;
        mHeaderToCategory.put(headerKey, categoryKey);
        // Collapsed by default (category starts with isPreferenceVisible="false").
        applyExpandedState(headerKey, category.isVisible());
        header.setOnPreferenceClickListener(p -> {
            applyExpandedState(headerKey, !category.isVisible());
            return true;
        });
    }

    private void applyExpandedState(String headerKey, boolean expanded) {
        final String categoryKey = mHeaderToCategory.get(headerKey);
        if (categoryKey == null) return;
        final Preference header = findPreference(headerKey);
        final PreferenceCategory category = findPreference(categoryKey);
        if (header == null || category == null) return;
        category.setVisible(expanded);
        final CharSequence aggregate = mHeaderAggregates.get(headerKey);
        final String hint = getString(expanded
                ? R.string.battery_stats_tap_collapse
                : R.string.battery_stats_tap_expand);
        header.setSummary(aggregate == null
                ? hint
                : getString(R.string.battery_stats_header_summary, aggregate, hint));
    }

    /** Stores the aggregate text for a header and refreshes its summary in place. */
    private void setHeaderAggregate(String headerKey, CharSequence aggregate) {
        mHeaderAggregates.put(headerKey, aggregate);
        final String categoryKey = mHeaderToCategory.get(headerKey);
        if (categoryKey == null) return;
        final PreferenceCategory category = findPreference(categoryKey);
        applyExpandedState(headerKey, category != null && category.isVisible());
    }

    @WorkerThread
    private void fetchSummary() {
        BatterySummaryStats summary = null;
        try {
            summary = mBatteryStatsManager.getBatterySummaryStats();
        } catch (RuntimeException e) {
            Log.w(TAG, "getBatterySummaryStats failed", e);
        }
        final BatterySummaryStats s = summary;
        ThreadUtils.postOnMainThread(() -> {
            if (!isAdded()) return;
            if (s != null) updateSummaryStats(s);
            else showSummaryUnavailable();
        });
    }

    @WorkerThread
    private void fetchComponentStats() {
        BatteryUsageStats usageStats = null;
        final List<ComponentPowerEntry> entries = new ArrayList<>();
        try {
            usageStats = mBatteryStatsManager.getBatteryUsageStats();
            final AggregateBatteryConsumer consumer = usageStats.getAggregateBatteryConsumer(
                    BatteryUsageStats.AGGREGATE_BATTERY_CONSUMER_SCOPE_DEVICE);
            for (int componentId : consumer.getPowerComponentIds()) {
                if (componentId == BatteryConsumer.POWER_COMPONENT_BASE
                        || componentId
                                == BatteryConsumer.POWER_COMPONENT_REATTRIBUTED_TO_OTHER_CONSUMERS) {
                    continue;
                }
                final double powerMah = consumer.getConsumedPower(componentId);
                if (powerMah > 0) {
                    entries.add(new ComponentPowerEntry(
                            componentTitle(consumer.getPowerComponentName(componentId)),
                            powerMah));
                }
            }
        } catch (RuntimeException e) {
            Log.w(TAG, "getBatteryUsageStats failed", e);
        } finally {
            if (usageStats != null) {
                try { usageStats.close(); } catch (Exception ignored) { }
            }
        }
        entries.sort(Comparator.comparingDouble(
                (ComponentPowerEntry e) -> e.powerMah).reversed());
        final List<ComponentPowerEntry> finalEntries = entries;
        ThreadUtils.postOnMainThread(() -> {
            if (!isAdded()) return;
            final PreferenceCategory cat = findPreference(KEY_COMPONENT_POWER_STATS);
            if (cat != null) {
                cat.removeAll();
                addComponentEntries(cat, finalEntries);
            }
            double totalMah = 0;
            for (ComponentPowerEntry e : finalEntries) {
                totalMah += e.powerMah;
            }
            setHeaderAggregate(KEY_COMPONENT_POWER_HEADER,
                    getString(R.string.battery_stats_components_total, totalMah));
        });
    }

    @WorkerThread
    private void fetchKernelWakeupStats() {
        WakeupSourceStats[] stats = null;
        try {
            stats = mBatteryStatsManager.getKernelWakeupStats();
        } catch (RuntimeException e) {
            Log.w(TAG, "getKernelWakeupStats failed", e);
        }
        final WakeupSourceStats[] finalStats = stats;
        ThreadUtils.postOnMainThread(() -> {
            if (!isAdded()) return;
            updateKernelWakeupStats(finalStats);
        });
    }

    @WorkerThread
    private void fetchUidWakelockStats() {
        UidWakelockStats[] stats = null;
        try {
            stats = mBatteryStatsManager.getUidWakelockStats();
        } catch (RuntimeException e) {
            Log.w(TAG, "getUidWakelockStats failed", e);
        }
        final UidWakelockStats[] finalStats = stats;
        ThreadUtils.postOnMainThread(() -> {
            if (!isAdded()) return;
            updateUidWakelockStats(finalStats);
        });
    }

    @WorkerThread
    private void fetchUidAlarmStats() {
        UidAlarmStats[] stats = null;
        try {
            stats = mBatteryStatsManager.getUidAlarmStats();
        } catch (RuntimeException e) {
            Log.w(TAG, "getUidAlarmStats failed", e);
        }
        final UidAlarmStats[] finalStats = stats;
        ThreadUtils.postOnMainThread(() -> {
            if (!isAdded()) return;
            updateUidAlarmStats(finalStats);
        });
    }

    @WorkerThread
    private void fetchWakeupReasonStats() {
        WakeupSourceStats[] stats = null;
        try {
            stats = mBatteryStatsManager.getWakeupReasonStats();
        } catch (RuntimeException e) {
            Log.w(TAG, "getWakeupReasonStats failed", e);
        }
        final WakeupSourceStats[] finalStats = stats;
        ThreadUtils.postOnMainThread(() -> {
            if (!isAdded()) return;
            updateWakeupReasonStats(finalStats);
        });
    }

    @WorkerThread
    private void fetchSuspendStats() {
        SuspendStats stats = null;
        try {
            stats = mBatteryStatsManager.getSystemSuspendStats();
        } catch (RuntimeException e) {
            Log.w(TAG, "getSystemSuspendStats failed", e);
        }
        final SuspendStats finalStats = stats;
        ThreadUtils.postOnMainThread(() -> {
            if (!isAdded()) return;
            updateSuspendStats(finalStats);
        });
    }

    private void updateSummaryStats(@NonNull BatterySummaryStats stats) {
        mLastSummary = stats;
        final double capMah = stats.learnedBatteryCapacityUah > 0
                ? stats.learnedBatteryCapacityUah / 1000.0
                : stats.estimatedBatteryCapacityMah;
        setSummary(KEY_SCREEN_ON_DURATION, formatDuration(stats.screenOnTimeMs));
        setSummary(KEY_SCREEN_ON_DRAIN,
                formatDrainPctAndMah(stats.screenOnDischargePercent, stats.screenOnDischargeMah));
        setSummary(KEY_SCREEN_ON_DRAIN_RATE,
                formatDrainRate(stats.screenOnDischargeMah, stats.screenOnTimeMs, capMah));
        setSummary(KEY_SCREEN_OFF_DURATION, formatDuration(stats.screenOffTimeMs));
        setSummary(KEY_SCREEN_OFF_DRAIN,
                formatDrainPctAndMah(stats.screenOffDischargePercent,
                        stats.screenOffDischargeMah));
        setSummary(KEY_SCREEN_OFF_DEEP_SLEEP,
                formatDurationAndPercent(stats.deepSleepTimeMs, stats.screenOffTimeMs));
        setSummary(KEY_SCREEN_OFF_AWAKE,
                formatDurationAndPercent(stats.screenOffAwakeTimeMs, stats.screenOffTimeMs));
        setSummary(KEY_SCREEN_OFF_DRAIN_RATE,
                formatDrainRate(stats.screenOffDischargeMah, stats.screenOffTimeMs, capMah));
        setSummary(KEY_LEARNED_CAPACITY, formatCapacity(stats.learnedBatteryCapacityUah / 1000));
        setSummary(KEY_ESTIMATED_CAPACITY, formatCapacity(stats.estimatedBatteryCapacityMah));
        setSummary(KEY_TIME_ON_BATTERY, formatDuration(stats.batteryRealtimeMs));
        setSummary(KEY_BATTERY_UPTIME, formatDuration(stats.batteryUptimeMs));
        setHeaderAggregate(KEY_RAW_STATS_HEADER,
                getString(R.string.battery_stats_raw_total, (int) stats.estimatedBatteryCapacityMah));
    }

    private void addComponentEntries(
            @NonNull PreferenceCategory category,
            @NonNull List<ComponentPowerEntry> entries) {
        if (entries.isEmpty()) {
            addStaticPreference(category, "component_empty", R.string.battery_stats_no_data, null);
            return;
        }
        for (int i = 0; i < entries.size(); i++) {
            final ComponentPowerEntry entry = entries.get(i);
            addStaticPreference(category, "component_" + i, entry.title,
                    getString(R.string.battery_stats_power_mah, entry.powerMah));
        }
    }

    private void updateKernelWakeupStats(WakeupSourceStats[] stats) {
        final PreferenceCategory cat = findPreference(KEY_KERNEL_WAKEUP_STATS);
        if (cat == null) return;
        cat.removeAll();
        long kTotalCount = 0, kTotalTimeMs = 0;
        if (stats != null) {
            for (WakeupSourceStats s : stats) {
                kTotalCount += s.count;
                kTotalTimeMs += s.totalTimeMs;
            }
        }
        setHeaderAggregate(KEY_KERNEL_WAKEUP_HEADER,
                getString(R.string.battery_stats_kernel_total,
                        kTotalCount, formatDuration(kTotalTimeMs)));
        if (stats == null || stats.length == 0) {
            addStaticPreference(cat, "kwakeup_empty", R.string.battery_stats_no_data, null);
            return;
        }
        for (int i = 0; i < stats.length; i++) {
            final WakeupSourceStats s = stats[i];
            addStaticPreference(cat, "kwakeup_" + i, s.name,
                    getString(R.string.battery_stats_count_time,
                            s.count, formatDuration(s.totalTimeMs)));
        }
    }

    private void updateUidWakelockStats(UidWakelockStats[] stats) {
        final PreferenceCategory cat = findPreference(KEY_UID_WAKELOCK_STATS);
        if (cat == null) return;
        cat.removeAll();
        long wTotalTimeMs = 0;
        if (stats != null) {
            for (UidWakelockStats s : stats) wTotalTimeMs += s.partialTimeMs;
        }
        setHeaderAggregate(KEY_UID_WAKELOCK_HEADER,
                getString(R.string.battery_stats_wakelocks_total,
                        stats == null ? 0 : stats.length, formatDuration(wTotalTimeMs)));
        if (stats == null || stats.length == 0) {
            addStaticPreference(cat, "uwakelock_empty", R.string.battery_stats_no_data, null);
            return;
        }
        for (int i = 0; i < stats.length; i++) {
            final UidWakelockStats s = stats[i];
            final String title = resolveUidLabel(s.uid) + " / " + s.wakelockName;
            final String summary = getString(R.string.battery_stats_wakelock_summary,
                    formatDuration(s.partialTimeMs), s.count);
            addStaticPreference(cat, "uwakelock_" + i, title, summary);
        }
    }

    private void updateUidAlarmStats(UidAlarmStats[] stats) {
        final PreferenceCategory cat = findPreference(KEY_UID_ALARM_STATS);
        if (cat == null) return;
        cat.removeAll();
        long aTotalWakeups = 0;
        if (stats != null) {
            for (UidAlarmStats s : stats) aTotalWakeups += s.count;
        }
        setHeaderAggregate(KEY_UID_ALARM_HEADER,
                getString(R.string.battery_stats_alarms_total,
                        stats == null ? 0 : stats.length, aTotalWakeups));
        if (stats == null || stats.length == 0) {
            addStaticPreference(cat, "ualarm_empty", R.string.battery_stats_no_data, null);
            return;
        }
        for (int i = 0; i < stats.length; i++) {
            final UidAlarmStats s = stats[i];
            final String appLabel = resolvePackageLabel(s.packageName);
            final String title = appLabel + " / " + s.tag;
            final String summary = getString(R.string.battery_stats_alarm_wakeups, s.count);
            addStaticPreference(cat, "ualarm_" + i, title, summary);
        }
    }

    private void updateWakeupReasonStats(WakeupSourceStats[] stats) {
        final PreferenceCategory cat = findPreference(KEY_WAKEUP_REASON_STATS);
        if (cat == null) return;
        cat.removeAll();
        long rTotalCount = 0;
        if (stats != null) {
            for (WakeupSourceStats s : stats) rTotalCount += s.count;
        }
        setHeaderAggregate(KEY_WAKEUP_REASON_HEADER,
                getString(R.string.battery_stats_reasons_total,
                        stats == null ? 0 : stats.length, rTotalCount));
        if (stats == null || stats.length == 0) {
            addStaticPreference(cat, "wreason_empty", R.string.battery_stats_no_data, null);
            return;
        }
        for (int i = 0; i < stats.length; i++) {
            final WakeupSourceStats s = stats[i];
            addStaticPreference(cat, "wreason_" + i, s.name,
                    getString(R.string.battery_stats_count_time,
                            s.count, formatDuration(s.totalTimeMs)));
        }
    }

    private void updateSuspendStats(SuspendStats stats) {
        if (stats == null || !stats.available) {
            setHeaderAggregate(KEY_SUSPEND_STATS_HEADER,
                    getString(R.string.battery_stats_unavailable));
            setSummary(KEY_SUSPEND_ATTEMPTS, R.string.battery_stats_unavailable);
            setSummary(KEY_SUSPEND_TIME, R.string.battery_stats_unavailable);
            setSummary(KEY_SUSPEND_OVERHEAD, R.string.battery_stats_unavailable);
            setSummary(KEY_SUSPEND_SLEEP_TIME, R.string.battery_stats_unavailable);
            return;
        }
        setHeaderAggregate(KEY_SUSPEND_STATS_HEADER,
                getString(R.string.battery_stats_suspend_summary,
                        stats.suspendAttemptCount,
                        stats.failedSuspendCount,
                        stats.shortSuspendCount));
        setSummary(KEY_SUSPEND_ATTEMPTS,
                getString(R.string.battery_stats_suspend_summary,
                        stats.suspendAttemptCount,
                        stats.failedSuspendCount,
                        stats.shortSuspendCount));
        setSummary(KEY_SUSPEND_TIME, formatDuration(stats.suspendTimeMillis));
        setSummary(KEY_SUSPEND_OVERHEAD,
                formatDuration(stats.suspendOverheadTimeMillis
                        + stats.failedSuspendOverheadTimeMillis));
        setSummary(KEY_SUSPEND_SLEEP_TIME, formatDuration(stats.sleepTimeMillis));
    }

    private void showSummaryUnavailable() {
        setSummary(KEY_SCREEN_ON_DURATION, R.string.battery_stats_unavailable);
        setSummary(KEY_SCREEN_ON_DRAIN, R.string.battery_stats_unavailable);
        setSummary(KEY_SCREEN_ON_DRAIN_RATE, R.string.battery_stats_unavailable);
        setSummary(KEY_SCREEN_OFF_DURATION, R.string.battery_stats_unavailable);
        setSummary(KEY_SCREEN_OFF_DRAIN, R.string.battery_stats_unavailable);
        setSummary(KEY_SCREEN_OFF_DEEP_SLEEP, R.string.battery_stats_unavailable);
        setSummary(KEY_SCREEN_OFF_AWAKE, R.string.battery_stats_unavailable);
        setSummary(KEY_SCREEN_OFF_DRAIN_RATE, R.string.battery_stats_unavailable);
        setSummary(KEY_LEARNED_CAPACITY, R.string.battery_stats_unavailable);
        setSummary(KEY_ESTIMATED_CAPACITY, R.string.battery_stats_unavailable);
        setSummary(KEY_TIME_ON_BATTERY, R.string.battery_stats_unavailable);
        setSummary(KEY_BATTERY_UPTIME, R.string.battery_stats_unavailable);
    }

    private void showUnavailable() {
        showSummaryUnavailable();
        setSummary(KEY_SUSPEND_ATTEMPTS, R.string.battery_stats_unavailable);
        setSummary(KEY_SUSPEND_TIME, R.string.battery_stats_unavailable);
        setSummary(KEY_SUSPEND_OVERHEAD, R.string.battery_stats_unavailable);
        setSummary(KEY_SUSPEND_SLEEP_TIME, R.string.battery_stats_unavailable);
    }

    private void setSummary(String key, int resId) {
        setSummary(key, getString(resId));
    }

    private void setSummary(String key, CharSequence summary) {
        final Preference preference = findPreference(key);
        if (preference != null) {
            preference.setSummary(summary);
        }
    }

    private void addStaticPreference(
            PreferenceCategory category, String key, int titleResId, CharSequence summary) {
        addStaticPreference(category, key, getString(titleResId), summary);
    }

    private void addStaticPreference(
            PreferenceCategory category, String key, CharSequence title, CharSequence summary) {
        final Preference preference = new Preference(category.getContext());
        preference.setKey(key);
        preference.setTitle(title);
        preference.setSummary(summary);
        preference.setSelectable(false);
        category.addPreference(preference);
    }

    private String formatDuration(long durationMs) {
        if (durationMs <= 0) {
            return getString(R.string.battery_stats_unavailable);
        }
        return BatteryUtils.formatElapsedTimeWithoutComma(
                getContext(), durationMs, /* withSeconds= */ true,
                /* collapseTimeUnit= */ false).toString();
    }

    private String formatDurationAndPercent(long durationMs, long totalMs) {
        final String duration = formatDuration(durationMs);
        final double percent = totalMs > 0 ? durationMs * 100.0 / totalMs : 0.0;
        return getString(R.string.battery_stats_time_percent,
                duration, String.format(Locale.getDefault(), "%.1f%%", percent));
    }

    private String formatDrainRate(long drainMah, long durationMs, double capacityMah) {
        if (drainMah <= 0 || durationMs <= 0 || capacityMah <= 0) {
            return getString(R.string.battery_stats_unavailable);
        }
        final double hours = durationMs / (60.0 * 60.0 * 1000.0);
        final double rate = (drainMah / capacityMah) * 100.0 / hours;
        return getString(R.string.battery_stats_drain_rate, rate);
    }

    private String formatCapacity(int capacityMah) {
        return capacityMah > 0
                ? getString(R.string.battery_stats_capacity_mah, capacityMah)
                : getString(R.string.battery_stats_unavailable);
    }

    private String formatDrainPctAndMah(int pct, long mah) {
        if (mah > 0) {
            return getString(R.string.battery_stats_drain_pct_mah, pct, (int) mah);
        }
        return getString(R.string.battery_stats_percent, pct);
    }

    @WorkerThread
    private void saveCurrentSessionToPrefs() {
        final BatterySummaryStats snap = mLastSummary;
        if (snap == null || !isAdded()) return;
        requireContext().getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE)
                .edit()
                .putLong(PREF_SCREEN_ON_MS,   snap.screenOnTimeMs)
                .putInt (PREF_SCREEN_ON_PCT,  snap.screenOnDischargePercent)
                .putLong(PREF_SCREEN_ON_MAH,  snap.screenOnDischargeMah)
                .putLong(PREF_SCREEN_OFF_MS,  snap.screenOffTimeMs)
                .putInt (PREF_SCREEN_OFF_PCT, snap.screenOffDischargePercent)
                .putLong(PREF_SCREEN_OFF_MAH, snap.screenOffDischargeMah)
                .putLong(PREF_DEEP_SLEEP_MS,  snap.deepSleepTimeMs)
                .putLong(PREF_RESET_TIME,     System.currentTimeMillis())
                .apply();
    }

    private void loadPreviousSession() {
        if (!isAdded()) return;
        final SharedPreferences prefs =
                requireContext().getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE);
        final long resetTime = prefs.getLong(PREF_RESET_TIME, 0L);
        if (resetTime == 0L) {
            setHeaderAggregate(KEY_PREV_SESSION_HEADER,
                    getString(R.string.battery_stats_prev_session_none));
            final PreferenceCategory cat = findPreference(KEY_PREV_SESSION_STATS);
            if (cat != null) cat.setVisible(false);
            return;
        }

        final long screenOnMs  = prefs.getLong(PREF_SCREEN_ON_MS, 0);
        final int  screenOnPct = prefs.getInt (PREF_SCREEN_ON_PCT, 0);
        final long screenOnMah = prefs.getLong(PREF_SCREEN_ON_MAH, 0);
        final long screenOffMs  = prefs.getLong(PREF_SCREEN_OFF_MS, 0);
        final int  screenOffPct = prefs.getInt (PREF_SCREEN_OFF_PCT, 0);
        final long screenOffMah = prefs.getLong(PREF_SCREEN_OFF_MAH, 0);

        final String screenOnSummary = getString(R.string.battery_stats_session_row,
                formatDuration(screenOnMs),
                formatDrainPctAndMah(screenOnPct, screenOnMah));
        final String screenOffSummary = getString(R.string.battery_stats_session_row,
                formatDuration(screenOffMs),
                formatDrainPctAndMah(screenOffPct, screenOffMah));
        final String recordedAt = DateFormat.getDateTimeInstance(
                DateFormat.SHORT, DateFormat.SHORT).format(new Date(resetTime));

        setSummary(KEY_PREV_SESSION_SCREEN_ON,   screenOnSummary);
        setSummary(KEY_PREV_SESSION_SCREEN_OFF,  screenOffSummary);
        setSummary(KEY_PREV_SESSION_RECORDED_AT, recordedAt);

        final PreferenceCategory cat = findPreference(KEY_PREV_SESSION_STATS);
        if (cat != null && !cat.isVisible()) cat.setVisible(false); // keep user's expand state
        setHeaderAggregate(KEY_PREV_SESSION_HEADER,
                getString(R.string.battery_stats_session_row,
                        formatDuration(screenOnMs + screenOffMs),
                        formatDrainPctAndMah(screenOnPct + screenOffPct,
                                screenOnMah + screenOffMah)));
    }

    /**
     * Returns a human-readable label for a UID: app label if resolvable, else "uid/<n>".
     * Intentionally cheap — only called for the top-N entries shown.
     */
    private String resolveUidLabel(int uid) {
        if (mPackageManager == null) return "uid/" + uid;
        final String[] packages = mPackageManager.getPackagesForUid(uid);
        if (packages == null || packages.length == 0) return "uid/" + uid;
        return resolvePackageLabel(packages[0]);
    }

    /**
     * Returns the app label for a package name, or the package name itself if not resolvable.
     */
    private String resolvePackageLabel(String packageName) {
        if (mPackageManager == null || packageName == null) return packageName;
        try {
            final ApplicationInfo info = mPackageManager.getApplicationInfo(packageName, 0);
            final CharSequence label = mPackageManager.getApplicationLabel(info);
            return label != null ? label.toString() : packageName;
        } catch (PackageManager.NameNotFoundException e) {
            return packageName;
        }
    }

    private static String componentTitle(String componentName) {
        if (componentName == null || componentName.isEmpty()) return "Unknown";
        final String[] parts = componentName.split("_");
        final StringBuilder title = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (title.length() > 0) title.append(' ');
            title.append(part.substring(0, 1).toUpperCase(Locale.ROOT));
            if (part.length() > 1) title.append(part.substring(1));
        }
        return title.toString();
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.detailed_battery_stats);

    private static final class ComponentPowerEntry {
        final String title;
        final double powerMah;

        ComponentPowerEntry(String title, double powerMah) {
            this.title = title;
            this.powerMah = powerMah;
        }
    }
}
