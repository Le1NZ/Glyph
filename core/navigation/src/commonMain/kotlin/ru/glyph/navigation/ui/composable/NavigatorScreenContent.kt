package ru.glyph.navigation.ui.composable

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI
import ru.glyph.design.padding.LocalPaddingValuesProvider
import ru.glyph.navigation.api.model.BaseDestination
import ru.glyph.navigation.ui.NavigatorScreenPresenter
import ru.glyph.navigation.ui.logic.rememberNavigationController
import ru.glyph.navigation.ui.rememberSceneStrategy
import ru.glyph.navigation.ui.state.rememberNavigationState

@Composable
@KoinExperimentalAPI
internal fun NavigatorScreenContent(
    presenter: NavigatorScreenPresenter,
    modifier: Modifier = Modifier,
) {
    val state = rememberNavigationState(
        presenter = presenter,
    )

    val controller = rememberNavigationController(
        presenter = presenter,
        state = state,
    )

    val sceneStrategy = rememberSceneStrategy(
        hideOverlayEvent = controller.hideOverlayEvent,
        onOverlayHidden = controller::onOverlayHidden,
    )

    Scaffold(
        modifier = modifier,
    ) { innerPadding ->
        SharedTransitionLayout {
            CompositionLocalProvider(
                LocalPaddingValuesProvider provides innerPadding,
            ) {
                NavDisplay(
                    backStack = state.stack,
                    entryProvider = koinEntryProvider(),
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator(),
                    ),
                    onBack = controller::onBackFromScreenDestination,
                    sceneStrategy = sceneStrategy,
                    transitionSpec = { pushAnimationSpec() },
                    popTransitionSpec = { popAnimationSpec() },
                    predictivePopTransitionSpec = { popAnimationSpec() },
                )
            }
        }
    }
}
