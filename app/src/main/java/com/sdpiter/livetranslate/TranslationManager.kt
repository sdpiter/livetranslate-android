package com.sdpiter.livetranslate

import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

class TranslationManager {
    
    private var translator: Translator? = null
    private var currentSourceLang = TranslateLanguage.RUSSIAN
    private var currentTargetLang = TranslateLanguage.ENGLISH
    
    init {
        initializeTranslator()
    }
    
    fun setLanguages(sourceLang: String, targetLang: String) {
        val source = mapLanguageCode(sourceLang)
        val target = mapLanguageCode(targetLang)
        
        if (source != currentSourceLang || target != currentTargetLang) {
            currentSourceLang = source
            currentTargetLang = target
            translator?.close()
            initializeTranslator()
        }
    }
    
    private fun initializeTranslator() {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(currentSourceLang)
            .setTargetLanguage(currentTargetLang)
            .build()
        
        translator = Translation.getClient(options)
        
        // Download model if needed
        translator?.downloadModelIfNeeded()
    }
    
    suspend fun translate(text: String): String {
        return try {
            translator?.translate(text)?.await() ?: throw Exception("Translator not initialized")
        } catch (e: Exception) {
            throw Exception("Translation failed: ${e.message}")
        }
    }
    
    private fun mapLanguageCode(code: String): String {
        return when (code) {
            "ru" -> TranslateLanguage.RUSSIAN
            "en" -> TranslateLanguage.ENGLISH
            "es" -> TranslateLanguage.SPANISH
            "fr" -> TranslateLanguage.FRENCH
            "de" -> TranslateLanguage.GERMAN
            "zh" -> TranslateLanguage.CHINESE
            "ja" -> TranslateLanguage.JAPANESE
            "ko" -> TranslateLanguage.KOREAN
            "ar" -> TranslateLanguage.ARABIC
            "it" -> TranslateLanguage.ITALIAN
            else -> TranslateLanguage.ENGLISH
        }
    }
    
    fun close() {
        translator?.close()
    }
}
