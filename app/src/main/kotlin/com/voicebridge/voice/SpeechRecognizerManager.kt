package com.voicebridge.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

/**
 * SpeechRecognizerManager wraps Android's [SpeechRecognizer] to listen
 * continuously for a configurable trigger phrase.
 *
 * The recogniser is restarted automatically after each result or recoverable
 * error so that the app stays in a perpetual listening state while the service
 * is running.
 *
 * Responsibilities:
 *  - Initialise and configure the speech recogniser.
 *  - Start and stop listening sessions.
 *  - Parse recognition results and detect the trigger phrase.
 *  - Invoke a callback when the phrase is detected.
 *
 * @param context Application or service context used to create the recogniser.
 * @param triggerPhrase The phrase to listen for (case-insensitive). Defaults to
 *   [DEFAULT_TRIGGER_PHRASE] if not supplied.
 * @param onPhraseDetected Callback invoked when the trigger phrase is detected.
 */
class SpeechRecognizerManager(
    private val context: Context,
    private val triggerPhrase: String = DEFAULT_TRIGGER_PHRASE,
    private val onPhraseDetected: () -> Unit
) {

    private var speechRecognizer: SpeechRecognizer? = null

    /** Tracks whether continuous listening should remain active. */
    private var isListening = false

    /** Delay (ms) before retrying after a recognition error, to avoid rapid loops. */
    private val errorRetryDelayMs = 1_000L

    private val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        private const val TAG = "SpeechRecognizerManager"

        /** Default trigger phrase used when none has been user-configured. */
        const val DEFAULT_TRIGGER_PHRASE = "call arpan"

        /**
         * Checks whether [recognisedText] contains [triggerPhrase] (case-insensitive).
         *
         * Exposed as a companion-object function so it can be unit-tested on
         * the JVM without requiring an Android Context or a live recogniser.
         *
         * @param recognisedText The best-guess text returned by the recogniser.
         * @param triggerPhrase  The phrase to search for; defaults to [DEFAULT_TRIGGER_PHRASE].
         * @return `true` if the trigger phrase was found.
         */
        fun isTriggerPhrase(
            recognisedText: String,
            triggerPhrase: String = DEFAULT_TRIGGER_PHRASE
        ): Boolean {
            return recognisedText.trim().lowercase()
                .contains(triggerPhrase.trim().lowercase())
        }
    }

    /**
     * Initialises the speech recogniser engine.
     *
     * Checks [SpeechRecognizer.isRecognitionAvailable] before creating the
     * recogniser to handle devices without a recognition service gracefully.
     */
    fun init() {
        Log.d(TAG, "init() called")
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(buildRecognitionListener())
        } else {
            Log.w(TAG, "Speech recognition is not available on this device.")
        }
    }

    /**
     * Starts a continuous listening session.
     *
     * Builds a [RecognizerIntent] with [RecognizerIntent.ACTION_RECOGNIZE_SPEECH]
     * and calls [SpeechRecognizer.startListening]. The listener will restart
     * recognition automatically after each result so that the app remains ready
     * for the trigger phrase.
     */
    fun startListening() {
        Log.d(TAG, "startListening() called")
        isListening = true
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer?.startListening(intent)
    }

    /**
     * Stops the current listening session.
     *
     * Sets [isListening] to `false` so that the automatic restart logic inside
     * [buildRecognitionListener] does not restart recognition after the session ends.
     */
    fun stopListening() {
        Log.d(TAG, "stopListening() called")
        isListening = false
        mainHandler.removeCallbacksAndMessages(null)
        speechRecognizer?.stopListening()
    }

    /**
     * Releases all resources held by the recogniser.
     *
     * After calling this method the manager cannot be reused without a fresh
     * call to [init].
     */
    fun destroy() {
        Log.d(TAG, "destroy() called")
        isListening = false
        mainHandler.removeCallbacksAndMessages(null)
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Builds a [RecognitionListener] that:
     *  - Calls [isTriggerPhrase] on every full result and, for responsiveness,
     *    on partial results too.
     *  - Invokes [onPhraseDetected] as soon as the trigger phrase is found.
     *  - Restarts recognition automatically so listening is continuous.
     */
    private fun buildRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {

            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "onReadyForSpeech")
            }

            override fun onBeginningOfSpeech() {
                Log.d(TAG, "onBeginningOfSpeech")
            }

            override fun onRmsChanged(rmsdB: Float) {
                // no-op: RMS changes are too frequent to log
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // no-op
            }

            override fun onEndOfSpeech() {
                Log.d(TAG, "onEndOfSpeech")
            }

            override fun onError(error: Int) {
                Log.w(TAG, "onError: $error")
                // Delay the restart to avoid a rapid error loop if recognition
                // fails repeatedly (e.g., due to transient network issues).
                if (isListening) {
                    mainHandler.postDelayed({
                        if (isListening) startListening()
                    }, errorRetryDelayMs)
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                Log.d(TAG, "onResults: $matches")
                // If isListening was cleared by onPartialResults (trigger already
                // detected in that session), skip re-processing to avoid duplicates.
                if (!isListening) return
                if (matches != null) {
                    for (result in matches) {
                        if (isTriggerPhrase(result, triggerPhrase)) {
                            Log.d(TAG, "Trigger phrase detected: $result")
                            // Stop listening before invoking the callback so the
                            // recogniser does not restart while the call flow runs.
                            stopListening()
                            onPhraseDetected()
                            return
                        }
                    }
                }
                // No trigger phrase – restart for continuous listening
                if (isListening) {
                    startListening()
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                Log.d(TAG, "onPartialResults: $matches")
                if (matches != null) {
                    for (result in matches) {
                        if (isTriggerPhrase(result, triggerPhrase)) {
                            Log.d(TAG, "Trigger phrase detected in partial results: $result")
                            // Stop listening immediately so onResults for this same
                            // utterance does not fire onPhraseDetected a second time.
                            stopListening()
                            onPhraseDetected()
                            return
                        }
                    }
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // no-op
            }
        }
    }
}
