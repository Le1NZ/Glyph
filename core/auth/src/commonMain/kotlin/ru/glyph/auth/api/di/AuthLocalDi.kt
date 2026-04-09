package ru.glyph.auth.api.di

import org.koin.core.module.Module
import org.koin.dsl.module
import ru.glyph.auth.api.UserCenter
import ru.glyph.auth.api.UserInfoUseCase
import ru.glyph.auth.di.authPlatformModule
import ru.glyph.auth.internal.UserCenterImpl
import ru.glyph.auth.internal.UserInfoUseCaseImpl
import ru.glyph.auth.internal.network.YandexUserInfoService
import ru.glyph.auth.internal.repository.UserInfoRepository
import ru.glyph.auth.internal.repository.UserInfoRepositoryImpl
import ru.glyph.auth.internal.storage.AuthTokenStorage
import ru.glyph.auth.internal.storage.AuthTokenStorageImpl

object AuthLocalDi {
    val module: Module = module {
        includes(authPlatformModule())
        single<AuthTokenStorage> { AuthTokenStorageImpl(get()) }
        single { YandexUserInfoService(get()) }
        single<UserInfoRepository> { UserInfoRepositoryImpl(inject()) }
        single<UserInfoUseCase> { UserInfoUseCaseImpl(inject()) }

        single<UserCenter> {
            UserCenterImpl(
                platformAuthProvider = get(),
                tokenStorage = get(),
            )
        }
    }
}

