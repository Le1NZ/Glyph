package ru.glyph.screen.home.ui.composable.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import ru.glyph.design.theme.GlyphFolderColors

@Immutable
internal data class FolderUiModel(
    val id: String,
    val name: String,
    val noteCount: Int,
    val color: Color,
) {

    companion object {

        fun forPreview() = FolderUiModel(
            id = "1",
            name = "Рабочие проекты",
            noteCount = 12,
            color = GlyphFolderColors.Blue,
        )
    }
}
