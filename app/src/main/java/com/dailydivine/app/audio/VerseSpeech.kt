package com.dailydivine.app.audio

/** One stretch of speech: the text and the language tag of the voice that should read it. */
data class SpeechPart(val text: String, val languageTag: String)

/**
 * What is read aloud for a verse: the Sanskrit (when the verse has it) first, then the meaning in the
 * language the user chose. Android has no Sanskrit voice, so the Sanskrit is read by a Hindi voice,
 * which reads the same Devanagari script. If the phone has no Hindi voice that part is skipped and
 * the meaning is still read.
 */
object VerseSpeech {
    const val SANSKRIT_VOICE_TAG = "hi"

    fun plan(originalText: String, translatedText: String, languageCode: String): List<SpeechPart> {
        val parts = mutableListOf<SpeechPart>()
        if (hasDevanagari(originalText)) parts += SpeechPart(cleanForSpeech(originalText), SANSKRIT_VOICE_TAG)
        parts += SpeechPart(translatedText, languageCode)
        return parts
    }

    /** The no-break spaces and line breaks of the display text become plain spaces; dandas become pauses. */
    fun cleanForSpeech(sanskrit: String): String =
        sanskrit
            .replace('\u00A0', ' ')
            .replace("\u0965", ".")
            .replace("\u0964", ",")
            .replace('\n', ' ')
            .replace(Regex("\\s+"), " ")
            .replace(" ,", ",")
            .replace(" .", ".")
            .trim()

    private fun hasDevanagari(text: String) = text.any { it in '\u0900'..'\u097F' }
}
