package ru.glyph.utils

fun <T> Result<T>.toConvertedResult(): ConvertedResult<T> {
    return map {
        ConvertedResult.Success(it)
    }.getOrElse {
        ConvertedResult.Error
    }
}