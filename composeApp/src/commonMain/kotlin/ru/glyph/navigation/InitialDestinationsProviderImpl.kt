package ru.glyph.navigation

import ru.glyph.auth.api.UserCenter
import ru.glyph.auth.api.model.UserState
import ru.glyph.navigation.api.deps.InitialDestinationsProvider
import ru.glyph.navigation.api.model.BaseDestination
import ru.glyph.navigation.api.model.Screen

internal class InitialDestinationsProviderImpl(
    userCenterLazy: Lazy<UserCenter>,
) : InitialDestinationsProvider {

    private val userCenter by userCenterLazy

    override fun initialDestinations(): List<BaseDestination> {
        val screen = when (userCenter.authState.value) {
            UserState.Authorized -> Screen.Home
            UserState.NotAuthorized -> Screen.Auth
        }

        return listOf(screen)
    }
}