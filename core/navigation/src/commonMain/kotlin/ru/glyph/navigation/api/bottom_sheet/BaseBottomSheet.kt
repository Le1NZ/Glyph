package ru.glyph.navigation.api.bottom_sheet

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import ru.glyph.design.padding.localPaddingValues
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Stable
@OptIn(ExperimentalUuidApi::class)
abstract class BaseBottomSheet(
    val title: (@Composable () -> String)?,
    val skipPartiallyExpanded: Boolean,
) {
    val id = Uuid.random().toString()

    @Composable
    abstract fun Content(
        contentPadding: PaddingValues = localPaddingValues,
    )
}
