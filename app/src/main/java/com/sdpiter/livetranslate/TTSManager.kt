package com.sdpiter.livetranslate

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import java.util.*

class TTSManager(context: Context) {
    
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var speechSpeed = 1.0f
    
    // --- ИЗМЕНЕНО ---
    // Переменная для хранения ВСЕХ голосов, доступных в системе
    private var allVoices: Set<Voice>? = null
    // Переменная для хранения ИМЕНИ голоса, выбранного в настройках
    private var selectedVoiceName: String? = null
    // --- КОНЕЦ ИЗМЕНЕНИЙ ---
    
    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                tts?.setSpeechRate(speechSpeed)
                
                // --- ИЗМЕНЕНО ---
                // Как только TTS готов, получаем и сохраняем список всех голосов
                allVoices = tts?.voices
                // --- КОНЕЦ ИЗМЕНЕНИЙ ---
            }
        }
    }
    
    fun setSpeed(speed: Float) {
        speechSpeed = speed
        tts?.setSpeechRate(speed)
    }

    // --- ИЗМЕНЕНО ---
    // Новая публичная функция, которую мы будем вызывать из MainActivity
    // чтобы передать сохраненное в настройках имя голоса
    fun setVoice(voiceName: String?) {
        this.selectedVoiceName = voiceName
    }
    // --- КОНЕЦ ИЗМЕНЕНИЙ ---
    
    fun speak(text: String, languageCode: String, onComplete: (() -> Unit)? = null) {
        if (!isInitialized) {
            onComplete?.invoke()
            return
        }
        
        val locale = getLocale(languageCode)
        // Устанавливаем язык. Это ОБЯЗАТЕЛЬНО, даже если мы потом выберем голос
        tts?.language = locale
        tts?.setSpeechRate(speechSpeed)
        
        // --- ИЗМЕНЕНО: Логика выбора голоса ---
        // Проверяем, есть ли у нас загруженный список голосов и выбрано ли имя в настройках
        if (allVoices != null && !selectedVoiceName.isNullOrEmpty()) {
            
            // Пытаемся найти голос, который:
            // 1. Совпадает с именем, сохраненным в настройках
            // 2. Совпадает с языком, на котором мы пытаемся говорить СЕЙЧАС
            val desiredVoice = allVoices!!.firstOrNull { 
                it.name == selectedVoiceName && it.locale == locale
            }
            
            if (desiredVoice != null) {
                // Если такой голос найден - применяем его
                tts?.voice = desiredVoice
            }
            // Если голос не найден (например, в настройках выбрали "US-Female",
            // а говорим на русском), TTS автоматически использует
            // стандартный голос для `locale`, который мы установили выше.
        }
        // --- КОНЕЦ ИЗМЕНЕНИЙ ---
        
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            
            override fun onDone(utteranceId: String?) {
                onComplete?.invoke()
            }
            
            @Suppress("DEPRECATION")
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
