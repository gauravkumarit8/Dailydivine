package com.dailydivine.app.ui.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailydivine.app.data.local.entity.Alarm
import com.dailydivine.app.data.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    private val alarmRepository: AlarmRepository
) : ViewModel() {

    val alarms: StateFlow<List<Alarm>> = alarmRepository.getAllAlarms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
