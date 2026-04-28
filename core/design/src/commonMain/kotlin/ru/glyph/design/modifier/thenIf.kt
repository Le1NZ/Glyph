package ru.glyph.design.modifier

import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
@Stable
inline fun Modifier.thenIf(condition: Boolean, body: () -> Modifier): Modifier {
    contract {
        callsInPlace(body, kind = InvocationKind.AT_MOST_ONCE)
    }
    return then(if (condition) body() else Modifier)
}