package ru.glyph.datastore.api

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

fun interface DataStoreFactory {

    fun create(name: String): DataStore<Preferences>

    companion object {

        internal const val FILE_NAME_SUFFIX = "preferences_pb"
    }
}
