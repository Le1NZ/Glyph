package ru.glyph.network.api

fun interface TokenProvider {
    suspend fun getToken(): String?
}
