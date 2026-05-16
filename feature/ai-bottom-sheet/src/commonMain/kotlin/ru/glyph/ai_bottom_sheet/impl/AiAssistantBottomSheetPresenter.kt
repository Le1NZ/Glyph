package ru.glyph.ai_bottom_sheet.impl

import androidx.compose.runtime.Stable
import ru.glyph.navigation.api.Navigator

internal interface AiAssistantBottomSheetPresenter {

    fun onInsertText(text: String)
    fun onCancel()

    @Stable
    fun interface Factory {

        fun create(
            onInsertText: (String) -> Unit,
        ): AiAssistantBottomSheetPresenter
    }
}

internal class AiAssistantBottomSheetPresenterImpl(
    private val onInsertText: (String) -> Unit,
    navigatorLazy: Lazy<Navigator>,
) : AiAssistantBottomSheetPresenter {

    private val navigator by navigatorLazy

    override fun onInsertText(text: String) {
        navigator.hideOverlay()
        onInsertText.invoke(text)
    }

    override fun onCancel() {
        navigator.hideOverlay()
    }
}

internal class AiAssistantBottomSheetPresenterPreview : AiAssistantBottomSheetPresenter {

    override fun onInsertText(text: String) = Unit
    override fun onCancel() = Unit
}
