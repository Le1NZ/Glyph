package ru.glyph.tag_bottom_sheets.form

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.glyph.design.Res as DesignRes
import ru.glyph.design.ic_check
import ru.glyph.design.components.PrimaryButton
import ru.glyph.design.components.SecondaryButton
import ru.glyph.design.theme.GlyphShape
import ru.glyph.design.theme.GlyphTheme
import ru.glyph.design.theme.toGlyphColor
import ru.glyph.model.FolderColor
import ru.glyph.navigation.api.model.BottomSheet
import ru.glyph.string.resources.Res
import ru.glyph.string.resources.confirm_bottom_sheet_secondary_action
import ru.glyph.string.resources.tag_form_color
import ru.glyph.string.resources.tag_form_create_title
import ru.glyph.string.resources.tag_form_edit_title
import ru.glyph.string.resources.tag_form_name_placeholder
import ru.glyph.string.resources.tag_form_save

@Composable
internal fun TagFormBottomSheetContent(
    presenter: TagFormPresenter,
    mode: BottomSheet.TagForm.Mode,
    initialName: String,
    initialColor: FolderColor,
    modifier: Modifier = Modifier,
) {
    val colors = GlyphTheme.colors
    val typography = GlyphTheme.typography

    var name by rememberSaveable(initialName) { mutableStateOf(initialName) }
    var color by rememberSaveable(initialColor) { mutableStateOf(initialColor) }

    val titleRes = when (mode) {
        BottomSheet.TagForm.Mode.Create -> Res.string.tag_form_create_title
        BottomSheet.TagForm.Mode.Edit -> Res.string.tag_form_edit_title
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
                        .background(color = colors.surfaceVariant, shape = GlyphShape.card)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    if (name.isEmpty()) {
                        Text(
                            text = stringResource(Res.string.tag_form_name_placeholder),
                            style = typography.heading3.copy(
                                color = colors.textSecondary.copy(alpha = 0.5f),
                            ),
                        )
                    }
                    innerTextField()
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Color Picker
        Text(
            text = stringResource(Res.string.tag_form_color),
            style = typography.heading3,
            color = colors.textPrimary,
            modifier = Modifier.align(Alignment.Start)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            items(FolderColor.entries.toTypedArray()) { folderColor ->
                val isSelected = color == folderColor
                val glyphColor = folderColor.toGlyphColor()
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(glyphColor)
                        .clickable { color = folderColor },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            painter = painterResource(DesignRes.drawable.ic_check),
                            contentDescription = null,
                            tint = colors.contentOnAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryButton(
            text = stringResource(Res.string.tag_form_save),
            onClick = { presenter.onSave(name, color) },
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
