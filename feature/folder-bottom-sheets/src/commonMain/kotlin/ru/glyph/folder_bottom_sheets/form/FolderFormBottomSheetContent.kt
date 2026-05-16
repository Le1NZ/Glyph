package ru.glyph.folder_bottom_sheets.form

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.glyph.design.components.PrimaryButton
import ru.glyph.design.components.SecondaryButton
import ru.glyph.design.theme.GlyphShape
import ru.glyph.design.theme.GlyphTheme
import ru.glyph.navigation.api.model.BottomSheet
import ru.glyph.string.resources.Res
import ru.glyph.string.resources.confirm_bottom_sheet_secondary_action
import ru.glyph.string.resources.folder_form_create_title
import ru.glyph.string.resources.folder_form_name_placeholder
import ru.glyph.string.resources.folder_form_rename_title
import ru.glyph.string.resources.folder_form_save

@Composable
internal fun FolderFormBottomSheetContent(
    presenter: FolderFormPresenter,
    mode: BottomSheet.FolderForm.Mode,
    initialName: String,
    modifier: Modifier = Modifier,
) {
    val colors = GlyphTheme.colors
    val typography = GlyphTheme.typography

    var name by rememberSaveable(initialName) { mutableStateOf(initialName) }

    val titleRes = when (mode) {
        BottomSheet.FolderForm.Mode.Create -> Res.string.folder_form_create_title
        BottomSheet.FolderForm.Mode.Rename -> Res.string.folder_form_rename_title
    }

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(titleRes),
            style = typography.heading1,
            color = colors.textPrimary,
        )

        Spacer(modifier = Modifier.height(20.dp))

        BasicTextField(
            value = name,
            onValueChange = { name = it },
            singleLine = true,
            textStyle = typography.heading3.copy(color = colors.textPrimary),
            decorationBox = { innerTextField ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = GlyphTheme.colors.background, shape = GlyphShape.card)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    if (name.isEmpty()) {
                        Text(
                            text = stringResource(Res.string.folder_form_name_placeholder),
                            style = typography.heading3.copy(
                                color = colors.textSecondary.copy(alpha = 0.5f),
                            ),
                        )
                    }
                    innerTextField()
                }
            },
            modifier = Modifier
                .fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(20.dp))

        PrimaryButton(
            text = stringResource(Res.string.folder_form_save),
            onClick = { presenter.onSave(name) },
            enabled = name.trim().isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        SecondaryButton(
            text = stringResource(Res.string.confirm_bottom_sheet_secondary_action),
            onClick = presenter::onCancel,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
