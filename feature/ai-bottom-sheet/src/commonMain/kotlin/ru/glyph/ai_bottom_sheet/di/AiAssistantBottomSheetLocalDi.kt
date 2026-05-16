package ru.glyph.ai_bottom_sheet.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.compose.koinInject
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import ru.glyph.ai_bottom_sheet.api.AiApiService
import ru.glyph.ai_bottom_sheet.api.AiRepository
import ru.glyph.ai_bottom_sheet.impl.AiAssistantBottomSheetInternal
import ru.glyph.ai_bottom_sheet.impl.AiAssistantBottomSheetPresenter
import ru.glyph.ai_bottom_sheet.impl.AiAssistantBottomSheetPresenterImpl
import ru.glyph.ai_bottom_sheet.impl.AiAssistantViewModel
import ru.glyph.navigation.api.di.bottomSheet
import ru.glyph.navigation.api.model.BottomSheet
import ru.glyph.navigation.api.model.BottomSheetMeta

@OptIn(KoinExperimentalAPI::class)
object AiAssistantBottomSheetLocalDi {

    val module = module {
        factory { AiApiService(get(), get<ru.glyph.network.api.ApiConfig>()) }
        factory { AiRepository(get()) }
        
        factory {
            AiAssistantViewModel(
                aiRepository = get(),
                coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
            )
        }

        factory {
            AiAssistantBottomSheetPresenter.Factory { onInsertText ->
                AiAssistantBottomSheetPresenterImpl(
                    onInsertText = onInsertText,
                    navigatorLazy = inject(),
                )
            }
        }

        bottomSheet<BottomSheet.AiAssistant>(
            meta = BottomSheetMeta(skipPartiallyExpanded = true),
        ) { bottomSheet ->
            AiAssistantBottomSheetInternal(
                presenterFactory = koinInject(),
                viewModel = koinInject(),
                noteContent = bottomSheet.noteContent,
                onInsertText = bottomSheet.onInsertText,
            )
        }
    }
}
