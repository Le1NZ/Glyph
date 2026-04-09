package ru.glyph

import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.yandex.authsdk.YandexAuthOptions
import com.yandex.authsdk.YandexAuthSdk
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.koinConfiguration
import ru.glyph.auth.api.AndroidAuthLauncher
import ru.glyph.di.AppDi

@Composable
actual fun App() {
    val context = LocalContext.current
    KoinApplication(
        configuration = koinConfiguration(
            declaration = {
                androidContext(context)
                modules(AppDi.modules)
            },
        ),
    ) {
        AppContent()
    }
}

@Composable
internal actual fun PlatformAuthEffect() {
    val activity = LocalActivity.current ?: return
    val launcher: AndroidAuthLauncher = koinInject()
    val sdk = remember(activity) { YandexAuthSdk.create(YandexAuthOptions(context = activity)) }
    val activityLauncher = rememberLauncherForActivityResult(sdk.contract) {
        launcher.handleResult(it)
    }

    LaunchedEffect(activityLauncher) {
        launcher.launchRequests.collect { options ->
            activityLauncher.launch(options)
        }
    }
}
