package com.voicebridge.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

/**
 * TTSManager is a wrapper around Android's [TextToSpeech] engine.
 *
 * It initialises the TTS engine asynchronously and exposes a simple [speak]
 * function that the rest of the application can call without worrying about
 * engine readiness. Messages queued before the engine is ready are dropped
 * gracefully with a log warning.
 *
 * Usage:
 * ```
 * val tts = TTSManager(context)
 * tts.speak("Calling Arpan")
 * // …
 * tts.shutdown() // call in onDestroy or when no longer needed
 * ```
 */
class TTSManager(context: Context) : TextToSpeech.OnInitListener {

    private val tts: TextToSpeech = TextToSpeech(context, this)
    private var isReady = false

    companion object {
        private const val TAG = "TTSManager"
    }

    /**
     * Called by the [TextToSpeech] engine once initialisation is complete.
     * Sets [isReady] so that subsequent [speak] calls can proceed.
     */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.getDefault())
            isReady = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
            if (!isReady) {
                Log.w(TAG, "TTS language not supported, falling back.")
                // Fallback to English if the device locale is not supported
                tts.setLanguage(Locale.ENGLISH)
                isReady = true
            }
        } else {
            Log.e(TAG, "TextToSpeech initialisation failed with status: $status")
        }
    }

    /**
     * Speaks the given [message] using the TTS engine.
     *
     * Uses [TextToSpeech.QUEUE_FLUSH] so each new utterance immediately
     * replaces any previously queued speech, keeping feedback snappy.
     *
     * @param message The text to be spoken aloud.
     */
    fun speak(message: String) {
        if (isReady) {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Log.w(TAG, "TTS not ready yet, dropping message: $message")
        }
    }

    /**
     * Releases TTS engine resources. Should be called when the component that
     * owns this manager is destroyed (e.g., in Activity.onDestroy or
     * Service.onDestroy).
     */
    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
