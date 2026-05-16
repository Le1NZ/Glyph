package ru.glyph.server.model

import kotlinx.serialization.Serializable

@Serializable
data class AiGenerateRequest(
    val prompt: String,
    val noteContent: String
)

@Serializable
data class AiGenerateResponse(
    val generatedText: String? = null,
    val error: String? = null
)
