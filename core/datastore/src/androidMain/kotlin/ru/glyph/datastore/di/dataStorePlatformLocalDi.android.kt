package ru.glyph.datastore.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ru.glyph.datastore.api.DataStoreFactory
import ru.glyph.datastore.internal.AndroidDataStoreFactory

internal actual fun dataStorePlatformLocalDi() = module {
    single<DataStoreFactory> { AndroidDataStoreFactory(context = androidContext()) }
}