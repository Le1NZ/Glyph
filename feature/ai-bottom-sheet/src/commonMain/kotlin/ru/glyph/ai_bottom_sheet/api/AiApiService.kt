package ru.glyph.ai_bottom_sheet.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

import ru.glyph.network.api.ApiConfig

import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

class AiApiService(
    private val httpClient: HttpClient,
    private val apiConfig: ApiConfig
) {
    suspend fun generateText(prompt: String, noteContent: String): String {
        val request = AiGenerateRequest(prompt, noteContent)
        val response = httpClient.post("${apiConfig.baseUrl}/api/ai/generate") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        
        if (!response.status.isSuccess()) {
            val errorBody = try { response.bodyAsText() } catch (e: Exception) { "No body" }
            throw Exception("Server error ${response.status.value}: $errorBody")
        }
        
        val aiResponse = response.body<AiGenerateResponse>()
        if (aiResponse.error != null) {
            throw Exception(aiResponse.error)
        }
        return aiResponse.generatedText ?: throw Exception("Empty response from AI")
    }
}
