package ru.glyph.design.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import ru.glyph.design.padding.LocalPaddingValuesProvider

@Composable
fun ScaffoldScreen(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val parentPadding = LocalPaddingValuesProvider.current
    Scaffold(
        modifier = modifier,
        topBar = topBar,
    ) { innerPadding ->
        val layoutDirection = LocalLayoutDirection.current
        val totalPadding = remember(innerPadding, parentPadding, layoutDirection) {
            PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = parentPadding.calculateBottomPadding(),
                start = parentPadding.calculateStartPadding(layoutDirection),
                end = parentPadding.calculateEndPadding(layoutDirection)
            )
        }

        CompositionLocalProvider(LocalPaddingValuesProvider provides totalPadding) {
            content()
        }
    }
}