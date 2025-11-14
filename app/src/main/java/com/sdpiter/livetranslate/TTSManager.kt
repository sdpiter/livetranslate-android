package com.sdpiter.livetranslate

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.*

class TTSManager(context: Context) {
    
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var speechSpeed = 1.0f
    
    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                tts?.setSpeechRate(speechSpeed)
            }
        }
    }
    
    fun setSpeed(speed: Float) {
        speechSpeed = speed
        tts?.setSpeechRate(speed)
    }
    
    fun speak(text: String, languageCode: String, onComplete: (() -> Unit)? = null) {
        if (!isInitialized) {
            onComplete?.invoke()
            return
        }
        
        val locale = getLocale(languageCode)
        tts?.language = locale
        tts?.setSpeechRate(speechSpeed)
        
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            
            override fun onDone(utteranceId: String?) {
                onComplete?.invoke()
            }
            
            override fun onError(utteranceId: String?) {
                onComplete?.invoke()
            }
        })
        
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utteranceId")
    }
    
    private fun getLocale(code: String): Locale {
        return when (code) {
            "ru" -> Locale("ru", "RU")
            "en" -> Locale.US
            "es" -> Locale("es", "ES")
            "fr" -> Locale.FRENCH
            "de" -> Locale.GERMAN
            "zh" -> Locale.CHINESE
            "ja" -> Locale.JAPANESE
            "ko" -> Locale.KOREAN
            "ar" -> Locale("ar", "SA")
            "it" -> Locale.ITALIAN
            else -> Locale.US
        }
    }
    
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
