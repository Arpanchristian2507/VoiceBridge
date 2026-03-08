package com.voicebridge.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * VibrationHelper provides a simple utility function to trigger haptic feedback.
 *
 * This is used to give the visually impaired user a physical confirmation that
 * their action (voice command or button press) has been detected. The vibration
 * pattern is a single short pulse to avoid being intrusive.
 */
object VibrationHelper {

    /** Duration of the confirmation vibration in milliseconds. */
    private const val VIBRATION_DURATION_MS = 300L

    /**
     * Triggers a short vibration pulse using the device vibrator.
     *
     * Handles the API level difference:
     *  - API 31+: uses [VibratorManager] to retrieve the default vibrator.
     *  - API 26–30: uses the deprecated [Vibrator] service directly.
     *
     * @param context The context used to access the system vibrator service.
     */
    fun vibrate(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    VIBRATION_DURATION_MS,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        VIBRATION_DURATION_MS,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(VIBRATION_DURATION_MS)
            }
        }
    }
}
