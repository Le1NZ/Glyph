package ru.glyph.screen.note.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.glyph.design.Res
import ru.glyph.design.components.LoadingScreen
import ru.glyph.design.ic_arrow_back
import ru.glyph.design.ic_delete
import ru.glyph.design.ic_edit
import ru.glyph.design.ic_format_bold
import ru.glyph.design.ic_format_code
import ru.glyph.design.ic_format_heading
import ru.glyph.design.ic_format_italic
import ru.glyph.design.ic_format_list
import ru.glyph.design.ic_visibility
import ru.glyph.design.padding.localPaddingValues
import ru.glyph.design.theme.GlyphTheme
import ru.glyph.screen.note.ui.NoteScreenViewModel
import ru.glyph.screen.note.ui.state.NoteUiState
import ru.glyph.string.resources.md_bold_cd
import ru.glyph.string.resources.md_code_cd
import ru.glyph.string.resources.md_heading_cd
import ru.glyph.string.resources.md_italic_cd
import ru.glyph.string.resources.md_list_cd
import ru.glyph.string.resources.note_back_cd
import ru.glyph.string.resources.note_delete_cd
import ru.glyph.string.resources.note_edit_cd
import ru.glyph.string.resources.note_placeholder
import ru.glyph.string.resources.note_preview_cd
import ru.glyph.string.resources.note_title_placeholder
import ru.glyph.string.resources.Res as StringRes

@Composable
internal fun NoteScreen(
    viewModel: NoteScreenViewModel,
    modifier: Modifier = Modifier,
) {
    when (val state = viewModel.uiState.collectAsStateWithLifecycle().value) {
        is NoteUiState.Loading -> LoadingScreen()

        is NoteUiState.Editing -> NoteScreenContent(
            state = state,
            onTitleChange = viewModel::onTitleChange,
            onContentChange = viewModel::onContentChange,
            onTogglePreview = viewModel::onTogglePreview,
            onDeleteClick = viewModel::onDeleteClick,
            onBackClick = viewModel::onBackClick,
            modifier = modifier,
        )
    }
}

@Composable
internal fun NoteScreenContent(
    state: NoteUiState.Editing,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onTogglePreview: () -> Unit,
    onDeleteClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var textFieldContent by remember {
        mutableStateOf(TextFieldValue(state.content))
    }

    val bottomPadding = localPaddingValues.calculateBottomPadding()
    val paddingValues = PaddingValues(bottom = bottomPadding)
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GlyphTheme.colors.background)
            .padding(paddingValues)
            .consumeWindowInsets(paddingValues)
            .imePadding(),
    ) {
        NoteTopBar(
            title = state.title,
            isPreviewMode = state.isPreviewMode,
            onBackClick = onBackClick,
            onTogglePreview = onTogglePreview,
            onDeleteClick = onDeleteClick,
        )

        when (state) {
            NoteUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GlyphTheme.colors.accent)
                }
            }

            is NoteUiState.Editing -> {
                if (state.isPreviewMode) {
                    NotePreview(
                        content = state.content,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    )
                } else {
                    NoteEditor(
                        title = state.title,
                        contentTFV = textFieldContent,
                        onTitleChange = onTitleChange,
                        onContentChange = { newTextFieldContent ->
                            val processed = applyMarkdownContinuation(
                                old = textFieldContent,
                                new = newTextFieldContent,
                            )

                            textFieldContent = processed
                            onContentChange(processed.text)
                        },
                        onShortcut = { newTextFieldContent ->
                            textFieldContent = newTextFieldContent
                            onContentChange(newTextFieldContent.text)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun NoteTopBar(
    title: String,
    isPreviewMode: Boolean,
    onBackClick: () -> Unit,
    onTogglePreview: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(GlyphTheme.colors.surface)
            .padding(top = localPaddingValues.calculateTopPadding())
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_back),
                contentDescription = stringResource(StringRes.string.note_back_cd),
                tint = GlyphTheme.colors.textPrimary,
            )
        }

        Text(
            text = title.ifBlank { stringResource(StringRes.string.note_title_placeholder) },
            style = GlyphTheme.typography.heading2.copy(
                color = if (title.isBlank()) {
                    GlyphTheme.colors.textSecondary
                } else {
                    GlyphTheme.colors.textPrimary
                },
            ),
            maxLines = 1,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
        )

        IconButton(onClick = onTogglePreview) {
            Icon(
                painter = painterResource(
                    if (isPreviewMode) Res.drawable.ic_edit else Res.drawable.ic_visibility,
                ),
                contentDescription = stringResource(
                    if (isPreviewMode) StringRes.string.note_edit_cd else StringRes.string.note_preview_cd,
                ),
                tint = GlyphTheme.colors.textPrimary,
                modifier = Modifier.size(20.dp),
            )
        }

        IconButton(onClick = onDeleteClick) {
            Icon(
                painter = painterResource(Res.drawable.ic_delete),
                contentDescription = stringResource(StringRes.string.note_delete_cd),
                tint = GlyphTheme.colors.textPrimary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun NoteEditor(
    title: String,
    contentTFV: TextFieldValue,
    onTitleChange: (String) -> Unit,
    onContentChange: (TextFieldValue) -> Unit,
    onShortcut: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
) {
    val titlePlaceholder = stringResource(StringRes.string.note_title_placeholder)
    val contentPlaceholder = stringResource(StringRes.string.note_placeholder)

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            BasicTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = GlyphTheme.typography.heading1.copy(
                    color = GlyphTheme.colors.textPrimary,
                ),
                cursorBrush = SolidColor(GlyphTheme.colors.accent),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (title.isEmpty()) {
                        Text(
                            text = titlePlaceholder,
                            style = GlyphTheme.typography.heading1.copy(
                                color = GlyphTheme.colors.textSecondary,
                            ),
                        )
                    }
                    innerTextField()
                },
            )
        }

        HorizontalDivider(
            color = GlyphTheme.colors.surfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            BasicTextField(
                value = contentTFV,
                onValueChange = onContentChange,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                textStyle = GlyphTheme.typography.body.copy(
                    color = GlyphTheme.colors.textPrimary,
                ),
                cursorBrush = SolidColor(GlyphTheme.colors.accent),
                decorationBox = { innerTextField ->
                    if (contentTFV.text.isEmpty()) {
                        Text(
                            text = contentPlaceholder,
                            style = GlyphTheme.typography.body.copy(
                                color = GlyphTheme.colors.textSecondary,
                            ),
                        )
                    }
                    innerTextField()
                },
            )
        }

        MarkdownShortcutsBar(
            contentTFV = contentTFV,
            onApply = onShortcut,
        )
    }
}

