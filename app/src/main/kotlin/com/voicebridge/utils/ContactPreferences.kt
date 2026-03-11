package com.voicebridge.utils

import android.content.Context

/**
 * ContactPreferences provides a thin wrapper around [android.content.SharedPreferences]
 * to persist the user-configured contact name, phone number, and trigger phrase.
 *
 * All values are stored in a private preference file named `voicebridge_prefs`.
 * Callers should use the static helper functions; no instance creation is required.
 */
object ContactPreferences {

    private const val PREFS_NAME = "voicebridge_prefs"
    private const val KEY_CONTACT_NAME = "contact_name"
    private const val KEY_PHONE_NUMBER = "phone_number"
    private const val KEY_TRIGGER_PHRASE = "trigger_phrase"

    /** Default trigger phrase used when the user has not yet set a custom one. */
    const val DEFAULT_TRIGGER_PHRASE = "call arpan"

    // -------------------------------------------------------------------------
    // Contact name
    // -------------------------------------------------------------------------

    /**
     * Saves the trusted contact's display name.
     * @param context Application or activity context.
     * @param name The contact name to persist.
     */
    fun saveContactName(context: Context, name: String) {
        prefs(context).edit().putString(KEY_CONTACT_NAME, name).apply()
    }

    /**
     * Returns the saved contact name, or an empty string if none has been set.
     */
    fun getContactName(context: Context): String =
        prefs(context).getString(KEY_CONTACT_NAME, "") ?: ""

    // -------------------------------------------------------------------------
    // Phone number
    // -------------------------------------------------------------------------

    /**
     * Saves the trusted contact's phone number in international format
     * (country code + number, no spaces, dashes, or plus sign).
     * @param context Application or activity context.
     * @param number The phone number to persist.
     */
    fun savePhoneNumber(context: Context, number: String) {
        prefs(context).edit().putString(KEY_PHONE_NUMBER, number).apply()
    }

    /**
     * Returns the saved phone number, or an empty string if none has been set.
     */
    fun getPhoneNumber(context: Context): String =
        prefs(context).getString(KEY_PHONE_NUMBER, "") ?: ""

    // -------------------------------------------------------------------------
    // Trigger phrase
    // -------------------------------------------------------------------------

    /**
     * Saves the voice trigger phrase.
     * @param context Application or activity context.
     * @param phrase The phrase to persist; must not be blank.
     */
    fun saveTriggerPhrase(context: Context, phrase: String) {
        prefs(context).edit().putString(KEY_TRIGGER_PHRASE, phrase).apply()
    }

    /**
     * Returns the saved trigger phrase, falling back to [DEFAULT_TRIGGER_PHRASE]
     * if none has been saved yet.
     */
    fun getTriggerPhrase(context: Context): String =
        prefs(context).getString(KEY_TRIGGER_PHRASE, DEFAULT_TRIGGER_PHRASE)
            ?.takeIf { it.isNotBlank() } ?: DEFAULT_TRIGGER_PHRASE

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Returns `true` when a phone number has been configured. */
    fun isConfigured(context: Context): Boolean = getPhoneNumber(context).isNotBlank()

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
