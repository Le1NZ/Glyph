package ru.glyph.confirm_bottom_sheet.impl

import androidx.compose.runtime.Stable
import ru.glyph.navigation.api.Navigator

internal interface ConfirmBottomPresenter {

    fun onConfirm()
    fun onCancel()

    @Stable
    fun interface Factory {

        fun create(
            onConfirm: () -> Unit,
        ): ConfirmBottomPresenter
    }
}

internal class ConfirmBottomPresenterImpl(
    private val onConfirmAccepted: () -> Unit,
    navigatorLazy: Lazy<Navigator>,
) : ConfirmBottomPresenter {

    private val navigator by navigatorLazy

    override fun onConfirm() {
        onConfirmAccepted()
        navigator.hideOverlay()
    }

    override fun onCancel() {
        navigator.hideOverlay()
    }
}

internal class ConfirmBottomPresenterPreview : ConfirmBottomPresenter {

    override fun onConfirm() = Unit
    override fun onCancel() = Unit
}