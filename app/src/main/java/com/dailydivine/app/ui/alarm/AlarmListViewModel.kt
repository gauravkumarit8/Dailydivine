package com.dailydivine.app.ui.alarm

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.PowerManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailydivine.app.alarm.AlarmScheduler
import com.dailydivine.app.data.local.entity.Alarm
import com.dailydivine.app.data.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Screen S11 (list view). Every mutation goes through AlarmRepository, never
 * AlarmDao directly, so Room and the real system alarm can never drift out
 * of sync (see AlarmRepository's own doc comment).
 */
@HiltViewModel
class AlarmListViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    val alarms: StateFlow<List<Alarm>> = alarmRepository.getAllAlarms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // v1.1/F004-R25: this tab previously had NO visibility into exact-alarm
    // permission status at all -- a user who skipped/denied it during
    // onboarding had no way to discover or fix that every alarm they
    // create or edit here is silently running through the inexact
    // setWindow() fallback (up to a 10-minute delivery window).
    private val _canScheduleExactAlarms = MutableStateFlow(AlarmScheduler(appContext).canScheduleExactAlarms())
    val canScheduleExactAlarms: StateFlow<Boolean> = _canScheduleExactAlarms.asStateFlow()

    // F004-R24 ("Handle battery optimization exemption request") -- was
    // never implemented anywhere in the app. Aggressive battery
    // optimization (especially on some OEM Android skins) can kill the
    // app's process before AlarmReceiver's broadcast ever gets to start
    // AlarmService, which looks identical to "the alarm just didn't fire"
    // from the user's side, same failure mode as the missing exact-alarm
    // permission above.
    private val _isIgnoringBatteryOptimizations = MutableStateFlow(checkIgnoringBatteryOptimizations())
    val isIgnoringBatteryOptimizations: StateFlow<Boolean> = _isIgnoringBatteryOptimizations.asStateFlow()

    fun refreshExactAlarmPermission() {
        _canScheduleExactAlarms.value = AlarmScheduler(appContext).canScheduleExactAlarms()
        _isIgnoringBatteryOptimizations.value = checkIgnoringBatteryOptimizations()
    }

    private fun checkIgnoringBatteryOptimizations(): Boolean {
        val powerManager = appContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(appContext.packageName)
    }

    /** True only for debuggable builds (CI debug APK); hides the test button in release. */
    val isDebuggable: Boolean =
        (appContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    /** Rings the full real path (Receiver -> Service -> Ring screen) in [seconds] seconds. */
    fun fireTestAlarm(seconds: Int = 10) {
        AlarmScheduler(appContext).scheduleTest(seconds)
    }

    fun toggleEnabled(alarm: Alarm) {
        viewModelScope.launch {
            alarmRepository.updateAndReschedule(alarm.copy(isEnabled = !alarm.isEnabled))
        }
    }

    fun updateTime(alarm: Alarm, hour: Int, minute: Int) {
        viewModelScope.launch {
            alarmRepository.updateAndReschedule(alarm.copy(hour = hour, minute = minute))
        }
    }

    fun toggleTts(alarm: Alarm) {
        viewModelScope.launch {
            alarmRepository.updateAndReschedule(alarm.copy(isTTSEnabled = !alarm.isTTSEnabled))
        }
    }

    fun delete(alarm: Alarm) {
        viewModelScope.launch { alarmRepository.delete(alarm) }
    }

    /** F004-R01/R02: 1 alarm free, up to 5 premium -- premium gating isn't
     *  wired yet (Sprint 7), so this just creates another daily alarm at a
     *  sensible default time for now. */
    fun addAlarm() {
        viewModelScope.launch {
            alarmRepository.createAndSchedule(hour = 7, minute = 0, label = "New Alarm")
        }
    }
}
