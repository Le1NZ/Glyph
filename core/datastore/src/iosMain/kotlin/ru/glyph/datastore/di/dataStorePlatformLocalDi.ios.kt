package ru.glyph.datastore.di

import org.koin.dsl.module
import ru.glyph.datastore.api.DataStoreFactory
import ru.glyph.datastore.internal.IosDataStoreFactory

internal actual fun dataStorePlatformLocalDi() = module {
    single<DataStoreFactory> { IosDataStoreFactory() }
}