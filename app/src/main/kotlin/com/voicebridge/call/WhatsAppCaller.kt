package com.voicebridge.call

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.voicebridge.utils.ContactPreferences

/**
 * WhatsAppCaller is responsible for launching a WhatsApp chat with the
 * user-configured emergency contact using the wa.me deep-link format.
 *
 * The wa.me link opens the WhatsApp (or WhatsApp Business) chat screen directly,
 * from where the user can immediately initiate a video call. This reduces the
 * number of steps a visually impaired user must perform to reach the call screen.
 *
 * The emergency contact phone number is stored in [ContactPreferences] and
 * configured by the user through the app's settings UI.
 */
object WhatsAppCaller {

    private const val TAG = "WhatsAppCaller"

    /**
     * Opens WhatsApp (or WhatsApp Business if installed) and navigates directly
     * to the chat with the number saved in [ContactPreferences].
     *
     * If no phone number has been configured yet this call is a no-op (it logs
     * a warning) so the app does not crash during initial setup.
     *
     * The ACTION_VIEW intent with a wa.me URI is handled by both WhatsApp and
     * WhatsApp Business, so whichever is installed will respond to the intent.
     *
     * @param context The context used to start the activity and read preferences.
     */
    fun startWhatsAppCall(context: Context) {
        val number = ContactPreferences.getPhoneNumber(context)
        if (number.isBlank()) {
            Log.w(TAG, "No phone number configured. Open the app and set a contact number.")
            return
        }
        val uri = Uri.parse("https://wa.me/$number")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            // FLAG_ACTIVITY_NEW_TASK is required when starting an activity from
            // a non-activity context (e.g., a Service or AccessibilityService)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
