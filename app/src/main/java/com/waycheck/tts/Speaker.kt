package com.waycheck.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class Speaker(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isReady = false
    private var lastSpokenText: String = ""
    private var lastSpokenTimeMs: Long = 0L

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Prefer Indian English voice for natural localized delivery
            val indianLocale = Locale.forLanguageTag("en-IN")
            val result = tts?.setLanguage(indianLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w("Speaker", "en-IN locale not installed, falling back to device default")
                tts?.setLanguage(Locale.getDefault())
            }
            tts?.setSpeechRate(0.98f) // slightly calmer, natural pacing
            tts?.setPitch(1.0f)
            isReady = true
        } else {
            Log.e("Speaker", "TextToSpeech initialization failed with status $status")
        }
    }

    fun speak(text: String, force: Boolean = false) {
        if (!isReady || text.isBlank()) return

        val now = System.currentTimeMillis()
        // Respectful cooldown: don't shout repeatedly. Wait at least 10 seconds between same alerts
        if (!force && text == lastSpokenText && (now - lastSpokenTimeMs) < 10000L) {
            return
        }

        lastSpokenText = text
        lastSpokenTimeMs = now
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "DuxUtteranceId")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }
}
