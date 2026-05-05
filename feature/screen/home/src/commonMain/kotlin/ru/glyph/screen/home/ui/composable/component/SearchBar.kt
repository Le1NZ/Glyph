package ru.glyph.screen.home.ui.composable.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.glyph.design.Res
import ru.glyph.design.ic_close
import ru.glyph.design.ic_search
import ru.glyph.design.theme.GlyphShape
import ru.glyph.design.theme.GlyphTheme
import ru.glyph.string.resources.Res as StringRes
import ru.glyph.string.resources.home_search_clear_cd
import ru.glyph.string.resources.home_search_placeholder

@Composable
internal fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = GlyphTheme.colors
    val typography = GlyphTheme.typography

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(color = colors.background, shape = GlyphShape.card)
            .padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_search),
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = typography.heading3.copy(color = colors.textPrimary),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = stringResource(StringRes.string.home_search_placeholder),
                        style = typography.heading3.copy(color = colors.textSecondary.copy(alpha = 0.5f)),
                    )
                }
                innerTextField()
            },
            modifier = Modifier.weight(1f),
        )
        if (value.isNotEmpty()) {
            IconButton(onClick = { onValueChange("") }) {
                Icon(
                    painter = painterResource(Res.drawable.ic_close),
                    contentDescription = stringResource(StringRes.string.home_search_clear_cd),
                    tint = colors.textSecondary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
