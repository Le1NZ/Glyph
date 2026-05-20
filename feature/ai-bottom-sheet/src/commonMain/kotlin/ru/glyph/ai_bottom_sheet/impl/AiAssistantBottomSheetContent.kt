package ru.glyph.ai_bottom_sheet.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.glyph.design.components.PrimaryButton
import ru.glyph.design.components.SecondaryButton
import ru.glyph.design.ic_send
import ru.glyph.design.theme.GlyphFolderColors
import ru.glyph.design.theme.GlyphTheme
import ru.glyph.string.resources.Res
import ru.glyph.string.resources.ai_assistant_disclaimer
import ru.glyph.string.resources.ai_assistant_greeting
import ru.glyph.string.resources.ai_assistant_insert_button
import ru.glyph.string.resources.ai_assistant_loading
import ru.glyph.string.resources.ai_assistant_prompt_hint
import ru.glyph.string.resources.ai_assistant_quick_fix
import ru.glyph.string.resources.ai_assistant_quick_shorter
import ru.glyph.string.resources.ai_assistant_quick_translate
import ru.glyph.string.resources.ai_assistant_retry_button
import ru.glyph.string.resources.ai_assistant_title
import ru.glyph.design.Res as DesignRes

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AiAssistantBottomSheetContent(
    presenter: AiAssistantBottomSheetPresenter,
    state: AiAssistantViewModel.State,
    prompt: String,
    onPromptChange: (String) -> Unit,
    onGenerate: (String?) -> Unit,
    onReset: () -> Unit,
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp, top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.ai_assistant_title),
            style = GlyphTheme.typography.heading1,
            color = GlyphTheme.colors.textPrimary,
        )

        Spacer(modifier = Modifier.height(24.dp))

        when (state) {
            is AiAssistantViewModel.State.Idle -> {
                Text(
                    text = stringResource(Res.string.ai_assistant_greeting),
                    style = GlyphTheme.typography.body,
                    color = GlyphTheme.colors.textPrimary,
                )

                Spacer(modifier = Modifier.height(24.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionChip(stringResource(Res.string.ai_assistant_quick_shorter)) {
                        onGenerate(it)
                    }
                    QuickActionChip(stringResource(Res.string.ai_assistant_quick_fix)) {
                        onGenerate(it)
                    }
                    QuickActionChip(stringResource(Res.string.ai_assistant_quick_translate)) {
                        onGenerate(it)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(GlyphTheme.colors.background)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        BasicTextField(
                            value = prompt,
                            onValueChange = onPromptChange,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = GlyphTheme.typography.body.copy(color = GlyphTheme.colors.textPrimary),
                            cursorBrush = SolidColor(GlyphTheme.colors.accent),
                            maxLines = 3,
                            decorationBox = { innerTextField ->
                                if (prompt.isEmpty()) {
                                    Text(
                                        text = stringResource(Res.string.ai_assistant_prompt_hint),
                                        style = GlyphTheme.typography.body.copy(color = GlyphTheme.colors.textSecondary)
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(GlyphTheme.colors.accent)
                            .clickable(enabled = prompt.isNotBlank()) {
                                onGenerate(null)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(DesignRes.drawable.ic_send),
                            contentDescription = null,
                            tint = GlyphTheme.colors.contentOnAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = stringResource(Res.string.ai_assistant_disclaimer),
                    style = GlyphTheme.typography.caption,
                    color = GlyphTheme.colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
            is AiAssistantViewModel.State.Loading -> {
                CircularProgressIndicator(color = GlyphTheme.colors.accent)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(Res.string.ai_assistant_loading),
                    style = GlyphTheme.typography.body,
                    color = GlyphTheme.colors.textPrimary
                )
            }
            is AiAssistantViewModel.State.Success -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlyphTheme.colors.background)
                        .padding(16.dp)
                ) {
                    Text(
                        text = state.generatedText,
                        style = GlyphTheme.typography.body,
                        color = GlyphTheme.colors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SecondaryButton(
                        text = "Отмена",
                        onClick = onReset,
                        modifier = Modifier.weight(1f),
                    )
                    PrimaryButton(
                        text = stringResource(Res.string.ai_assistant_insert_button),
                        onClick = { presenter.onInsertText(state.generatedText) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            is AiAssistantViewModel.State.Error -> {
                Text(
                    text = state.message,
                    style = GlyphTheme.typography.body,
                    color = GlyphFolderColors.Red
                )
                Spacer(modifier = Modifier.height(24.dp))
                PrimaryButton(
                    text = stringResource(Res.string.ai_assistant_retry_button),
                    onClick = onReset,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun QuickActionChip(
    text: String,
    onClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GlyphTheme.colors.background)
            .clickable { onClick(text) }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = GlyphTheme.typography.body,
            color = GlyphTheme.colors.textPrimary
        )
    }
}