@Composable
private fun MarkdownShortcutsBar(
    contentTFV: TextFieldValue,
    onApply: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
) {
    HorizontalDivider(color = GlyphTheme.colors.surfaceVariant)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
    ) {
        IconButton(onClick = { onApply(wrapSelection(contentTFV, "**")) }) {
            Icon(
                painter = painterResource(Res.drawable.ic_format_bold),
                contentDescription = stringResource(StringRes.string.md_bold_cd),
                tint = GlyphTheme.colors.textSecondary,
                modifier = Modifier.size(20.dp),
            )
        }
        IconButton(onClick = { onApply(wrapSelection(contentTFV, "*")) }) {
            Icon(
                painter = painterResource(Res.drawable.ic_format_italic),
                contentDescription = stringResource(StringRes.string.md_italic_cd),
                tint = GlyphTheme.colors.textSecondary,
                modifier = Modifier.size(20.dp),
            )
        }
        IconButton(onClick = { onApply(wrapSelection(contentTFV, "`")) }) {
            Icon(
                painter = painterResource(Res.drawable.ic_format_code),
                contentDescription = stringResource(StringRes.string.md_code_cd),
                tint = GlyphTheme.colors.textSecondary,
                modifier = Modifier.size(20.dp),
            )
        }
        IconButton(onClick = { onApply(insertAtLineStart(contentTFV, "- ")) }) {
            Icon(
                painter = painterResource(Res.drawable.ic_format_list),
                contentDescription = stringResource(StringRes.string.md_list_cd),
                tint = GlyphTheme.colors.textSecondary,
                modifier = Modifier.size(20.dp),
            )
        }
        IconButton(onClick = { onApply(insertAtLineStart(contentTFV, "# ")) }) {
            Icon(
                painter = painterResource(Res.drawable.ic_format_heading),
                contentDescription = stringResource(StringRes.string.md_heading_cd),
                tint = GlyphTheme.colors.textSecondary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun NotePreview(
    content: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Markdown(
            content = content,
            colors = markdownColor(
                text = GlyphTheme.colors.textPrimary,
                codeBackground = GlyphTheme.colors.surfaceVariant,
                inlineCodeBackground = GlyphTheme.colors.surfaceVariant,
            ),
            typography = markdownTypography(
                text = GlyphTheme.typography.body,
                h1 = GlyphTheme.typography.heading1,
                h2 = GlyphTheme.typography.heading2,
                h3 = GlyphTheme.typography.heading3,
                paragraph = GlyphTheme.typography.body,
            ),
        )
    }
}

/** Wraps the current selection in [prefix] + [suffix]. If no selection, inserts markers at cursor. */
private fun wrapSelection(
    tfv: TextFieldValue,
    prefix: String,
    suffix: String = prefix
): TextFieldValue {
    val start = tfv.selection.min
    val end = tfv.selection.max
    val newText = buildString {
        append(tfv.text.substring(0, start))
        append(prefix)
        append(tfv.text.substring(start, end))
        append(suffix)
        append(tfv.text.substring(end))
    }
    return TextFieldValue(
        text = newText,
        selection = TextRange(start + prefix.length, end + prefix.length),
    )
}

/** Inserts [prefix] at the beginning of the line the cursor is on. */
private fun insertAtLineStart(tfv: TextFieldValue, prefix: String): TextFieldValue {
    val cursorPos = tfv.selection.start
    val lineStart = tfv.text.lastIndexOf('\n', cursorPos - 1) + 1
    val newText = buildString {
        append(tfv.text.substring(0, lineStart))
        append(prefix)
        append(tfv.text.substring(lineStart))
    }
    return TextFieldValue(
        text = newText,
        selection = TextRange(cursorPos + prefix.length),
    )
}

/**
 * When the user presses Enter after a line that starts with "- ",
 * automatically prepends "- " on the new line.
 */
private fun applyMarkdownContinuation(old: TextFieldValue, new: TextFieldValue): TextFieldValue {
    if (new.text.length <= old.text.length) return new
    val insertPos = new.selection.start
    if (insertPos == 0 || new.text[insertPos - 1] != '\n') return new

    val prevLineStart = old.text.lastIndexOf('\n', old.selection.start - 1) + 1
    val prevLine = old.text.substring(prevLineStart, old.selection.start)
    if (!prevLine.startsWith("- ")) return new

    val newText = new.text.substring(0, insertPos) + "- " + new.text.substring(insertPos)
    return TextFieldValue(newText, TextRange(insertPos + 2))
}
