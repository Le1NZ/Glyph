package ru.glyph.infra

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import org.junit.Rule
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import ru.glyph.AppContent
import ru.glyph.auth.api.UserCenter
import ru.glyph.di.AppDi

@RunWith(AndroidJUnit4::class)
abstract class GlyphUiTest {

    @get:Rule
    val compose: ComposeContentTestRule = createComposeRule()

    fun startApp(vararg extraModules: Module) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        compose.setContent {
            KoinApplication(configuration = koinConfiguration {
                androidContext(context)
                allowOverride(true)
                modules(AppDi.modules + mockModule() + extraModules.toList())
            }) {
                AppContent()
            }
        }
        compose.waitForIdle()
    }

    fun waitForIdle() = compose.waitForIdle()
    fun onNodeWithText(text: String) = compose.onNodeWithText(text)
    fun onNodeWithContentDescription(label: String) = compose.onNodeWithContentDescription(label)

    private fun mockModule() = module {
        single<UserCenter> { MockUserCenter() }
        single<HttpClient> {
            HttpClient(MockEngine.Companion {
                respond(
                    "",
                    HttpStatusCode.Companion.ServiceUnavailable
                )
            })
        }
    }
}