package ru.glyph.design.theme

import androidx.compose.foundation.LocalIndication
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalGlyphColorScheme = staticCompositionLocalOf { lightGlyphColorScheme }
val LocalGlyphTypography = staticCompositionLocalOf { defaultGlyphTypography }

object GlyphTheme {

    val colors: GlyphColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalGlyphColorScheme.current

    val typography: GlyphTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalGlyphTypography.current
}

@Composable
fun GlyphTheme(
    colors: GlyphColorScheme = lightGlyphColorScheme,
    typography: GlyphTypography = defaultGlyphTypography,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalGlyphColorScheme provides colors,
        LocalGlyphTypography provides typography,
        LocalRippleConfiguration provides RippleConfiguration(color = GlyphFolderColors.Blue),
        LocalIndication provides ripple(),
        content = content,
    )
}
