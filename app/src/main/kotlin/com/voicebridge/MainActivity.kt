package com.voicebridge

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.voicebridge.call.WhatsAppCaller
import com.voicebridge.databinding.ActivityMainBinding
import com.voicebridge.service.VoiceCommandService
import com.voicebridge.utils.ContactPreferences
import com.voicebridge.utils.TTSManager
import com.voicebridge.utils.VibrationHelper

/**
 * MainActivity is the entry point of the VoiceBridge application.
 *
 * It provides:
 *  - A settings form where the user can configure the contact name,
 *    phone number, and trigger phrase that drive the rest of the app.
 *  - A button to open the system Accessibility Settings so the user can enable
 *    the WhatsAppAccessibilityService (required for volume-button detection).
 *  - A test button to manually trigger the WhatsApp call flow, useful for
 *    prototyping and verifying the setup without needing to say the trigger phrase.
 *
 * On launch it requests the RECORD_AUDIO permission (required by
 * [VoiceCommandService]) and starts the foreground service once granted.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var ttsManager: TTSManager

    /** Launcher that requests RECORD_AUDIO and starts the service on grant. */
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

        ttsManager = TTSManager(this)

        // Populate the form with any previously saved settings
        loadSavedSettings()

        // Request RECORD_AUDIO and start listening service
        requestAudioPermissionAndStartService()

        binding.btnSaveSettings.setOnClickListener { saveSettings() }

        binding.btnOpenAccessibilitySettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        binding.btnTestCall.setOnClickListener { triggerCall() }
    }

    // -------------------------------------------------------------------------
    // Settings helpers
    // -------------------------------------------------------------------------

    /** Reads saved preferences and fills the form fields. */
    private fun loadSavedSettings() {
        binding.etContactName.setText(ContactPreferences.getContactName(this))
        binding.etPhoneNumber.setText(ContactPreferences.getPhoneNumber(this))
        binding.etTriggerPhrase.setText(ContactPreferences.getTriggerPhrase(this))
        refreshStatusLabel()
    }

    /**
     * Validates and persists the settings entered in the form, then restarts
     * [VoiceCommandService] so it picks up the new trigger phrase immediately.
     */
    private fun saveSettings() {
        val name = binding.etContactName.text.toString().trim()
        val number = binding.etPhoneNumber.text.toString().trim()
        val phrase = binding.etTriggerPhrase.text.toString().trim()

        if (number.isBlank()) {
            Toast.makeText(this, getString(R.string.error_phone_required), Toast.LENGTH_SHORT).show()
            return
        }

        ContactPreferences.saveContactName(this, name)
        ContactPreferences.savePhoneNumber(this, number)
        ContactPreferences.saveTriggerPhrase(
            this,
            phrase.ifBlank { ContactPreferences.DEFAULT_TRIGGER_PHRASE }
        )

        refreshStatusLabel()
        Toast.makeText(this, getString(R.string.toast_settings_saved), Toast.LENGTH_SHORT).show()

        // Restart the service so the new trigger phrase takes effect immediately
        restartVoiceCommandService()
    }

    /** Updates the status label beneath the Save button to show the current config. */
    private fun refreshStatusLabel() {
        val number = ContactPreferences.getPhoneNumber(this)
        binding.tvSettingsStatus.text = if (number.isNotBlank()) {
            val name = ContactPreferences.getContactName(this).ifBlank { number }
            val phrase = ContactPreferences.getTriggerPhrase(this)
            getString(R.string.settings_status_configured, phrase, name)
        } else {
            getString(R.string.settings_status_not_configured)
        }
    }

    // -------------------------------------------------------------------------
    // Service helpers
    // -------------------------------------------------------------------------

    private fun requestAudioPermissionAndStartService() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startVoiceCommandService()
        } else {
            requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startVoiceCommandService() {
        startForegroundService(Intent(this, VoiceCommandService::class.java))
    }

    /**
     * Stops the running [VoiceCommandService] and restarts it so it re-reads
     * the latest trigger phrase from [ContactPreferences].
     */
    private fun restartVoiceCommandService() {
        stopService(Intent(this, VoiceCommandService::class.java))
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startVoiceCommandService()
        }
    }

    // -------------------------------------------------------------------------
    // Call trigger
    // -------------------------------------------------------------------------

    /**
     * Manually triggers the WhatsApp call flow with spoken and haptic feedback.
     * Used by the test button; identical to what the voice command or volume
     * button long-press triggers.
     */
    private fun triggerCall() {
        val contactName = ContactPreferences.getContactName(this)
        val message = if (contactName.isNotBlank()) {
            getString(R.string.tts_calling_name, contactName)
        } else {
            getString(R.string.tts_calling)
        }
        ttsManager.speak(message)
        VibrationHelper.vibrate(this)
        WhatsAppCaller.startWhatsAppCall(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
    }
}
