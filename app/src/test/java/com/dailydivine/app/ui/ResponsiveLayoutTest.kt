package com.dailydivine.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import com.dailydivine.app.UiLayoutTests
import com.dailydivine.app.ui.alarm.AlarmRingScreen
import com.dailydivine.app.ui.onboarding.ReligionSelectScreen
import com.dailydivine.app.ui.onboarding.WelcomeScreen
import com.dailydivine.app.ui.theme.DailyDivineTheme
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Automated proof that the screens that matter most stay usable on small phones, with large
 * system fonts, and in landscape. Each test sets the window size with a Robolectric qualifier
 * (e.g. w320dp-h568dp is a small phone) and the font scale through LocalDensity, then asserts
 * the primary controls are displayed, i.e. inside the window and not clipped away.
 *
 * Runs only with -PuiTests (see build.gradle.kts and the "UI layout tests" step in build.yml).
 */
@Category(UiLayoutTests::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ResponsiveLayoutTest {

    @get:Rule
    val rule = createComposeRule()

    private val longVerse =
        "Let the wise man, who has understood this, hold to the path of patience and kindness, " +
            "for the mind that is trained brings happiness, and a quiet heart is its own reward."

    private fun show(fontScale: Float, content: @Composable () -> Unit) {
        // The ring screen has an infinite pulse animation, which would keep a test from ever
        // becoming idle; drive the clock by hand instead.
        rule.mainClock.autoAdvance = false
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                DailyDivineTheme(religionId = 1) { content() }
            }
        }
        rule.mainClock.advanceTimeBy(500)
    }

    private fun showRing(fontScale: Float) = show(fontScale) {
        AlarmRingScreen(
            currentTime = "10:30 AM",
            versePreview = longVerse,
            snoozeMinutes = 10,
            onSnooze = {},
            onWakeUpAndRead = {}
        )
    }

    private fun assertRingControlsReachable() {
        rule.onNodeWithText("10:30 AM").assertIsDisplayed()
        rule.onNodeWithText("Wake Up & Read").assertIsDisplayed()
        rule.onNodeWithText("Snooze", substring = true).assertIsDisplayed()
    }

    // ---- Alarm ring screen: the one screen a half-asleep user must be able to operate ----

    @Test @Config(qualifiers = "w320dp-h568dp-xhdpi")
    fun ring_smallPhone_normalFont() { showRing(1.0f); assertRingControlsReachable() }

    @Test @Config(qualifiers = "w320dp-h568dp-xhdpi")
    fun ring_smallPhone_largestFont() { showRing(2.0f); assertRingControlsReachable() }

    @Test @Config(qualifiers = "w360dp-h640dp-xhdpi")
    fun ring_commonPhone_largeFont() { showRing(1.3f); assertRingControlsReachable() }

    @Test @Config(qualifiers = "w800dp-h360dp-xhdpi")
    fun ring_phoneLandscape_normalFont() { showRing(1.0f); assertRingControlsReachable() }

    @Test @Config(qualifiers = "w800dp-h360dp-xhdpi")
    fun ring_phoneLandscape_largestFont() { showRing(2.0f); assertRingControlsReachable() }

    @Test @Config(qualifiers = "w1000dp-h700dp-mdpi")
    fun ring_tablet() { showRing(1.0f); assertRingControlsReachable() }

    // ---- Onboarding: the pinned action button must never be pushed off-screen ----

    @Test @Config(qualifiers = "w320dp-h568dp-xhdpi")
    fun welcome_smallPhone_largestFont() {
        show(2.0f) { WelcomeScreen(onBegin = {}) }
        rule.onNodeWithText("Begin Your Journey").assertIsDisplayed()
    }

    @Test @Config(qualifiers = "w320dp-h568dp-xhdpi")
    fun religionSelect_smallPhone_largestFont() {
        show(2.0f) { ReligionSelectScreen(selectedReligionId = null, onSelect = {}, onContinue = {}) }
        rule.onNodeWithText("Continue", substring = true).assertIsDisplayed()
    }

    @Test @Config(qualifiers = "w800dp-h360dp-xhdpi")
    fun religionSelect_phoneLandscape() {
        show(1.0f) { ReligionSelectScreen(selectedReligionId = null, onSelect = {}, onContinue = {}) }
        rule.onNodeWithText("Continue", substring = true).assertIsDisplayed()
    }
}
