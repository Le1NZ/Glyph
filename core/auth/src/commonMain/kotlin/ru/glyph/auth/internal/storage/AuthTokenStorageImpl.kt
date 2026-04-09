package ru.glyph.auth.internal.storage

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import ru.glyph.datastore.api.DataStoreFactory

internal class AuthTokenStorageImpl(
    private val dataStoreFactory: DataStoreFactory,
) : AuthTokenStorage {

    private val dataStore by lazy { dataStoreFactory.create("auth_token_storage") }

    private val tokenKey = stringPreferencesKey("auth_token")
    private val serializer = AuthToken.serializer()

    override val token = dataStore.data.map { prefs ->
        prefs[tokenKey]?.let { tokenString ->
            Json.decodeFromString(serializer, tokenString)
        }
    }

    override fun getTokenBlocking(): AuthToken? = runBlocking {
        token.first()
    }

    override suspend fun saveToken(token: AuthToken) {
        dataStore.edit { prefs ->
            prefs[tokenKey] = Json.encodeToString(serializer, token)
        }
    }

    override suspend fun clearToken() {
        dataStore.edit { prefs -> prefs.remove(tokenKey) }
    }
}
