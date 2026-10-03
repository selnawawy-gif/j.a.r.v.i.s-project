package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class AssistantAudioState {
    IDLE,
    LISTENING_FOR_WAKE_WORD,
    WAKE_DETECTED,
    LISTENING_FOR_COMMAND,
    PROCESSING,
    SPEAKING
}

class WakeWordEngine(
    private val context: Context,
    private val onWakeWordDetected: () -> Unit,
    private val onCommandReceived: (String) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _audioState = MutableStateFlow(AssistantAudioState.IDLE)
    val audioState: StateFlow<AssistantAudioState> = _audioState.asStateFlow()

    private val _rmsAudioLevel = MutableStateFlow(0f)
    val rmsAudioLevel: StateFlow<Float> = _rmsAudioLevel.asStateFlow()

    private val _liveTranscript = MutableStateFlow("")
    val liveTranscript: StateFlow<String> = _liveTranscript.asStateFlow()

    private val _isWakeWordEnabled = MutableStateFlow(true)
    val isWakeWordEnabled: StateFlow<Boolean> = _isWakeWordEnabled.asStateFlow()

    // Strict foreground constraint
    var isForeground: Boolean = true
        set(value) {
            field = value
            if (!value) {
                stopListening()
            } else if (_isWakeWordEnabled.value) {
                startWakeWordListening()
            }
        }

    private var isListening = false
    private var awaitingCommandAfterWake = false
    private var isRestarting = false

    private val wakePhrases = listOf(
        "hey jarvis",
        "jarvis",
        "ok jarvis",
        "okay jarvis",
        "hello jarvis",
        "wake up jarvis"
    )

    fun setWakeWordEnabled(enabled: Boolean) {
        _isWakeWordEnabled.value = enabled
        if (enabled && isForeground) {
            startWakeWordListening()
        } else {
            stopListening()
            _audioState.value = AssistantAudioState.IDLE
        }
    }

    fun startWakeWordListening() {
        if (!isForeground || !_isWakeWordEnabled.value) return
        mainHandler.post {
            initRecognizerIfNeeded()
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }
                speechRecognizer?.startListening(intent)
                isListening = true
                _audioState.value = if (awaitingCommandAfterWake) {
                    AssistantAudioState.LISTENING_FOR_COMMAND
                } else {
                    AssistantAudioState.LISTENING_FOR_WAKE_WORD
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Trigger immediate listening for command (e.g. user tapped Arc Reactor)
     */
    fun startDirectCommandListening() {
        if (!isForeground) return
        awaitingCommandAfterWake = true
        _audioState.value = AssistantAudioState.LISTENING_FOR_COMMAND
        _liveTranscript.value = "Listening for command..."
        startWakeWordListening()
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.cancel()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isListening = false
            awaitingCommandAfterWake = false
            _rmsAudioLevel.value = 0f
            if (_audioState.value != AssistantAudioState.SPEAKING) {
                _audioState.value = AssistantAudioState.IDLE
            }
        }
    }

    private fun initRecognizerIfNeeded() {
        if (speechRecognizer == null && SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createRecognitionListener())
            }
        }
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {
                // Normalize 0..10 dB to 0.0..1.0
                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                _rmsAudioLevel.value = normalized
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _rmsAudioLevel.value = 0f
            }

            override fun onError(error: Int) {
                _rmsAudioLevel.value = 0f
                isListening = false
                scheduleRestart()
            }

            override fun onResults(results: Bundle?) {
                _rmsAudioLevel.value = 0f
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.firstOrNull()?.trim() ?: ""

                if (spokenText.isNotEmpty()) {
                    handleSpokenInput(spokenText)
                } else {
                    scheduleRestart()
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partial = matches?.firstOrNull()?.trim() ?: ""
                if (partial.isNotEmpty()) {
                    _liveTranscript.value = partial
                    checkWakeWordInPartial(partial)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    private fun checkWakeWordInPartial(text: String) {
        val lower = text.lowercase()
        for (phrase in wakePhrases) {
            val idx = lower.indexOf(phrase)
            if (idx != -1) {
                // Wake word detected!
                _audioState.value = AssistantAudioState.WAKE_DETECTED
                val trailingCommand = lower.substring(idx + phrase.length).trim()
                if (trailingCommand.length > 3) {
                    // Command was spoken in the same breath (e.g. "Hey Jarvis what is my schedule")
                    _liveTranscript.value = trailingCommand
                    stopListening()
                    onWakeWordDetected()
                    onCommandReceived(trailingCommand)
                    return
                } else {
                    // Just the wake word
                    awaitingCommandAfterWake = true
                    onWakeWordDetected()
                    return
                }
            }
        }
    }

    private fun handleSpokenInput(text: String) {
        val lower = text.lowercase()
        var commandToExecute: String? = null

        // If we were already awaiting a command after waking
        if (awaitingCommandAfterWake) {
            awaitingCommandAfterWake = false
            commandToExecute = text
        } else {
            // Check if wake word is part of the final result
            for (phrase in wakePhrases) {
                val idx = lower.indexOf(phrase)
                if (idx != -1) {
                    val trailing = lower.substring(idx + phrase.length).trim()
                    if (trailing.isNotEmpty()) {
                        commandToExecute = trailing
                    } else {
                        // User just said "Hey Jarvis"
                        onWakeWordDetected()
                        awaitingCommandAfterWake = true
                        startDirectCommandListening()
                        return
                    }
                    break
                }
            }
        }

        if (commandToExecute != null && commandToExecute.isNotBlank()) {
            _liveTranscript.value = commandToExecute
            _audioState.value = AssistantAudioState.PROCESSING
            onCommandReceived(commandToExecute)
        } else {
            scheduleRestart()
        }
    }

    private fun scheduleRestart() {
        if (!isForeground || !_isWakeWordEnabled.value || isRestarting) return
        isRestarting = true
        mainHandler.postDelayed({
            isRestarting = false
            if (isForeground && _isWakeWordEnabled.value) {
                startWakeWordListening()
            }
        }, 500)
    }

    fun destroy() {
        stopListening()
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            speechRecognizer = null
        }
    }
}
