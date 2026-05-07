package ru.glyph.design.theme

import androidx.compose.ui.graphics.Color
import ru.glyph.model.FolderColor

fun FolderColor.toGlyphColor(): Color = when (this) {
    FolderColor.BLUE -> GlyphFolderColors.Blue
    FolderColor.PURPLE -> GlyphFolderColors.Purple
    FolderColor.GREEN -> GlyphFolderColors.Green
    FolderColor.ORANGE -> GlyphFolderColors.Orange
    FolderColor.RED -> GlyphFolderColors.Red
    FolderColor.TEAL -> GlyphFolderColors.Teal
}
