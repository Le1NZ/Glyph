package ru.glyph.ai_bottom_sheet.api

class AiRepository(
    private val aiApiService: AiApiService
) {
    suspend fun generateText(prompt: String, noteContent: String): Result<String> {
        return try {
            val result = aiApiService.generateText(prompt, noteContent)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
