package com.dailydivine.app.audio

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

/** F005: wraps Android's native TextToSpeech engine (works fully offline,
 *  per F005-R05, once the device's language voice data is installed). */
/** Outcome of [TTSManager.speak]: the UI must be able to tell the user WHY nothing played. */
enum class SpeakResult { STARTED, ENGINE_NOT_READY, LANGUAGE_UNAVAILABLE }

class TTSManager(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    // Bumped by every new request and by stop(): a late callback from an older request is ignored.
    @Volatile private var generation = 0
    private var queue: List<Pair<String, Locale>> = emptyList()

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
                    override fun onDone(utteranceId: String?) = partFinished(utteranceId, failed = false)
                    @Deprecated("Deprecated in Java, still the only overload available on minSdk 26")
                    override fun onError(utteranceId: String?) = partFinished(utteranceId, failed = true)
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
        generation++
        mainHandler.removeCallbacksAndMessages(null)
        queue = listOf(text to language)
        onDoneCallback = onDone
        engine.setSpeechRate(rate)
        engine.setPitch(pitch)
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "part_${generation}_0")
        return SpeakResult.STARTED
    }

    /**
     * Reads [parts] one after the other, each with its own language (the Sanskrit of a verse, then its
     * meaning). A part whose voice is not installed is skipped; but if the LAST part (the meaning the
     * user chose) has no voice, nothing is read and [SpeakResult.LANGUAGE_UNAVAILABLE] says why, rather
     * than reading only the Sanskrit and leaving the user wondering.
     */
    fun speakParts(
        parts: List<SpeechPart>, rate: Float = 1.0f, pitch: Float = 1.0f, pauseMs: Long = 700L, onDone: (() -> Unit)? = null
    ): SpeakResult {
        val engine = tts
        if (!isInitialized || engine == null) return SpeakResult.ENGINE_NOT_READY
        val withLocales = parts.filter { it.text.isNotBlank() }.map { it.text to Locale.forLanguageTag(it.languageTag) }
        if (withLocales.isEmpty()) return SpeakResult.LANGUAGE_UNAVAILABLE
        fun playable(locale: Locale) = engine.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE
        if (!playable(withLocales.last().second)) return SpeakResult.LANGUAGE_UNAVAILABLE
        val toPlay = withLocales.filter { playable(it.second) }
        generation++
        mainHandler.removeCallbacksAndMessages(null)
        queue = toPlay
        pauseBetweenMs = pauseMs
        onDoneCallback = onDone
        engine.setSpeechRate(rate)
        engine.setPitch(pitch)
        playPart(0, generation)
        return SpeakResult.STARTED
    }

    private var pauseBetweenMs = 700L

    private fun playPart(index: Int, gen: Int) {
        val engine = tts ?: return
        val (text, locale) = queue.getOrNull(index) ?: return
        if (gen != generation) return
        engine.setLanguage(locale)
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "part_${gen}_$index")
    }

    /** Callback thread is the engine's: hop to the main thread, then either play the next part or finish. */
    private fun partFinished(utteranceId: String?, failed: Boolean) {
        val bits = utteranceId?.removePrefix("part_")?.split('_') ?: return
        val gen = bits.getOrNull(0)?.toIntOrNull() ?: return
        val index = bits.getOrNull(1)?.toIntOrNull() ?: return
        mainHandler.post {
            if (gen != generation) return@post // stopped, or a newer request took over
            if (!failed && index + 1 < queue.size) {
                mainHandler.postDelayed({ playPart(index + 1, gen) }, pauseBetweenMs)
            } else {
                onDoneCallback?.invoke()
            }
        }
    }

    fun stop() {
        generation++
        mainHandler.removeCallbacksAndMessages(null)
        tts?.stop()
    }

    fun shutdown() {
        tts?.shutdown()
        isInitialized = false
    }

    fun isSpeaking(): Boolean = tts?.isSpeaking == true
}
