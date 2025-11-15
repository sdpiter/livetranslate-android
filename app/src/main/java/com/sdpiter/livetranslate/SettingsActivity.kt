package com.sdpiter.livetranslate

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.slider.Slider
import com.sdpiter.livetranslate.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    
    companion object {
        private const val PREFS_NAME = "LiveTranslatePrefs"
        private const val KEY_AUTO_SPEAK = "auto_speak"
        private const val KEY_AUTO_TRANSLATE = "auto_translate"
        private const val KEY_SPEECH_SPEED = "speech_speed"
        private const val KEY_DARK_THEME = "dark_theme"
        
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
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Set up action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"
        
        loadSettings()
        setupListeners()
    }
    
    private fun loadSettings() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        binding.switchAutoSpeak.isChecked = prefs.getBoolean(KEY_AUTO_SPEAK, false)
        binding.switchAutoTranslate.isChecked = prefs.getBoolean(KEY_AUTO_TRANSLATE, true)
        binding.sliderSpeechSpeed.value = prefs.getFloat(KEY_SPEECH_SPEED, 1.0f)
        binding.switchDarkTheme.isChecked = prefs.getBoolean(KEY_DARK_THEME, false)
        
        updateSpeedLabel(binding.sliderSpeechSpeed.value)
    }
    
    private fun setupListeners() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Auto-speak toggle
        binding.switchAutoSpeak.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_AUTO_SPEAK, isChecked).apply()
        }
        
        // Auto-translate toggle
        binding.switchAutoTranslate.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_AUTO_TRANSLATE, isChecked).apply()
        }
        
        // Speech speed slider
        // СТАЛО:
        binding.sliderSpeechSpeed.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                prefs.edit().putFloat(KEY_SPEECH_SPEED, value).apply()
                updateSpeedLabel(value)
            }
        }
        
        // Dark theme toggle
        binding.switchDarkTheme.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_DARK_THEME, isChecked).apply()
            applyTheme(isChecked)
        }
    }
    
    private fun updateSpeedLabel(value: Float) {
        binding.tvSpeedValue.text = "Speed: %.1fx".format(value)
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
}
