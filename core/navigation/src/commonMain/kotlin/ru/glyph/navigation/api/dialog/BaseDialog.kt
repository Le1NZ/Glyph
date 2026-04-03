package ru.glyph.navigation.api.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
abstract class BaseDialog(
    val properties: DialogProperties,
) {
    val id = Uuid.random().toString()

    @Composable
    abstract fun Content()
}
