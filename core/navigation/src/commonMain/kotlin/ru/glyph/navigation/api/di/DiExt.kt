package ru.glyph.navigation.api.di

import androidx.compose.runtime.Composable
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import org.koin.dsl.navigation3.navigation
import ru.glyph.navigation.api.model.BottomSheetMeta
import ru.glyph.navigation.api.model.bottomSheetMetadata

@OptIn(KoinExperimentalAPI::class)
inline fun <reified T : Any> Module.bottomSheet(
    meta: BottomSheetMeta,
    metadata: Map<String, Any> = emptyMap(),
    noinline definition: @Composable Scope.(T) -> Unit,
) {
    navigation<T>(
        metadata = metadata + bottomSheetMetadata(meta),
        definition = definition,
    )
}
