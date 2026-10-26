package com.dailydivine.app.alarm

/**
 * Volume over time for one ring cycle (PRD F004-R10, R11, R19).
 *
 *  - R10: the alarm has its own volume (10-100 %), applied to the player, independent of the
 *    phone's alarm-stream slider. Never below [MIN_PERCENT]: an alarm must not be silent.
 *  - R11 (gradual option): starts quiet and reaches the chosen volume after [RAMP_MS] (30 s).
 *    With the option off, it starts at the chosen volume.
 *  - R19: if nobody responds, the volume keeps escalating from the chosen level up to 100 %
 *    across the rest of the escalation window.
 *
 * Pure (no Android) so it is unit-tested.
 */
object AlarmVolumeCurve {

    const val RAMP_MS = 30_000L
    const val START_FRACTION = 0.15f
    const val MIN_PERCENT = 10

    fun clampPercent(percent: Int): Int = percent.coerceIn(MIN_PERCENT, 100)

    fun volumeAt(elapsedMs: Long, windowMs: Long, volumePercent: Int, gradual: Boolean): Float {
        val base = clampPercent(volumePercent) / 100f
        val elapsed = elapsedMs.coerceAtLeast(0L)

        val ramped = if (gradual) {
            val start = minOf(START_FRACTION, base)
            start + (base - start) * (elapsed.toFloat() / RAMP_MS).coerceIn(0f, 1f)
        } else {
            base
        }

        val tail = windowMs - RAMP_MS
        val escalation = if (tail > 0 && elapsed > RAMP_MS) {
            (1f - base) * ((elapsed - RAMP_MS).toFloat() / tail).coerceIn(0f, 1f)
        } else {
            0f
        }
        return (ramped + escalation).coerceIn(0f, 1f)
    }
}
