package com.voicebridge

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.voicebridge.call.WhatsAppCaller
import com.voicebridge.databinding.ActivityMainBinding
import com.voicebridge.service.VoiceCommandService
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
 *
 * On launch it requests the RECORD_AUDIO permission (required by
 * [VoiceCommandService]) and starts the foreground service once the permission
 * is granted.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var ttsManager: TTSManager

    /** Launcher that requests the RECORD_AUDIO permission and starts the service on grant. */
    private val requestAudioPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startVoiceCommandService()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialise Text-to-Speech so the app can give spoken feedback
        ttsManager = TTSManager(this)

        // Request RECORD_AUDIO permission and start the voice recognition service
        requestAudioPermissionAndStartService()

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
     * Checks whether RECORD_AUDIO has already been granted. If yes, starts the
     * [VoiceCommandService] immediately; otherwise asks the user for the permission.
     */
    private fun requestAudioPermissionAndStartService() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startVoiceCommandService()
        } else {
            requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    /** Starts [VoiceCommandService] as a foreground service. */
    private fun startVoiceCommandService() {
        val intent = Intent(this, VoiceCommandService::class.java)
        startForegroundService(intent)
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
