package ru.glyph.di

import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.glyph.AppViewModel
import ru.glyph.auth.api.di.AuthLocalDi
import ru.glyph.confirm_bottom_sheet.di.ConfirmBottomSheetLocalDi
import ru.glyph.database.api.di.DatabaseLocalDi
import ru.glyph.datastore.api.di.DataStoreLocalDi
import ru.glyph.home.HomeDi
import ru.glyph.navigation.NavigationDi
import ru.glyph.network.NetworkDi
import ru.glyph.network.api.di.NetworkLocalDi
import ru.glyph.screen.auth.api.di.AuthScreenDi
import ru.glyph.screen.note.api.di.NoteScreenLocalDi
import ru.glyph.screen.profile.api.di.ProfileScreenDi
import ru.glyph.sync.api.di.SyncLocalDi

internal object AppDi {

    private val appModule = module {
        viewModel { AppViewModel(get(), get(), get()) }
    }

    val modules = listOf(
        appModule,
        NavigationDi.module,
        DataStoreLocalDi.module,
        DatabaseLocalDi.module,
        NetworkLocalDi.module,
        AuthLocalDi.module,
        NetworkDi.module,
        SyncLocalDi.module,
        HomeDi.module,
        AuthScreenDi.module,
        ProfileScreenDi.module,
        NoteScreenLocalDi.module,
        ConfirmBottomSheetLocalDi.module,
    )
}
