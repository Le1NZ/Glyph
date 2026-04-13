package ru.glyph

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performClick
import org.junit.Test
import ru.glyph.infra.GlyphUiTest

class NoteScreenUiTest : GlyphUiTest() {

    private fun openNewNote() {
        onNodeWithContentDescription("Создать заметку").performClick()
        waitForIdle()
    }

    @Test
    fun fab_click_navigatesToNoteScreen() {
        startApp()
        openNewNote()
        onNodeWithContentDescription("Назад").assertIsDisplayed()
    }

    @Test
    fun backButton_returnsToHomeScreen() {
        startApp()
        openNewNote()
        onNodeWithContentDescription("Назад").performClick()
        waitForIdle()
        onNodeWithContentDescription("Создать заметку").assertIsDisplayed()
    }

    @Test
    fun deleteButton_deletesNoteAndReturnsToHome() {
        startApp()
        openNewNote()
        onNodeWithContentDescription("Удалить заметку").performClick()
        waitForIdle()
        onNodeWithContentDescription("Создать заметку").assertIsDisplayed()
    }

    @Test
    fun noteScreen_showsPreviewToggleInEditMode() {
        startApp()
        openNewNote()
        onNodeWithContentDescription("Предпросмотр").assertIsDisplayed()
    }

    @Test
    fun previewToggle_switchesToPreviewMode() {
        startApp()
        openNewNote()
        onNodeWithContentDescription("Предпросмотр").performClick()
        waitForIdle()
        onNodeWithContentDescription("Редактировать").assertIsDisplayed()
    }

    @Test
    fun editToggle_returnsToEditMode() {
        startApp()
        openNewNote()
        onNodeWithContentDescription("Предпросмотр").performClick()
        waitForIdle()
        onNodeWithContentDescription("Редактировать").performClick()
        waitForIdle()
        onNodeWithContentDescription("Предпросмотр").assertIsDisplayed()
    }
}
