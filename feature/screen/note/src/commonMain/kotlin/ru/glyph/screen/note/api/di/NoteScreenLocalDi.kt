package ru.glyph.screen.note.api.di

import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import ru.glyph.navigation.api.model.Screen
import ru.glyph.screen.note.ui.NoteScreenViewModel
import ru.glyph.screen.note.ui.composable.NoteScreen

@OptIn(KoinExperimentalAPI::class)
object NoteScreenLocalDi {

    val module = module {
        factory { (noteId: String) -> NoteScreenViewModel(
            noteId = noteId,
            notesRepository = get(),
            navigator = get(),
        ) }
        navigation<Screen.Note> { screen ->
            NoteScreen(
                viewModel = koinViewModel<NoteScreenViewModel>(
                    parameters = { parametersOf(screen.noteId) },
                ),
            )
        }
    }
}
