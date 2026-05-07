package ru.glyph.screen.folder.api.di

import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import ru.glyph.navigation.api.model.Screen
import ru.glyph.screen.folder.ui.FolderScreenViewModel
import ru.glyph.screen.folder.ui.composable.FolderScreen

@OptIn(KoinExperimentalAPI::class)
object FolderScreenLocalDi {

    val module = module {
        factory { (folderId: String) ->
            FolderScreenViewModel(
                folderId = folderId,
                notesRepository = get(),
                foldersRepository = get(),
                navigator = get(),
            )
        }
        navigation<Screen.Folder> { screen ->
            FolderScreen(
                viewModel = koinViewModel<FolderScreenViewModel>(
                    parameters = { parametersOf(screen.folderId) },
                ),
            )
        }
    }
}
