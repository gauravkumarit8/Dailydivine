package com.dailydivine.app.alarm

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RearmTriggersTest {

    @Test fun bootAndUpdateRearm() {
        assertTrue(RearmTriggers.isRearmAction("android.intent.action.BOOT_COMPLETED"))
        assertTrue(RearmTriggers.isRearmAction("android.intent.action.MY_PACKAGE_REPLACED"))
    }

    @Test fun timeZoneAndClockChangesRearm() {
        assertTrue(RearmTriggers.isRearmAction("android.intent.action.TIMEZONE_CHANGED"))
        assertTrue(RearmTriggers.isRearmAction("android.intent.action.TIME_SET"))
    }

    @Test fun exactAlarmPermissionChangeRearms() {
        assertTrue(
            RearmTriggers.isRearmAction("android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED")
        )
    }

    @Test fun unrelatedAndNullActionsAreIgnored() {
        assertFalse(RearmTriggers.isRearmAction(null))
        assertFalse(RearmTriggers.isRearmAction(""))
        assertFalse(RearmTriggers.isRearmAction("android.intent.action.SCREEN_ON"))
        assertFalse(RearmTriggers.isRearmAction("com.example.FAKE_BOOT"))
    }
}
