package com.sdpiter.livetranslate

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.sdpiter.livetranslate.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var translationManager: TranslationManager
    private lateinit var speechManager: SpeechManager
    private lateinit var ttsManager: TTSManager
    
    private var sourceLanguage = "ru"
    private var targetLanguage = "en"
    private var autoSpeak = false // Автоозвучивание после перевода
    
    companion object {
        private const val RECORD_AUDIO_PERMISSION_CODE = 1001
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initializeManagers()
        setupSpinners()
        setupListeners()
        checkPermissions()
    }
    
    private fun initializeManagers() {
        translationManager = TranslationManager()
        speechManager = SpeechManager(this)
        ttsManager = TTSManager(this)
    }
    
    private fun setupSpinners() {
        // Создаём адаптер с кастомным layout для чёрного текста
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.languages,
            R.layout.spinner_item
        )
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        
        // Применяем к обоим Spinner
        binding.spinnerSourceLang.adapter = adapter
        binding.spinnerTargetLang.adapter = adapter
        
        // Устанавливаем начальные значения
        binding.spinnerSourceLang.setSelection(0) // Russian
        binding.spinnerTargetLang.setSelection(1) // English
    }
    
    private fun setupListeners() {
        // Language spinners
        binding.spinnerSourceLang.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                sourceLanguage = getLanguageCode(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        binding.spinnerTargetLang.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                targetLanguage = getLanguageCode(position)
                translationManager.setLanguages(sourceLanguage, targetLanguage)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Main Buttons
        binding.btnRecord.setOnClickListener { startRecording() }
        binding.btnTranslate.setOnClickListener { translateText() }
        binding.btnSpeak.setOnClickListener { speakTranslation() }
        
        // NEW: Swap Languages Button
        binding.fabSwapLanguages.setOnClickListener { swapLanguages() }
        
        // NEW: Clear Source Text Button
        binding.btnClearSource.setOnClickListener { clearSourceText() }
        
        // NEW: Copy Translation Button
        binding.btnCopyTranslation.setOnClickListener { copyTranslation() }
        
        // NEW: Share Translation Button
        binding.btnShareTranslation.setOnClickListener { shareTranslation() }
        
        // Speech recognition callback
        speechManager.onResult = { text ->
            binding.etSourceText.setText(text)
            updateStatus("Recognition complete")
            translateText()
        }
        
        speechManager.onError = { error ->
            updateStatus("Error: $error")
            hideProgress()
        }
    }
    
    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_CODE
            )
        }
    }
    
    private fun startRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Microphone permission required", Toast.LENGTH_SHORT).show()
            return
        }
        
        updateStatus("Listening...")
        showProgress()
        speechManager.startListening(sourceLanguage)
    }
    
    private fun translateText() {
        val text = binding.etSourceText.text.toString().trim()
        if (text.isEmpty()) {
            Toast.makeText(this, "No text to translate", Toast.LENGTH_SHORT).show()
            return
        }
        
        updateStatus("Translating...")
        showProgress()
        
        lifecycleScope.launch {
            try {
                val translation = translationManager.translate(text)
                binding.etTranslatedText.setText(translation)
                updateStatus("Translation complete")
                
                // NEW: Автоозвучивание если включено
                if (autoSpeak && translation.isNotEmpty()) {
                    speakTranslation()
                }
            } catch (e: Exception) {
                updateStatus("Translation error: ${e.message}")
                Toast.makeText(this@MainActivity, "Translation failed", Toast.LENGTH_SHORT).show()
            } finally {
                hideProgress()
            }
        }
    }
    
    private fun speakTranslation() {
        val text = binding.etTranslatedText.text.toString().trim()
        if (text.isEmpty()) {
            Toast.makeText(this, "No translation to speak", Toast.LENGTH_SHORT).show()
            return
        }
        
        updateStatus("Speaking...")
        ttsManager.speak(text, targetLanguage) {
            updateStatus("Ready")
        }
    }
    
    // NEW: Swap source and target languages
    private fun swapLanguages() {
        val sourcePosition = binding.spinnerSourceLang.selectedItemPosition
        val targetPosition = binding.spinnerTargetLang.selectedItemPosition
        
        // Меняем позиции в спиннерах
        binding.spinnerSourceLang.setSelection(targetPosition)
        binding.spinnerTargetLang.setSelection(sourcePosition)
        
        // Меняем текст местами
        val sourceText = binding.etSourceText.text.toString()
        val translatedText = binding.etTranslatedText.text.toString()
        
        binding.etSourceText.setText(translatedText)
        binding.etTranslatedText.setText(sourceText)
        
        Toast.makeText(this, "Languages swapped", Toast.LENGTH_SHORT).show()
    }
    
    // NEW: Clear source text
    private fun clearSourceText() {
        binding.etSourceText.setText("")
        binding.etTranslatedText.setText("")
        updateStatus("Ready")
        Toast.makeText(this, "Text cleared", Toast.LENGTH_SHORT).show()
    }
    
    // NEW: Copy translation to clipboard
    private fun copyTranslation() {
        val text = binding.etTranslatedText.text.toString().trim()
        if (text.isEmpty()) {
            Toast.makeText(this, "No translation to copy", Toast.LENGTH_SHORT).show()
            return
        }
        
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Translation", text)
        clipboard.setPrimaryClip(clip)
        
        Toast.makeText(this, "Translation copied to clipboard", Toast.LENGTH_SHORT).show()
    }
    
    // NEW: Share translation
    private fun shareTranslation() {
        val text = binding.etTranslatedText.text.toString().trim()
        if (text.isEmpty()) {
            Toast.makeText(this, "No translation to share", Toast.LENGTH_SHORT).show()
            return
        }
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        
        startActivity(Intent.createChooser(shareIntent, "Share translation via"))
    }
    
    private fun getLanguageCode(position: Int): String {
        val codes = resources.getStringArray(R.array.language_codes)
        return codes.getOrNull(position) ?: "en"
    }
    
    private fun updateStatus(message: String) {
        runOnUiThread {
            binding.tvStatus.text = message
        }
    }
    
    private fun showProgress() {
        runOnUiThread {
            binding.progressBar.visibility = View.VISIBLE
        }
    }
    
    private fun hideProgress() {
        runOnUiThread {
            binding.progressBar.visibility = View.GONE
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        speechManager.destroy()
        ttsManager.shutdown()
    }
    
    // Позволяет приложению работать в фоне
    override fun onPause() {
        super.onPause()
        // Приложение может продолжить работу в фоне
    }
    
    override fun onResume() {
        super.onResume()
        updateStatus("Ready")
    }
}
