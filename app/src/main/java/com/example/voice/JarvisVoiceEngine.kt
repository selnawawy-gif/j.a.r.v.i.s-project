package com.example.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class JarvisVoiceEngine(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentSpokenText = MutableStateFlow("")
    val currentSpokenText: StateFlow<String> = _currentSpokenText.asStateFlow()

    private val _availableVoices = MutableStateFlow<List<Voice>>(emptyList())
    val availableVoices: StateFlow<List<Voice>> = _availableVoices.asStateFlow()

    private val _selectedVoiceName = MutableStateFlow<String>("")
    val selectedVoiceName: StateFlow<String> = _selectedVoiceName.asStateFlow()

    var pitch: Float = 0.92f
        set(value) {
            field = value
            tts?.setPitch(value)
        }

    var speechRate: Float = 1.00f
        set(value) {
            field = value
            tts?.setSpeechRate(value)
        }

    // Strictly enforce foreground-only speech playback
    var isForeground: Boolean = true
        set(value) {
            field = value
            if (!value) {
                stop()
            }
        }

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { engine ->
                // Look for UK English voices (closest to Paul Bettany / Jarvis)
                val voicesList = try {
                    engine.voices?.toList() ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }

                _availableVoices.value = voicesList

                // Find best British voice
                val bestUkVoice = findBestJarvisVoice(voicesList)
                if (bestUkVoice != null) {
                    engine.voice = bestUkVoice
                    _selectedVoiceName.value = bestUkVoice.name
                } else {
                    val result = engine.setLanguage(Locale.UK)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        engine.setLanguage(Locale.ENGLISH)
                    }
                    _selectedVoiceName.value = "Default British Engine"
                }

                engine.setPitch(pitch)
                engine.setSpeechRate(speechRate)

                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        _currentSpokenText.value = ""
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        _currentSpokenText.value = ""
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        _isSpeaking.value = false
                        _currentSpokenText.value = ""
                    }
                })

                _isInitialized.value = true
            }
        }
    }

    private fun findBestJarvisVoice(voices: List<Voice>): Voice? {
        val ukVoices = voices.filter { voice ->
            val loc = voice.locale
            loc.country.equals("GB", ignoreCase = true) ||
            loc.language.equals("en", ignoreCase = true) && loc.country.equals("GB", ignoreCase = true) ||
            voice.name.contains("en-gb", ignoreCase = true)
        }

        // Prioritize male/resonant voices if named
        val preferredVoice = ukVoices.find { voice ->
            val name = voice.name.lowercase()
            name.contains("rjs") || name.contains("male") || name.contains("gbc") || name.contains("gbb")
        } ?: ukVoices.firstOrNull()

        return preferredVoice
    }

    fun selectVoiceByName(name: String) {
        val voice = _availableVoices.value.find { it.name == name }
        if (voice != null) {
            tts?.voice = voice
            _selectedVoiceName.value = voice.name
        }
    }

    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        // Only speak if app is in the foreground
        if (!isForeground) {
            return
        }

        val engine = tts ?: return
        if (!_isInitialized.value) return

        _currentSpokenText.value = text
        _isSpeaking.value = true

        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        }

        val utteranceId = "JARVIS_${System.currentTimeMillis()}"
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _isSpeaking.value = false
        _currentSpokenText.value = ""
    }

    fun shutdown() {
        stop()
        try {
            tts?.shutdown()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        tts = null
        _isInitialized.value = false
    }
}
