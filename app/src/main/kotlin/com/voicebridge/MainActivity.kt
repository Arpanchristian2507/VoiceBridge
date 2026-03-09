package com.voicebridge

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.voicebridge.call.WhatsAppCaller
import com.voicebridge.databinding.ActivityMainBinding
import com.voicebridge.utils.TTSManager
import com.voicebridge.utils.VibrationHelper

/**
 * MainActivity is the entry point of the VoiceBridge application.
 *
 * It provides:
 *  - A button to open the system Accessibility Settings so the user can enable
 *    the WhatsAppAccessibilityService (required for volume-button detection).
 *  - A test button to manually trigger the WhatsApp call flow, useful during
 *    development and for sighted caregivers setting up the device.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var ttsManager: TTSManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialise Text-to-Speech so the app can give spoken feedback
        ttsManager = TTSManager(this)

        // Open the system Accessibility Settings screen so the user can enable
        // the VoiceBridge accessibility service
        binding.btnOpenAccessibilitySettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }

        // Manually trigger the call flow – useful for testing without needing
        // the volume-button trigger or voice command
        binding.btnTestCall.setOnClickListener {
            triggerCall()
        }
    }

    /**
     * Triggers the WhatsApp call flow with spoken and haptic feedback.
     * This is the same action that is invoked by the voice command or
     * the volume-button long-press trigger.
     */
    private fun triggerCall() {
        ttsManager.speak(getString(R.string.tts_calling))
        VibrationHelper.vibrate(this)
        WhatsAppCaller.startWhatsAppCall(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
    }
}
