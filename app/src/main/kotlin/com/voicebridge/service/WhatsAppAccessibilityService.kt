package com.voicebridge.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.voicebridge.R
import com.voicebridge.call.WhatsAppCaller
import com.voicebridge.utils.TTSManager
import com.voicebridge.utils.VibrationHelper

/**
 * WhatsAppAccessibilityService extends [AccessibilityService] to detect a
 * long-press of the Volume Up hardware button.
 *
 * When the button is held for [LONG_PRESS_THRESHOLD_MS] (3 seconds) the service
 * triggers the WhatsApp call flow, providing spoken and haptic feedback.
 *
 * To function correctly this service must be enabled by the user in:
 *   Settings → Accessibility → VoiceBridge → VoiceBridge Accessibility Service
 *
 * The accessibility service configuration is defined in
 * res/xml/accessibility_service_config.xml with the flag
 * `canRequestFilterKeyEvents` set to `true`, which is required to receive
 * hardware key events.
 */
class WhatsAppAccessibilityService : AccessibilityService() {

    /** Timestamp (ms) when the Volume Up key was first pressed. */
    private var volumeUpPressedAt: Long = 0L

    /** Whether the Volume Up key is currently being held down. */
    private var isVolumeUpHeld: Boolean = false

    private lateinit var ttsManager: TTSManager

    companion object {
        private const val TAG = "WhatsAppA11yService"

        /** Minimum hold duration (ms) required to trigger the call flow. */
        const val LONG_PRESS_THRESHOLD_MS = 3_000L
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        ttsManager = TTSManager(this)
        Log.d(TAG, "WhatsAppAccessibilityService connected")
    }

    /**
     * Intercepts hardware key events.
     *
     * - On [KeyEvent.ACTION_DOWN] for [KeyEvent.KEYCODE_VOLUME_UP] the start
     *   time is recorded.
     * - On [KeyEvent.ACTION_UP] the elapsed duration is calculated. If it
     *   exceeds [LONG_PRESS_THRESHOLD_MS] the call flow is triggered.
     *
     * Returning `true` consumes the event so the system does not also change
     * the volume when the long-press is detected.
     * Returning `false` passes the event to the system normally for short presses.
     *
     * @param event The key event delivered by the system.
     * @return `true` if the event was consumed, `false` otherwise.
     */
    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode != KeyEvent.KEYCODE_VOLUME_UP) {
            // Not the volume-up button – do not consume the event
            return false
        }

        return when (event.action) {
            KeyEvent.ACTION_DOWN -> {
                if (!isVolumeUpHeld) {
                    // Record when the button was first pressed
                    volumeUpPressedAt = System.currentTimeMillis()
                    isVolumeUpHeld = true
                }
                // Do not consume on key-down so the volume preview still works
                false
            }

            KeyEvent.ACTION_UP -> {
                val holdDurationMs = System.currentTimeMillis() - volumeUpPressedAt
                isVolumeUpHeld = false

                Log.d(TAG, "Volume Up released after ${holdDurationMs}ms")

                if (holdDurationMs >= LONG_PRESS_THRESHOLD_MS) {
                    Log.d(TAG, "Long press detected – triggering call flow")
                    onLongPressDetected()
                    // Consume the event so the system does not act on the volume key
                    true
                } else {
                    // Short press – let the system handle volume adjustment
                    false
                }
            }

            else -> false
        }
    }

    /**
     * Called when a 3-second Volume Up long-press is confirmed.
     * Provides spoken feedback and launches the WhatsApp call flow.
     */
    private fun onLongPressDetected() {
        ttsManager.speak(getString(R.string.tts_opening_whatsapp))
        VibrationHelper.vibrate(this)
        WhatsAppCaller.startWhatsAppCall(this)
    }

    /**
     * Required override for [AccessibilityService].
     * VoiceBridge does not consume general accessibility events; all logic is
     * driven through hardware key events via [onKeyEvent].
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No-op: this service only uses onKeyEvent for triggering
    }

    /**
     * Required override for [AccessibilityService].
     * Called when the system wants the service to stop processing events.
     */
    override fun onInterrupt() {
        Log.d(TAG, "WhatsAppAccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
    }
}
