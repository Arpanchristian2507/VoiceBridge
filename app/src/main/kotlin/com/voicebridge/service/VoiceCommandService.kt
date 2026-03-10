package com.voicebridge.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationCompat
import com.voicebridge.MainActivity
import com.voicebridge.R
import com.voicebridge.call.WhatsAppCaller
import com.voicebridge.utils.TTSManager
import com.voicebridge.utils.VibrationHelper
import com.voicebridge.voice.SpeechRecognizerManager

/**
 * VoiceCommandService is a foreground service that hosts the voice recognition
 * engine and listens continuously for the trigger phrase "Call Arpan".
 *
 * Running as a foreground service ensures the OS does not kill the process
 * while waiting for the trigger phrase, and it satisfies the
 * FOREGROUND_SERVICE permission requirement for background microphone access.
 *
 * Responsibilities:
 *  - Start and stop [SpeechRecognizerManager] listening sessions.
 *  - Receive the trigger-phrase callback and invoke the call flow.
 *  - Provide spoken feedback via [TTSManager] and haptic feedback via
 *    [VibrationHelper].
 */
class VoiceCommandService : Service() {

    private lateinit var ttsManager: TTSManager
    private lateinit var speechRecognizerManager: SpeechRecognizerManager

    companion object {
        private const val TAG = "VoiceCommandService"
        private const val NOTIFICATION_CHANNEL_ID = "voicebridge_channel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "VoiceCommandService created")

        ttsManager = TTSManager(this)

        // Initialise the speech recogniser; the callback fires when the trigger
        // phrase "Call Arpan" is detected.
        speechRecognizerManager = SpeechRecognizerManager(this) {
            onTriggerPhraseDetected()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "VoiceCommandService started")

        // Promote to foreground so Android keeps the service alive
        startForeground(NOTIFICATION_ID, buildForegroundNotification())

        // Announce that the service is active and ready to listen
        ttsManager.speak(getString(R.string.tts_listening))

        // Initialise the recognition engine
        speechRecognizerManager.init()

        // Start listening only when the RECORD_AUDIO permission has been granted.
        // The permission must be requested by an Activity before starting this service.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            speechRecognizerManager.startListening()
        } else {
            Log.w(TAG, "RECORD_AUDIO permission not granted – skipping startListening()")
        }

        // Return START_STICKY so the OS restarts the service if it is killed
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "VoiceCommandService destroyed")
        speechRecognizerManager.stopListening()
        speechRecognizerManager.destroy()
        ttsManager.shutdown()
    }

    override fun onBind(intent: Intent?): IBinder? {
        // This service does not support binding
        return null
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Called when [SpeechRecognizerManager] detects the trigger phrase.
     * Provides spoken feedback and launches the WhatsApp call flow.
     */
    private fun onTriggerPhraseDetected() {
        Log.d(TAG, "Trigger phrase detected – initiating call flow")
        ttsManager.speak(getString(R.string.tts_calling))
        VibrationHelper.vibrate(this)
        WhatsAppCaller.startWhatsAppCall(this)
    }

    /**
     * Builds the persistent notification required to run as a foreground service.
     * Tapping the notification opens [MainActivity].
     */
    private fun buildForegroundNotification(): Notification {
        createNotificationChannel()

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.tts_listening))
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    /** Creates the notification channel required on API 26+. */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getString(R.string.app_name),
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
