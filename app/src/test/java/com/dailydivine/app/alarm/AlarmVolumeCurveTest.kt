package com.dailydivine.app.alarm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AlarmVolumeCurveTest {

    private val window = AlarmEscalationController.DEFAULT_ESCALATION_WINDOW_MS

    @Test fun gradualStartsQuietAndReachesChosenVolumeAfter30Seconds() {
        assertEquals(0.15f, AlarmVolumeCurve.volumeAt(0, window, 80, true), 0.001f)
        assertEquals(0.475f, AlarmVolumeCurve.volumeAt(15_000, window, 80, true), 0.01f)
        assertEquals(0.80f, AlarmVolumeCurve.volumeAt(AlarmVolumeCurve.RAMP_MS, window, 80, true), 0.001f)
    }

    @Test fun nonGradualStartsAtChosenVolume() {
        assertEquals(0.60f, AlarmVolumeCurve.volumeAt(0, window, 60, false), 0.001f)
        assertEquals(0.60f, AlarmVolumeCurve.volumeAt(20_000, window, 60, false), 0.001f)
    }

    @Test fun unattendedAlarmEscalatesToFullVolumeAtTheEndOfTheWindow() {
        assertEquals(1.0f, AlarmVolumeCurve.volumeAt(window, window, 80, true), 0.001f)
        assertEquals(1.0f, AlarmVolumeCurve.volumeAt(window, window, 80, false), 0.001f)
        val midway = AlarmVolumeCurve.volumeAt(window / 2, window, 80, true)
        assertTrue(midway > 0.80f && midway < 1.0f)
    }

    @Test fun neverDecreasesOverTime() {
        for (gradual in listOf(true, false)) {
            var previous = 0f
            var t = 0L
            while (t <= window) {
                val v = AlarmVolumeCurve.volumeAt(t, window, 70, gradual)
                assertTrue("volume dropped at $t ms", v + 1e-6f >= previous)
                previous = v
                t += 5_000
            }
        }
    }

    @Test fun volumeIsClampedSoTheAlarmIsNeverSilent() {
        assertEquals(10, AlarmVolumeCurve.clampPercent(0))
        assertEquals(10, AlarmVolumeCurve.clampPercent(-5))
        assertEquals(100, AlarmVolumeCurve.clampPercent(250))
        assertTrue(AlarmVolumeCurve.volumeAt(0, window, 0, true) >= 0.1f - 1e-6f)
        assertTrue(AlarmVolumeCurve.volumeAt(0, window, 0, false) >= 0.1f - 1e-6f)
    }

    @Test fun veryLowChosenVolumeStartsAtItsOwnLevelNotAbove() {
        // 10 % is below the 15 % ramp start: the ramp must not start louder than the setting.
        assertEquals(0.10f, AlarmVolumeCurve.volumeAt(0, window, 10, true), 0.001f)
    }

    @Test fun fullVolumeStaysFull() {
        assertEquals(1.0f, AlarmVolumeCurve.volumeAt(window / 2, window, 100, false), 0.001f)
        assertEquals(1.0f, AlarmVolumeCurve.volumeAt(AlarmVolumeCurve.RAMP_MS, window, 100, true), 0.001f)
    }

    @Test fun negativeElapsedIsTreatedAsZero() {
        assertEquals(0.15f, AlarmVolumeCurve.volumeAt(-1_000, window, 80, true), 0.001f)
    }
}
