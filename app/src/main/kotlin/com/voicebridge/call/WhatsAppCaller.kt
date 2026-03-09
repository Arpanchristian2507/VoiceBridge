package com.voicebridge.call

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * WhatsAppCaller is responsible for launching a WhatsApp chat with a predefined
 * emergency contact using the wa.me deep-link format.
 *
 * The wa.me link opens the WhatsApp (or WhatsApp Business) chat screen directly,
 * from where the user can immediately initiate a video call. This reduces the
 * number of steps a visually impaired user must perform to reach the call screen.
 */
object WhatsAppCaller {

    /**
     * The predefined emergency contact phone number in international format
     * (country code followed by the number, no spaces, dashes, or plus sign).
     *
     * Change this value to the trusted contact's actual number before deploying.
     */
    const val EMERGENCY_CONTACT_NUMBER = "1234567890" // TODO: Replace with the real contact number

    /**
     * Opens WhatsApp (or WhatsApp Business if installed) and navigates directly
     * to the chat with [EMERGENCY_CONTACT_NUMBER].
     *
     * The ACTION_VIEW intent with a wa.me URI is handled by both WhatsApp and
     * WhatsApp Business, so whichever is installed will respond to the intent.
     *
     * @param context The context used to start the activity.
     */
    fun startWhatsAppCall(context: Context) {
        val uri = Uri.parse("https://wa.me/$EMERGENCY_CONTACT_NUMBER")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            // FLAG_ACTIVITY_NEW_TASK is required when starting an activity from
            // a non-activity context (e.g., a Service or AccessibilityService)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
