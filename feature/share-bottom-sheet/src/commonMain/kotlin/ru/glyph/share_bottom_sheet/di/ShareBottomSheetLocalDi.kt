package ru.glyph.share_bottom_sheet.di

import io.ktor.client.HttpClient
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.glyph.navigation.api.di.bottomSheet
import ru.glyph.navigation.api.model.BottomSheet
import ru.glyph.navigation.api.model.BottomSheetMeta
import ru.glyph.network.api.ApiConfig
import ru.glyph.share_bottom_sheet.api.ShareNoteBottomSheet
import ru.glyph.share_bottom_sheet.impl.ShareApiService
import ru.glyph.share_bottom_sheet.impl.ShareApiServiceImpl
import ru.glyph.share_bottom_sheet.impl.ShareNotePresenter
import ru.glyph.share_bottom_sheet.impl.ShareNotePresenterImpl
import ru.glyph.share_bottom_sheet.impl.ShareNoteRepository
import ru.glyph.share_bottom_sheet.impl.ShareNoteRepositoryImpl
import ru.glyph.share_bottom_sheet.impl.ShareNoteViewModel

object ShareBottomSheetLocalDi {
    val module = module {
        single {
            ShareApiServiceImpl(
                client = get<HttpClient>(),
                baseUrl = get<ApiConfig>().baseUrl,
            )
        } bind ShareApiService::class

        factoryOf(::ShareNoteRepositoryImpl) bind ShareNoteRepository::class

        viewModel { params ->
            ShareNoteViewModel(
                noteId = params.get(),
                repository = get(),
            )
        }

        factory {
            ShareNotePresenter.Factory { viewModel ->
                ShareNotePresenterImpl(
                    viewModel = viewModel,
                )
            }
        }

        bottomSheet<BottomSheet.ShareNote>(
            meta = BottomSheetMeta(skipPartiallyExpanded = true),
        ) { bottomSheet ->
            ShareNoteBottomSheet(
                overlay = bottomSheet
            )
        }
    }
}