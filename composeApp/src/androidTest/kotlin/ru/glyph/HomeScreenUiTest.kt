package ru.glyph

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Test
import ru.glyph.infra.GlyphUiTest

class HomeScreenUiTest : GlyphUiTest() {

    @Test
    fun app_startsOnHomeScreen_whenUserIsAuthorized() {
        startApp()
        onNodeWithContentDescription("Создать заметку").assertIsDisplayed()
    }

    @Test
    fun createNote_appearsInHomeListAsUntitled() {
        startApp()
        onNodeWithContentDescription("Создать заметку").performClick()
        waitForIdle()
        onNodeWithContentDescription("Назад").performClick()
        waitForIdle()
        onNodeWithText("Новая заметка").assertIsDisplayed()
    }

    @Test
    fun createNote_withTitle_appearsInHomeListWithTitle() {
        startApp()
        onNodeWithContentDescription("Создать заметку").performClick()
        waitForIdle()
        onNodeWithText("Начни писать...").performClick()
        onNodeWithText("Начни писать...").performTextInput("Моя тестовая заметка")
        waitForIdle()
        onNodeWithContentDescription("Назад").performClick()
        waitForIdle()
        onNodeWithText("Моя тестовая заметка").assertIsDisplayed()
    }

    @Test
    fun noteCard_click_opensNoteScreen() {
        startApp()
        onNodeWithContentDescription("Создать заметку").performClick()
        waitForIdle()
        onNodeWithContentDescription("Назад").performClick()
        waitForIdle()
        onNodeWithText("Новая заметка").performClick()
        waitForIdle()
        onNodeWithContentDescription("Удалить заметку").assertIsDisplayed()
    }
}
