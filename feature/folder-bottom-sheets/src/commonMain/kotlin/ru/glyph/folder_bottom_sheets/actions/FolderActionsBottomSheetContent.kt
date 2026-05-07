package ru.glyph.folder_bottom_sheets.actions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.glyph.design.components.PrimaryButton
import ru.glyph.design.components.SecondaryButton
import ru.glyph.design.theme.GlyphTheme
import ru.glyph.model.Folder
import ru.glyph.string.resources.Res
import ru.glyph.string.resources.folder_actions_delete
import ru.glyph.string.resources.folder_actions_rename

@Composable
internal fun FolderActionsBottomSheetContent(
    presenter: FolderActionsPresenter,
    folder: Folder,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = folder.name,
            style = GlyphTheme.typography.heading1,
            color = GlyphTheme.colors.textPrimary,
        )

        Spacer(modifier = Modifier.height(20.dp))

        SecondaryButton(
            text = stringResource(Res.string.folder_actions_rename),
            onClick = presenter::onRename,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        PrimaryButton(
            text = stringResource(Res.string.folder_actions_delete),
            onClick = presenter::onDelete,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
