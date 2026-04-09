package ru.glyph.network

import org.koin.dsl.module
import ru.glyph.auth.api.UserCenter
import ru.glyph.network.api.TokenProvider

internal object NetworkDi {

    val module = module {
        single<TokenProvider> { TokenProvider { get<UserCenter>().getToken() } }
    }
}
