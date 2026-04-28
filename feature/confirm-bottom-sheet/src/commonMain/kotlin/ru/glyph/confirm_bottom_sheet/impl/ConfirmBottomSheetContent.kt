package ru.glyph.confirm_bottom_sheet.impl

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.glyph.design.components.PrimaryButton
import ru.glyph.design.components.SecondaryButton
import ru.glyph.design.theme.GlyphTheme
import ru.glyph.string.resources.Res
import ru.glyph.string.resources.confirm_bottom_sheet_primary_action
import ru.glyph.string.resources.confirm_bottom_sheet_secondary_action

@Composable
internal fun ConfirmBottomSheetContent(
    presenter: ConfirmBottomPresenter,
    text: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = text,
            style = GlyphTheme.typography.heading1,
            color = GlyphTheme.colors.textPrimary,
        )

        Spacer(modifier = Modifier.height(20.dp))

        PrimaryButton(
            text = stringResource(Res.string.confirm_bottom_sheet_primary_action),
            onClick = presenter::onConfirm,
            modifier = Modifier
                .fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        SecondaryButton(
            text = stringResource(Res.string.confirm_bottom_sheet_secondary_action),
            onClick = presenter::onCancel,
            modifier = Modifier
                .fillMaxWidth(),
        )
    }
}

@Composable
@Preview
private fun ConfirmBottomSheetContentPreview() {
    GlyphTheme {
        ConfirmBottomSheetContent(
            presenter = ConfirmBottomPresenterPreview(),
            text = "Confirm?",
        )
    }
}