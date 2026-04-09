package ru.glyph.auth.internal.auth

import com.yandex.authsdk.YandexAuthLoginOptions
import com.yandex.authsdk.YandexAuthResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import ru.glyph.auth.api.AndroidAuthLauncher

internal class YandexAndroidAuthProvider : PlatformAuthProvider, AndroidAuthLauncher {

    private val launchChannel = Channel<YandexAuthLoginOptions>()
    private val resultChannel = Channel<PlatformAuthResult>(capacity = 1)

    override val launchRequests = launchChannel.receiveAsFlow()

    override suspend fun authenticate(): PlatformAuthResult {
        launchChannel.send(YandexAuthLoginOptions())
        return resultChannel.receive()
    }

    override fun handleResult(result: YandexAuthResult) {
        resultChannel.trySend(result.toPlatformResult())
    }

    private fun YandexAuthResult.toPlatformResult(): PlatformAuthResult = when (this) {
        is YandexAuthResult.Success -> PlatformAuthResult.Success(
            token = token.value,
            expiresIn = token.expiresIn,
        )

        is YandexAuthResult.Failure -> PlatformAuthResult.Error(exception)
        YandexAuthResult.Cancelled -> PlatformAuthResult.Cancelled
    }
}
