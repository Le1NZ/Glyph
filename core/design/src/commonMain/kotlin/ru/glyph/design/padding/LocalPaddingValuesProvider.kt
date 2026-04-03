package ru.glyph.design.padding

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf

val LocalPaddingValuesProvider = compositionLocalOf<PaddingValues> {
    error("Developer error. LocalPaddingValuesProvider is not provided")
}

val localPaddingValues: PaddingValues
    @Composable
    @ReadOnlyComposable
    get() = LocalPaddingValuesProvider.current
