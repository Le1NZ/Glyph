package ru.glyph.ai_bottom_sheet.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

@Composable
internal fun AiAssistantBottomSheetInternal(
    presenterFactory: AiAssistantBottomSheetPresenter.Factory,
    viewModel: AiAssistantViewModel,
    noteContent: String,
    onInsertText: (String) -> Unit,
) {
    val presenter = remember(onInsertText) {
        presenterFactory.create(onInsertText)
    }
    
    val state by viewModel.state.collectAsState()
    val prompt by viewModel.prompt.collectAsState()

    AiAssistantBottomSheetContent(
        presenter = presenter,
        state = state,
        prompt = prompt,
        onPromptChange = viewModel::onPromptChange,
        onGenerate = { quickPrompt ->
            viewModel.generateText(noteContent, quickPrompt)
        },
        onReset = viewModel::reset
    )
}
