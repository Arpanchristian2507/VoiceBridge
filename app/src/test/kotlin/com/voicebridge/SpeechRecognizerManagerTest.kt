package com.voicebridge

import com.voicebridge.voice.SpeechRecognizerManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JVM unit tests for [SpeechRecognizerManager.Companion.isTriggerPhrase].
 *
 * These tests run entirely on the JVM (no Android framework required) and verify
 * that the trigger-phrase matching logic correctly identifies the "call arpan"
 * phrase in various forms of recognised text.
 */
class SpeechRecognizerManagerTest {

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
}
