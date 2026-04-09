package ru.glyph.auth.internal.auth

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AuthenticationServices.ASWebAuthenticationPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASWebAuthenticationSession
import platform.Foundation.NSBundle
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import kotlin.coroutines.resume

private val CLIENT_ID: String = NSBundle.mainBundle
    .objectForInfoDictionaryKey("YANDEX_CLIENT_ID") as? String
    ?: error("YANDEX_CLIENT_ID is not set in Info.plist")

private val REDIRECT_URI = "yx${CLIENT_ID}://authorize"

internal class YandexIosAuthProvider : PlatformAuthProvider {

    override suspend fun authenticate(): PlatformAuthResult =
        suspendCancellableCoroutine { continuation ->
            val session = buildAuthSession(
                onResult = { continuation.resume(it) },
            )
            session.presentationContextProvider = IosAuthPresentationContext()
            session.prefersEphemeralWebBrowserSession = false

            if (!session.start()) {
                continuation.resume(PlatformAuthResult.Error(IllegalStateException("Failed to start auth session")))
            }

            continuation.invokeOnCancellation { session.cancel() }
        }

    private fun buildAuthSession(
        onResult: (PlatformAuthResult) -> Unit,
    ): ASWebAuthenticationSession {
        val url = NSURL(string = buildAuthUrl())
        return ASWebAuthenticationSession(
            uRL = url,
            callbackURLScheme = "yx$CLIENT_ID",
        ) { callbackUrl, error ->
            val result = when {
                error != null -> handleError(error.code)
                callbackUrl != null -> parseCallback(callbackUrl)
                else -> PlatformAuthResult.Error(IllegalStateException("Unknown auth error"))
            }
            onResult(result)
        }
    }

    private fun buildAuthUrl(): String {
        return "https://oauth.yandex.ru/authorize" +
                "?response_type=token" +
                "&client_id=$CLIENT_ID" +
                "&redirect_uri=${NSURL(string = REDIRECT_URI)}" +
                "&display=touch"
    }

    private fun handleError(code: Long) = if (code == 1L) {
        PlatformAuthResult.Cancelled
    } else {
        PlatformAuthResult.Error(IllegalStateException("Auth error code: $code"))
    }

    private fun parseCallback(callbackUrl: NSURL): PlatformAuthResult {
        val params = callbackUrl.fragment
            ?.split("&")
            ?.associate { it.substringBefore("=") to it.substringAfter("=") }
            ?: return PlatformAuthResult.Error(IllegalStateException("No fragment in callback URL"))

        val token = params["access_token"]
            ?: return PlatformAuthResult.Error(IllegalStateException("No access_token in callback"))

        val expiresIn = params["expires_in"]?.toLongOrNull() ?: 3600L
        return PlatformAuthResult.Success(token, expiresIn)
    }
}

private class IosAuthPresentationContext :
    NSObject(),
    ASWebAuthenticationPresentationContextProvidingProtocol {

    override fun presentationAnchorForWebAuthenticationSession(
        session: ASWebAuthenticationSession,
    ): UIWindow = UIApplication.sharedApplication.keyWindow
        ?: UIApplication.sharedApplication.windows.filterIsInstance<UIWindow>().firstOrNull()
        ?: UIWindow()
}
