package ru.glyph.datastore.api.di

import org.koin.core.module.Module
import org.koin.dsl.module
import ru.glyph.datastore.di.dataStorePlatformLocalDi

object DataStoreLocalDi {
    val module: Module = module {
        includes(dataStorePlatformLocalDi())
    }
}
