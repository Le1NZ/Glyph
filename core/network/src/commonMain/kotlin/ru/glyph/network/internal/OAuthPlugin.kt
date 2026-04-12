package ru.glyph.network.internal

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import ru.glyph.network.api.TokenProvider

internal fun oAuthPlugin(
    tokenProvider: TokenProvider,
) = createClientPlugin("OAuthPlugin") {
    onRequest { request, _ ->
        if (!request.headers.contains(HttpHeaders.Authorization)) {
            tokenProvider.getToken()?.let { token ->
                request.header("X-Auth-Token", token)
            }
        }
    }
}
