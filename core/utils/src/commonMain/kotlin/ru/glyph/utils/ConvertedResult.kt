package ru.glyph.utils

sealed interface ConvertedResult<out T> {

    data object Error : ConvertedResult<Nothing>

    data class Success<T>(
        val value: T,
    ) : ConvertedResult<T>
}