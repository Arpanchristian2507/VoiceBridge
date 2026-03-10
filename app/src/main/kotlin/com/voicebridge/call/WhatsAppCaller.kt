package com.voicebridge.call

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.voicebridge.R

/**
 * WhatsAppCaller is responsible for launching a WhatsApp chat with the
 * configured emergency contact using the wa.me deep-link format.
 *
 * The wa.me link opens the WhatsApp (or WhatsApp Business) chat screen directly,
 * from where the user can immediately initiate a video call. This reduces the
 * number of steps a visually impaired user must perform to reach the call screen.
 *
 * The emergency contact phone number is stored in
 * `res/values/strings.xml` under the key `emergency_contact_number` so that
 * it can be changed without modifying source code.
 */
object WhatsAppCaller {

    /**
     * Opens WhatsApp (or WhatsApp Business if installed) and navigates directly
     * to the chat with the number defined in `res/values/strings.xml` as
     * `emergency_contact_number`.
     *
     * The ACTION_VIEW intent with a wa.me URI is handled by both WhatsApp and
     * WhatsApp Business, so whichever is installed will respond to the intent.
     *
     * @param context The context used to start the activity and read resources.
     */
    fun startWhatsAppCall(context: Context) {
        val number = context.getString(R.string.emergency_contact_number)
        val uri = Uri.parse("https://wa.me/$number")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            // FLAG_ACTIVITY_NEW_TASK is required when starting an activity from
            // a non-activity context (e.g., a Service or AccessibilityService)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
