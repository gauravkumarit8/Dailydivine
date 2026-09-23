package com.dailydivine.app.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

/** F005: wraps Android's native TextToSpeech engine (works fully offline,
 *  per F005-R05, once the device's language voice data is installed). */
class TTSManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var onDoneCallback: (() -> Unit)? = null

    fun initialize(onReady: () -> Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                // Real completion tracking rather than the caller having to
                // guess when speech actually finishes. onDone/onError fire
                // on a background (TTS engine) thread; StateFlow.value
                // assignment is thread-safe, so callers can update Compose
                // state directly from this callback without an explicit
                // dispatcher switch.
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        onDoneCallback?.invoke()
                    }
                    @Deprecated("Deprecated in Java, still the only overload available on minSdk 26")
                    override fun onError(utteranceId: String?) {
                        onDoneCallback?.invoke()
                    }
                })
                onReady()
            }
        }
    }

    /** @param onDone called once speech genuinely finishes (or errors) --
     *  replaces the previous "fire and forget" behavior where callers had
     *  no way to know when playback actually ended. */
    fun speak(text: String, language: Locale, rate: Float = 1.0f, pitch: Float = 1.0f, onDone: (() -> Unit)? = null) {
        if (!isInitialized) return
        onDoneCallback = onDone
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
