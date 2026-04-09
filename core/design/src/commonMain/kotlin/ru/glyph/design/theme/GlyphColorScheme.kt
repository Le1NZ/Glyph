package ru.glyph.design.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class GlyphColorScheme(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textSubtle: Color,
    val shadow: Color,
    val accent: Color,
    val fabBackground: Color,
    val fabContent: Color,
    val contentOnAccent: Color,
)

val lightGlyphColorScheme = GlyphColorScheme(
    background = Color(0xFFF9FAFB),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF3F4F6),
    textPrimary = Color(0xFF0A0A0A),
    textSecondary = Color(0xFF6A7282),
    textSubtle = Color(0xFF4A5565),
    shadow = Color(0x1A000000),
    accent = Color(0xFFFC3F1D),
    fabBackground = Color(0xFF0A0A0A),
    fabContent = Color(0xFFFFFFFF),
    contentOnAccent = Color(0xFFFFFFFF),
)

object GlyphFolderColors {
    val Blue = Color(0xFF2B7FFF)
    val Purple = Color(0xFFAD46FF)
    val Green = Color(0xFF00BC7D)
    val Orange = Color(0xFFFF6B35)
    val Red = Color(0xFFFF3B30)
    val Teal = Color(0xFF00AECB)
}
