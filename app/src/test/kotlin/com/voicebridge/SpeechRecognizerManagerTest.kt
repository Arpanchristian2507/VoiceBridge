package com.voicebridge

import com.voicebridge.voice.SpeechRecognizerManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JVM unit tests for [SpeechRecognizerManager.Companion.isTriggerPhrase].
 *
 * These tests run entirely on the JVM (no Android framework required) and verify
 * that the trigger-phrase matching logic correctly identifies the configured phrase
 * in various forms of recognised text.
 *
 * The two-argument overload `isTriggerPhrase(text, phrase)` is tested against
 * both the default phrase ("call arpan") and custom user-defined phrases so that
 * the user-configurable trigger feature is fully covered.
 */
class SpeechRecognizerManagerTest {

    // -------------------------------------------------------------------------
    // Default phrase ("call arpan") tests — backward-compatible single-arg calls
    // -------------------------------------------------------------------------

    @Test
    fun `isTriggerPhrase returns true for exact lowercase phrase`() {
        assertTrue(SpeechRecognizerManager.isTriggerPhrase("call arpan"))
    }

    @Test
    fun `isTriggerPhrase returns true for mixed case`() {
        assertTrue(SpeechRecognizerManager.isTriggerPhrase("Call Arpan"))
    }

    @Test
    fun `isTriggerPhrase returns true for all uppercase`() {
        assertTrue(SpeechRecognizerManager.isTriggerPhrase("CALL ARPAN"))
    }

    @Test
    fun `isTriggerPhrase returns true when phrase is embedded in longer text`() {
        assertTrue(SpeechRecognizerManager.isTriggerPhrase("please call arpan right now"))
    }

    @Test
    fun `isTriggerPhrase returns true with leading and trailing whitespace`() {
        assertTrue(SpeechRecognizerManager.isTriggerPhrase("  call arpan  "))
    }

    @Test
    fun `isTriggerPhrase returns false for unrelated text`() {
        assertFalse(SpeechRecognizerManager.isTriggerPhrase("hello world"))
    }

    @Test
    fun `isTriggerPhrase returns false for empty string`() {
        assertFalse(SpeechRecognizerManager.isTriggerPhrase(""))
    }

    @Test
    fun `isTriggerPhrase returns false for partial phrase`() {
        assertFalse(SpeechRecognizerManager.isTriggerPhrase("call"))
    }

    @Test
    fun `isTriggerPhrase returns false for reversed phrase`() {
        assertFalse(SpeechRecognizerManager.isTriggerPhrase("arpan call"))
    }

    // -------------------------------------------------------------------------
    // Custom user-defined trigger phrase tests
    // -------------------------------------------------------------------------

    @Test
    fun `isTriggerPhrase returns true for custom phrase exact match`() {
        assertTrue(SpeechRecognizerManager.isTriggerPhrase("hey google", "hey google"))
    }

    @Test
    fun `isTriggerPhrase returns true for custom phrase mixed case`() {
        assertTrue(SpeechRecognizerManager.isTriggerPhrase("Hey Google", "hey google"))
    }

    @Test
    fun `isTriggerPhrase returns true for custom phrase embedded in sentence`() {
        assertTrue(SpeechRecognizerManager.isTriggerPhrase("ok please call mom now", "call mom"))
    }

    @Test
    fun `isTriggerPhrase returns false when text does not contain custom phrase`() {
        assertFalse(SpeechRecognizerManager.isTriggerPhrase("call arpan", "call mom"))
    }

    @Test
    fun `isTriggerPhrase trims whitespace in custom phrase`() {
        assertTrue(SpeechRecognizerManager.isTriggerPhrase("emergency call", "  emergency call  "))
    }
}
