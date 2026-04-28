package ru.glyph.navigation.api.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import ru.glyph.navigation.ui.SceneStrategyImpl

sealed class BottomSheet : OverlayDestination() {

    data class Confirm(
        val text: @Composable () -> String,
        val onConfirm: () -> Unit,
    ) : BottomSheet()
}

@Immutable
data class BottomSheetMeta(
    val skipPartiallyExpanded: Boolean,
)

fun bottomSheetMetadata(
    meta: BottomSheetMeta,
): Map<String, BottomSheetMeta> {
    return SceneStrategyImpl.bottomSheet(meta)
}