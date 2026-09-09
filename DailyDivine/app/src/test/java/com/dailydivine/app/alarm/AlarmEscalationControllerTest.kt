package com.dailydivine.app.alarm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies F004-R19 (v1.1): an alarm left unattended must escalate through
 * multiple auto-snooze cycles rather than silently going quiet after a flat
 * timeout, and must still terminate (not loop forever) once the cycle cap
 * is reached.
 *
 * This logic was additionally verified standalone (outside Gradle/Android)
 * during implementation — see /docs/verification/escalation-check.md.
 */
class AlarmEscalationControllerTest {

    private val controller = AlarmEscalationController(
        escalationWindowMs = 1000L,
        autoSnoozeMinutes = 5,
        maxAutoSnoozeCycles = 6
    )

    @Test
    fun `within escalation window keeps ringing with ramping volume`() {
        val decision = controller.onTick(elapsedMsSinceRingStart = 200L, cyclesUsedSoFar = 0)
        assertTrue(decision is AlarmEscalationController.Decision.KeepEscalating)
        val fraction = (decision as AlarmEscalationController.Decision.KeepEscalating).volumeFraction
        assertTrue(fraction in 0f..1f)
    }

    @Test
    fun `window elapsed with cycles remaining triggers auto-snooze`() {
        val decision = controller.onTick(elapsedMsSinceRingStart = 1500L, cyclesUsedSoFar = 0)
        assertTrue(decision is AlarmEscalationController.Decision.AutoSnooze)
        val snooze = decision as AlarmEscalationController.Decision.AutoSnooze
        assertEquals(1, snooze.cyclesUsedAfterThis)
        assertEquals(5, snooze.minutes)
    }

    @Test
    fun `exhausting all cycles stops and logs missed alarm`() {
        val decision = controller.onTick(elapsedMsSinceRingStart = 1500L, cyclesUsedSoFar = 6)
        assertTrue(decision is AlarmEscalationController.Decision.StopAndLogMissed)
        assertEquals(6, (decision as AlarmEscalationController.Decision.StopAndLogMissed).totalCyclesUsed)
    }

    @Test
    fun `escalation loop always terminates within max cycles`() {
        var cycles = 0
        var decision: AlarmEscalationController.Decision = controller.onTick(1500L, cycles)
        var loops = 0
        while (decision is AlarmEscalationController.Decision.AutoSnooze) {
            cycles = decision.cyclesUsedAfterThis
            decision = controller.onTick(1500L, cycles)
            loops++
            assertTrue("escalation must terminate, not loop forever", loops <= 20)
        }
        assertTrue(decision is AlarmEscalationController.Decision.StopAndLogMissed)
        assertEquals(6, cycles)
    }
}
