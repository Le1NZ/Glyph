package ru.glyph.auth.api

import com.yandex.authsdk.YandexAuthLoginOptions
import com.yandex.authsdk.YandexAuthResult
import kotlinx.coroutines.flow.Flow

interface AndroidAuthLauncher {
    val launchRequests: Flow<YandexAuthLoginOptions>
    fun handleResult(result: YandexAuthResult)
}
