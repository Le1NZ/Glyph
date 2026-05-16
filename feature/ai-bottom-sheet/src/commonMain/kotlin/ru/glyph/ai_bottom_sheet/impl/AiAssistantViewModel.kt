package ru.glyph.ai_bottom_sheet.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.glyph.ai_bottom_sheet.api.AiRepository

internal class AiAssistantViewModel(
    private val aiRepository: AiRepository,
    private val coroutineScope: CoroutineScope,
) {
    private val _state = MutableStateFlow<State>(State.Idle)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _prompt = MutableStateFlow("")
    val prompt: StateFlow<String> = _prompt.asStateFlow()

    fun onPromptChange(newPrompt: String) {
        _prompt.value = newPrompt
    }

    fun generateText(noteContent: String, quickPrompt: String? = null) {
        if (quickPrompt != null) {
            _prompt.value = quickPrompt
        }
        val currentPrompt = _prompt.value
        if (currentPrompt.isBlank()) return
        
        _state.value = State.Loading
        coroutineScope.launch {
            val result = aiRepository.generateText(currentPrompt, noteContent)
            result.fold(
                onSuccess = { generatedText ->
                    _state.value = State.Success(generatedText)
                },
                onFailure = { error ->
                    _state.value = State.Error(error.message ?: "Unknown error")
                }
            )
        }
    }

    fun reset() {
        _state.value = State.Idle
        _prompt.value = ""
    }

    sealed interface State {
        data object Idle : State
        data object Loading : State
        data class Success(val generatedText: String) : State
        data class Error(val message: String) : State
    }
}
