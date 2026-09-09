package com.dailydivine.app.domain.model

data class MilestoneBadge(val days: Int, val name: String, val description: String)

data class StreakInfo(
    val currentStreak: Int,
    val longestStreak: Int,
    val totalDaysActive: Int,
    val currentMilestone: MilestoneBadge?,
    val nextMilestone: MilestoneBadge?,
    val progressToNext: Float,
    val hasFreezeAvailable: Boolean
) {
    companion object {
        // PRD Section 6.2 / F003 milestone table
        val MILESTONES = listOf(
            MilestoneBadge(1, "First Step", "Started the journey"),
            MilestoneBadge(7, "Week Warrior", "7 consecutive days"),
            MilestoneBadge(21, "Habit Formed", "21 days - habit formed"),
            MilestoneBadge(30, "Monthly Devotee", "Full month completed"),
            MilestoneBadge(50, "Half Century", "50 days strong"),
            MilestoneBadge(100, "Centurion", "Triple digits!"),
            MilestoneBadge(200, "Deep Roots", "Deeply committed"),
            MilestoneBadge(365, "Yearly Devotee", "Full year completed"),
            MilestoneBadge(500, "Enlightened", "Legendary commitment"),
            MilestoneBadge(730, "Divine Soul", "2 years of devotion")
        )
    }
}
