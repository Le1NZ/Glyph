package ru.glyph.ai_bottom_sheet.api

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
