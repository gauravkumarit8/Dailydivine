package com.dailydivine.app.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/** F005: wraps Android's native TextToSpeech engine (works fully offline,
 *  per F005-R05, once the device's language voice data is installed). */
class TTSManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    fun initialize(onReady: () -> Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                onReady()
            }
        }
    }

    fun speak(text: String, language: Locale, rate: Float = 1.0f, pitch: Float = 1.0f) {
        if (!isInitialized) return
        tts?.let {
            it.language = language
            it.setSpeechRate(rate)
            it.setPitch(pitch)
            it.speak(text, TextToSpeech.QUEUE_FLUSH, null, "verse_id")
        }
    }

    fun stop() { tts?.stop() }

    fun shutdown() {
        tts?.shutdown()
        isInitialized = false
    }

    fun isSpeaking(): Boolean = tts?.isSpeaking == true
}
