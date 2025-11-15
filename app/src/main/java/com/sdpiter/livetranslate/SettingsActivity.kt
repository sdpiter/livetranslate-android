package com.sdpiter.livetranslate

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.sdpiter.livetranslate.databinding.ActivitySettingsBinding
import java.util.*

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    
    // Переменные для TTS и списка голосов
    private var tts: TextToSpeech? = null
    private val voiceList = mutableListOf<Voice>()
    private val voiceDisplayList = mutableListOf<String>()
    private lateinit var voiceAdapter: ArrayAdapter<String>

    companion object {
        private const val PREFS_NAME = "LiveTranslatePrefs"
        private const val KEY_AUTO_SPEAK = "auto_speak"
        private const val KEY_AUTO_TRANSLATE = "auto_translate"
        private const val KEY_SPEECH_SPEED = "speech_speed"
        private const val KEY_DARK_THEME = "dark_theme"
        
        // Новый ключ для сохранения имени голоса
        private const val KEY_SPEECH_VOICE = "speech_voice"
        
        fun isAutoSpeakEnabled(context: Context): Boolean {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_AUTO_SPEAK, false)
        }
        
        fun isAutoTranslateEnabled(context: Context): Boolean {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_AUTO_TRANSLATE, true)
        }
        
        fun getSpeechSpeed(context: Context): Float {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getFloat(KEY_SPEECH_SPEED, 1.0f)
        }
        
        fun isDarkThemeEnabled(context: Context): Boolean {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_DARK_THEME, false)
        }

        // Новая функция для получения имени голоса
        fun getSpeechVoice(context: Context): String? {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_SPEECH_VOICE, null) // По умолчанию null
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"
        
        // Инициализируем адаптер для голосов
        setupVoiceSpinner()
        
        loadSettings()
        setupListeners()

        // Инициализируем TTS, чтобы получить список голосов
        initializeTts()
    }
    
    private fun loadSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        binding.switchAutoSpeak.isChecked = prefs.getBoolean(KEY_AUTO_SPEAK, false)
        binding.switchAutoTranslate.isChecked = prefs.getBoolean(KEY_AUTO_TRANSLATE, true)
        binding.sliderSpeechSpeed.value = prefs.getFloat(KEY_SPEECH_SPEED, 1.0f)
        binding.switchDarkTheme.isChecked = prefs.getBoolean(KEY_DARK_THEME, false)
        
        updateSpeedLabel(binding.sliderSpeechSpeed.value)
        // Загрузка голоса произойдет в onInit TTS
    }

    // Новая функция для инициализации TTS
    private fun initializeTts() {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                loadVoices()
            }
        }
    }

    // Новая функция для загрузки голосов в Spinner
    private fun loadVoices() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedVoiceName = prefs.getString(KEY_SPEECH_VOICE, null)
        
        // Получаем все голоса из системы
        val systemVoices = tts?.voices?.filter { 
            !it.isNetworkConnectionRequired 
        }?.sortedBy { it.locale.displayLanguage }
        
        voiceList.clear()
        voiceDisplayList.clear()

        // Добавляем опцию "По умолчанию"
        voiceDisplayList.add(getString(R.string.voice_default))
        voiceList.add(Voice("DEFAULT", Locale.getDefault(), Voice.QUALITY_NORMAL, Voice.LATENCY_NORMAL, false, emptySet())) // Фиктивный Voice-объект

        var savedSelectionIndex = 0

        if (systemVoices != null) {
            for ((index, voice) in systemVoices.withIndex()) {
                // Формируем красивое имя для отображения
                val displayName = "${voice.locale.displayName} (${getVoiceGender(voice)}) - [${voice.name}]"
                voiceDisplayList.add(displayName)
                voiceList.add(voice)
                
                // Если этот голос был сохранен ранее, запоминаем его позицию
                if (voice.name == savedVoiceName) {
                    savedSelectionIndex = index + 1 // +1 из-за "По умолчанию"
                }
            }
        }
        
        runOnUiThread {
            voiceAdapter.notifyDataSetChanged()
            binding.spinnerSpeechVoice.setSelection(savedSelectionIndex)
        }
    }

    private fun getVoiceGender(voice: Voice): String {
        val name = voice.name.toLowerCase(Locale.ROOT)
        return when {
            name.contains("female") -> "Female"
            name.contains("male") -> "Male"
            else -> "Neutral"
        }
    }
    
    // Настраиваем Spinner для голосов
    private fun setupVoiceSpinner() {
        voiceAdapter = ArrayAdapter(this, R.layout.spinner_item, voiceDisplayList)
        voiceAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerSpeechVoice.adapter = voiceAdapter
    }

    private fun setupListeners() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        binding.switchAutoSpeak.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_AUTO_SPEAK, isChecked).apply()
        }
        
        binding.switchAutoTranslate.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_AUTO_TRANSLATE, isChecked).apply()
        }
        
        binding.sliderSpeechSpeed.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                prefs.edit().putFloat(KEY_SPEECH_SPEED, value).apply()
                updateSpeedLabel(value)
            }
        }
        
        binding.switchDarkTheme.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_DARK_THEME, isChecked).apply()
            applyTheme(isChecked)
        }

        // Слушатель для Spinner'а голосов
        binding.spinnerSpeechVoice.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedVoice = voiceList.getOrNull(position)
                
                if (selectedVoice != null) {
                    if (position == 0) { // Если выбрано "По умолчанию"
                        prefs.edit().remove(KEY_SPEECH_VOICE).apply()
                    } else {
                        // Сохраняем уникальное имя голоса
                        prefs.edit().putString(KEY_SPEECH_VOICE, selectedVoice.name).apply()
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    private fun updateSpeedLabel(value: Float) {
        binding.tvSpeedValue.text = String.format(Locale.US, "Speed: %.1fx", value)
    }
    
    private fun applyTheme(darkTheme: Boolean) {
        if (darkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    // Не забываем выключить TTS
    override fun onDestroy() {
        super.onDestroy()
        tts?.stop()
        tts?.shutdown()
    }
}
