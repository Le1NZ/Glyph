package ru.glyph.datastore.internal

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import ru.glyph.datastore.api.DataStoreFactory

internal class IosDataStoreFactory : DataStoreFactory {

    @OptIn(ExperimentalForeignApi::class)
    override fun create(name: String): DataStore<Preferences> {
        val directory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        val path = requireNotNull(directory?.path) { "Could not get document directory path" }
        return PreferenceDataStoreFactory.createWithPath(
            produceFile = { "$path/$name.${DataStoreFactory.FILE_NAME_SUFFIX}".toPath() }
        )
    }
}
