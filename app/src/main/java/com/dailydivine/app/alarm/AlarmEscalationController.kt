package com.dailydivine.app.alarm

/**
 * PRD F004-R19 (v1.1, UPDATED).
 *
 * Replaces the original "auto-dismiss after 5 minutes of no interaction"
 * behavior, which let a deep sleeper's alarm go silent and stopped waking
 * them up. New behavior:
 *
 *   RINGING --(no interaction for [escalationWindowMs])--> auto-snooze
 *   auto-snooze --(fires again)--> RINGING, with cyclesUsed += 1
 *   ... up to [maxAutoSnoozeCycles] times ...
 *   final cycle exhausted --> STOP + log "alarm_missed"
 *
 * This class is pure state/decision logic with no Android framework
 * dependencies, so it's directly unit-testable (see AlarmEscalationControllerTest).
 * AlarmService is responsible for actually driving the tone volume ramp and
 * calling into AlarmScheduler.scheduleSnooze() based on this class's decisions.
 */
class AlarmEscalationController(
    private val escalationWindowMs: Long = DEFAULT_ESCALATION_WINDOW_MS,
    private val autoSnoozeMinutes: Int = DEFAULT_AUTO_SNOOZE_MINUTES,
    private val maxAutoSnoozeCycles: Int = DEFAULT_MAX_AUTO_SNOOZE_CYCLES
) {

    sealed class Decision {
        /** Still within the escalation window — keep ringing / keep ramping volume. */
        data class KeepEscalating(val volumeFraction: Float) : Decision()

        /** Escalation window elapsed with no interaction — auto-snooze and try again. */
        data class AutoSnooze(val minutes: Int, val cyclesUsedAfterThis: Int) : Decision()

        /** All auto-snooze cycles exhausted — stop the alarm and log the miss. */
        data class StopAndLogMissed(val totalCyclesUsed: Int) : Decision()
    }

    /**
     * @param elapsedMsSinceRingStart time since the tone started for the current cycle
     * @param cyclesUsedSoFar         how many auto-snooze cycles have already elapsed
     */
    fun onTick(elapsedMsSinceRingStart: Long, cyclesUsedSoFar: Int): Decision {
        if (elapsedMsSinceRingStart < escalationWindowMs) {
            val fraction = (elapsedMsSinceRingStart.toFloat() / escalationWindowMs).coerceIn(0f, 1f)
            return Decision.KeepEscalating(volumeFraction = fraction)
        }

        return if (cyclesUsedSoFar < maxAutoSnoozeCycles) {
            Decision.AutoSnooze(minutes = autoSnoozeMinutes, cyclesUsedAfterThis = cyclesUsedSoFar + 1)
        } else {
            Decision.StopAndLogMissed(totalCyclesUsed = cyclesUsedSoFar)
        }
    }

    companion object {
        const val DEFAULT_ESCALATION_WINDOW_MS = 30 * 60 * 1000L // 30 minutes
        const val DEFAULT_AUTO_SNOOZE_MINUTES = 5
        const val DEFAULT_MAX_AUTO_SNOOZE_CYCLES = 6

        // Firebase Analytics event name + params (see PRD Section 26.1)
        const val EVENT_ALARM_MISSED = "alarm_missed"
        const val PARAM_ALARM_ID = "alarm_id"
        const val PARAM_SNOOZE_CYCLES_USED = "snooze_cycles_used"
    }
}
