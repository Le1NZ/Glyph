package ru.glyph.design.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.glyph.design.theme.GlyphShape
import ru.glyph.design.theme.GlyphTheme
import ru.glyph.design.utils.pxToDp

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    PrimaryButtonLayout(
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
fun LoadingPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = true,
    textStyle: TextStyle = GlyphTheme.typography.heading3.copy(fontWeight = FontWeight.Medium),
) {
    val textSize = rememberTextMeasurer().measure(
        text = text,
        style = textStyle,
        maxLines = 1,
    ).size

    var lastButtonSize by remember { mutableStateOf(textSize) }

    PrimaryButtonLayout(
        onClick = onClick,
        modifier = modifier
            .sizeIn(
                minHeight = lastButtonSize.height.pxToDp(),
                minWidth = lastButtonSize.width.pxToDp(),
            )
            .onSizeChanged { size ->
                lastButtonSize = size
            },
        enabled = !loading,
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = GlyphTheme.colors.contentOnAccent,
                strokeWidth = 2.dp,
                modifier = Modifier
                    .height(textSize.height.pxToDp())
                    .aspectRatio(1f),
            )
        } else {
            Text(
                text = text,
                textAlign = TextAlign.Center,
                style = textStyle,
            )
        }
    }
}

@Composable
fun PrimaryButtonLayout(
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
            containerColor = GlyphTheme.colors.accent,
            contentColor = GlyphTheme.colors.contentOnAccent,
        ),
    ) { content() }
}