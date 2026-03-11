package com.voicebridge.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.voicebridge.R
import com.voicebridge.call.WhatsAppCaller
import com.voicebridge.utils.TTSManager
import com.voicebridge.utils.VibrationHelper

/**
 * WhatsAppAccessibilityService extends [AccessibilityService] to:
 * 1. Detect a long-press of the Volume Up hardware button to trigger a call.
 * 2. Automatically click the "Call" button once WhatsApp opens.
 */
class WhatsAppAccessibilityService : AccessibilityService() {

    private var volumeUpPressedAt: Long = 0L
    private var isVolumeUpHeld: Boolean = false
    private lateinit var ttsManager: TTSManager

    companion object {
        private const val TAG = "WhatsAppA11yService"
        const val LONG_PRESS_THRESHOLD_MS = 3_000L
        
        // Package names for WhatsApp and WhatsApp Business
        private val WHATSAPP_PACKAGES = listOf("com.whatsapp", "com.whatsapp.w4b")
        
        // Common content descriptions for call buttons in various languages.
        // We include both Video and Voice call options.
        private val CALL_BUTTON_DESCRIPTIONS = listOf(
            "Video call", "Videoanruf", "Appel vidéo", "Llamada de video",
            "Voice call", "Sprachanruf", "Appel vocal", "Llamada de voz",
            "Call", "Anrufen", "Appeler", "Llamar"
        )
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        ttsManager = TTSManager(this)
        Log.d(TAG, "WhatsAppAccessibilityService connected")
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode != KeyEvent.KEYCODE_VOLUME_UP) return false

        return when (event.action) {
            KeyEvent.ACTION_DOWN -> {
                if (!isVolumeUpHeld) {
                    volumeUpPressedAt = System.currentTimeMillis()
                    isVolumeUpHeld = true
                }
                false
            }
            KeyEvent.ACTION_UP -> {
                val holdDurationMs = System.currentTimeMillis() - volumeUpPressedAt
                isVolumeUpHeld = false
                if (holdDurationMs >= LONG_PRESS_THRESHOLD_MS) {
                    onLongPressDetected()
                    true
                } else {
                    false
                }
            }
            else -> false
        }
    }

    private fun onLongPressDetected() {
        ttsManager.speak(getString(R.string.tts_opening_whatsapp))
        VibrationHelper.vibrate(this)
        WhatsAppCaller.startWhatsAppCall(this)
    }

    /**
     * Listen for window changes to automate clicking the call button.
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!WhatsAppCaller.isCallPending) return

        val packageName = event.packageName?.toString()
        if (packageName !in WHATSAPP_PACKAGES) return

        // We check for window state changes or content changes which happen as the chat loads
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || 
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            
            val rootNode = rootInActiveWindow ?: return
            if (tryClickCallButton(rootNode)) {
                Log.d(TAG, "Successfully clicked WhatsApp call button automatically.")
                // Reset the flag so we don't keep clicking if the user stays in the chat
                WhatsAppCaller.isCallPending = false
            }
        }
    }

    /**
     * Recursively searches for the call button and clicks it.
     */
    private fun tryClickCallButton(node: AccessibilityNodeInfo): Boolean {
        val description = node.contentDescription?.toString()
        
        if (description != null) {
            val matches = CALL_BUTTON_DESCRIPTIONS.any { it.equals(description, ignoreCase = true) }
            if (matches) {
                // Found a potential button. Ensure it or its parent is clickable.
                var target: AccessibilityNodeInfo? = node
                while (target != null && !target.isClickable) {
                    target = target.parent
                }
                
                if (target != null && target.isClickable) {
                    target.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    return true
                }
            }
        }

        // Recursively check children
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (tryClickCallButton(child)) return true
        }
        return false
    }

    override fun onInterrupt() {
        Log.d(TAG, "WhatsAppAccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
    }
}
