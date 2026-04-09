package ru.glyph.datastore.internal

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import ru.glyph.datastore.api.DataStoreFactory

internal class AndroidDataStoreFactory(
    private val context: Context,
) : DataStoreFactory {

    override fun create(name: String): DataStore<Preferences> {
        return PreferenceDataStoreFactory.createWithPath(
            produceFile = {
                context.filesDir
                    .resolve("$name.${DataStoreFactory.FILE_NAME_SUFFIX}")
                    .absolutePath
                    .toPath()
            },
        )
    }
}
