package ru.glyph.design.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.glyph.design.theme.GlyphShape
import ru.glyph.design.theme.GlyphTheme

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    SecondaryButtonLayout(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = GlyphTheme.typography.heading3.copy(fontWeight = FontWeight.Medium),
        )
    }
}

@Composable
fun SecondaryButtonLayout(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        contentPadding = PaddingValues(all = 16.dp),
        shape = GlyphShape.button,
        colors = ButtonDefaults.buttonColors(
            containerColor = GlyphTheme.colors.shadow,
            contentColor = GlyphTheme.colors.textPrimary,
        ),
    ) { content() }
}
