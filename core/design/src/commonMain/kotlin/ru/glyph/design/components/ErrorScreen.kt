package ru.glyph.design.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.glyph.design.padding.localPaddingValues
import ru.glyph.design.theme.GlyphTheme
import ru.glyph.string.resources.Res
import ru.glyph.string.resources.error_retry_button
import ru.glyph.string.resources.error_text

@Composable
fun ErrorScreen(
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(localPaddingValues)
            .padding(all = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.error_text),
                style = GlyphTheme.typography.heading1,
                color = GlyphTheme.colors.textPrimary,
            )

            PrimaryButton(
                text = stringResource(Res.string.error_retry_button),
                onClick = onRetryClick,
                modifier = modifier,
            )
        }
    }
}