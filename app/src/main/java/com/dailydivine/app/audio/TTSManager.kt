package com.dailydivine.app.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

/** F005: wraps Android's native TextToSpeech engine (works fully offline,
 *  per F005-R05, once the device's language voice data is installed). */
/** Outcome of [TTSManager.speak]: the UI must be able to tell the user WHY nothing played. */
enum class SpeakResult { STARTED, ENGINE_NOT_READY, LANGUAGE_UNAVAILABLE }

class TTSManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    @Volatile private var isInitialized = false
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
    fun speak(
        text: String, language: Locale, rate: Float = 1.0f, pitch: Float = 1.0f, onDone: (() -> Unit)? = null
    ): SpeakResult {
        val engine = tts
        if (!isInitialized || engine == null) return SpeakResult.ENGINE_NOT_READY
        // F005-R06: setLanguage returns an error code instead of throwing when the
        // voice data isn't installed; previously that failed silently (no sound).
        val langResult = engine.setLanguage(language)
        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            return SpeakResult.LANGUAGE_UNAVAILABLE
        }
        onDoneCallback = onDone
        engine.setSpeechRate(rate)
        engine.setPitch(pitch)
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "verse_id")
        return SpeakResult.STARTED
    }

    fun stop() { tts?.stop() }

    fun shutdown() {
        tts?.shutdown()
        isInitialized = false
    }

    fun isSpeaking(): Boolean = tts?.isSpeaking == true
}
