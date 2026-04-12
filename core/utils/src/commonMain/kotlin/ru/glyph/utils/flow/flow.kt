package ru.glyph.utils.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

fun <T> Flow<T>.collectIn(
    scope: CoroutineScope,
    collector: FlowCollector<T>,
): Job = scope.launch {
    collect(collector)
}

inline fun <T> Flow<T>.collectLatestIn(
    scope: CoroutineScope,
    crossinline action: suspend (value: T) -> Unit,
): Job {
    return scope.launch { collectLatest { action(it) } }
}

fun <T> Flow<T>.windowedWithPrevious(initial: T): Flow<Pair<T, T>> = flow {
    var previous: T = initial
    collect { current ->
        emit(previous to current)
        previous = current
    }
}

fun <T> StateFlow<T>.windowedWithPrevious(): Flow<Pair<T, T>> = flow {
    var previous: T = value
    collect { current ->
        emit(previous to current)
        previous = current
    }
}

fun <T> Flow<T>.debounceWithPrevious(initial: T, debouncer: (prev: T, curr: T) -> Long): Flow<T> {
    var prev: T = initial
    return this
        .debounce { curr -> debouncer(prev, curr) }
        .onEach { prev = it }
}

inline fun <T, K1, K2> Flow<T>.distinctUntilChangedBy(
    crossinline keySelector1: (T) -> K1,
    crossinline keySelector2: (T) -> K2
): Flow<T> {
    return distinctUntilChanged { old, new ->
        keySelector1.invoke(old) == keySelector1.invoke(new) &&
                keySelector2.invoke(old) == keySelector2.invoke(new)
    }
}
