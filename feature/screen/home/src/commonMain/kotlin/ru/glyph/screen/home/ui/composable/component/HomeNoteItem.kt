package ru.glyph.screen.home.ui.composable.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.glyph.design.Res
import ru.glyph.design.ic_description
import ru.glyph.design.theme.GlyphElevation
import ru.glyph.design.theme.GlyphShape
import ru.glyph.design.theme.GlyphTheme
import ru.glyph.screen.home.ui.composable.model.NoteUiModel
import ru.glyph.string.resources.note_untitled
import ru.glyph.string.resources.time_days_ago
import ru.glyph.string.resources.time_hours_ago
import ru.glyph.string.resources.time_just_now
import ru.glyph.string.resources.time_minutes_ago
import ru.glyph.string.resources.time_yesterday
import ru.glyph.utils.clock.currentTimeDuration
import kotlin.time.Duration.Companion.seconds
import ru.glyph.string.resources.Res as StringRes

@Composable
internal fun HomeNoteItem(
    note: NoteUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = GlyphElevation.card,
                shape = GlyphShape.card,
                spotColor = GlyphTheme.colors.shadow,
                ambientColor = GlyphTheme.colors.shadow,
            )
            .background(color = GlyphTheme.colors.surface, shape = GlyphShape.card)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color = GlyphTheme.colors.surfaceVariant, shape = GlyphShape.iconContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_description),
                contentDescription = null,
                tint = GlyphTheme.colors.textSecondary,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val displayTitle = note.title.ifBlank { stringResource(StringRes.string.note_untitled) }
            Text(
                text = displayTitle,
                style = GlyphTheme.typography.heading3.copy(color = GlyphTheme.colors.textPrimary),
                maxLines = 1,
            )
            Text(
                text = rememberRelativeTime(note.updatedAt),
                style = GlyphTheme.typography.body.copy(color = GlyphTheme.colors.textSecondary),
                maxLines = 1,
            )
            if (note.tags.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    note.tags.forEach { tag ->
                        HomeNoteTag(tag = tag)
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberRelativeTime(millis: Long): String {
    var now by remember { mutableStateOf(currentTimeDuration().inWholeMilliseconds) }

    LaunchedEffect(millis) {
        while (true) {
            delay(10.seconds)
            now = currentTimeDuration().inWholeMilliseconds
        }
    }

    return formatRelativeTime(millis, now)
}

@Composable
private fun formatRelativeTime(millis: Long, now: Long): String {
    val diff = (now - millis).coerceAtLeast(0)
    val minutes = diff / 60_000
    val hours = diff / 3_600_000
    val days = diff / 86_400_000
    return when {
        minutes < 1 -> stringResource(StringRes.string.time_just_now)
        minutes < 60 -> stringResource(StringRes.string.time_minutes_ago, minutes.toInt())
        hours < 24 -> stringResource(StringRes.string.time_hours_ago, hours.toInt())
        days == 1L -> stringResource(StringRes.string.time_yesterday)
        days < 7 -> stringResource(StringRes.string.time_days_ago, days.toInt())
        else -> formatAbsoluteDate(millis)
    }
}

private fun formatAbsoluteDate(millis: Long): String {
    val totalDays = millis / 86_400_000
    val d = ((totalDays % 31) + 1).toInt().coerceIn(1, 31)
    val m = ((totalDays / 31) % 12 + 1).toInt().coerceIn(1, 12)
    val y = (1970 + totalDays / 365).toInt()
    return "$d.${m.toString().padStart(2, '0')}.$y"
}
