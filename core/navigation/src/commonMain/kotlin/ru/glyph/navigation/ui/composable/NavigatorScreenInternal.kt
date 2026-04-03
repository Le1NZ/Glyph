package ru.glyph.navigation.ui.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import ru.glyph.navigation.api.deps.NavigationDependencies
import ru.glyph.navigation.ui.NavigatorScreenPresenterImpl
import ru.glyph.navigation.ui.NavigatorScreenViewModel

@OptIn(KoinExperimentalAPI::class)
@Composable
internal fun NavigatorScreenInternal(
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<NavigatorScreenViewModel>()
    val dependencies = koinInject<NavigationDependencies>()
    val presenter = remember(viewModel) {
        NavigatorScreenPresenterImpl(
            viewModel = viewModel,
            initialDestinationsProvider = dependencies.initialDestinationsProvider,
        )
    }

    NavigatorScreenContent(
        presenter = presenter,
        modifier = modifier,
    )
}
