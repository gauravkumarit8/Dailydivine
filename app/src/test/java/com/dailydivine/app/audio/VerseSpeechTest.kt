package com.dailydivine.app.audio

import org.junit.Assert.assertEquals
import org.junit.Test

class VerseSpeechTest {
    private val sanskrit = "\u0938\u092E\u094B\u093D\u0939\u0902 \u0938\u0930\u094D\u0935\u092D\u0942\u0924\u0947\u0937\u0941\u00A0\u0964\n\u092F\u0947 \u092D\u091C\u0928\u094D\u0924\u093F\u00A0\u0965"

    @Test fun `a verse with Sanskrit is read Sanskrit first then the meaning in the chosen language`() {
        val plan = VerseSpeech.plan(sanskrit, "I am the same to all beings.", "en")
        assertEquals(2, plan.size)
        assertEquals("hi", plan[0].languageTag)
        assertEquals("I am the same to all beings.", plan[1].text)
        assertEquals("en", plan[1].languageTag)
    }

    @Test fun `a Hindi reader hears the Sanskrit and then the Hindi meaning`() {
        val plan = VerseSpeech.plan(sanskrit, "\u092E\u0948\u0902 \u0938\u092C \u092E\u0947\u0902 \u0938\u092E\u093E\u0928 \u0939\u0942\u0901\u0964", "hi")
        assertEquals(listOf("hi", "hi"), plan.map { it.languageTag })
    }

    @Test fun `a verse without Sanskrit is read as before`() {
        val plan = VerseSpeech.plan("", "Never the spirit was born.", "en")
        assertEquals(listOf(SpeechPart("Never the spirit was born.", "en")), plan)
    }

    @Test fun `display-only characters are removed from the spoken Sanskrit`() {
        val spoken = VerseSpeech.plan(sanskrit, "x", "en")[0].text
        assertEquals(false, spoken.contains('\u00A0'))
        assertEquals(false, spoken.contains('\n'))
        assertEquals(false, spoken.contains('\u0964'))
        assertEquals(false, spoken.contains('\u0965'))
        assertEquals(true, spoken.endsWith("."))
    }
}
