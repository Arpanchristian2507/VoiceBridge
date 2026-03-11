package com.voicebridge.call

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * WhatsAppCaller is responsible for launching a WhatsApp chat with a predefined
 * emergency contact using the wa.me deep-link format.
 *
 * To achieve a "direct call" experience, this class works in tandem with
 * WhatsAppAccessibilityService. It sets a [isCallPending] flag so that the
 * accessibility service knows to automatically click the "Call" button once
 * the chat window opens.
 */
object WhatsAppCaller {

    /**
     * The predefined emergency contact phone number in international format.
     * Change this value to the trusted contact's actual number.
     */
    const val EMERGENCY_CONTACT_NUMBER = "+1234567890" // TODO: Replace with the real contact number

    /**
     * Flag used to signal the Accessibility Service that a call has been
     * requested and it should attempt to click the call button automatically.
     */
    var isCallPending = false

    /**
     * Opens WhatsApp and navigates to the chat with [EMERGENCY_CONTACT_NUMBER].
     * Sets [isCallPending] to true so the Accessibility Service can automate
     * the final click to start the call.
     *
     * @param context The context used to start the activity.
     */
    fun startWhatsAppCall(context: Context) {
        isCallPending = true
        
        val uri = Uri.parse("https://wa.me/$EMERGENCY_CONTACT_NUMBER")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
