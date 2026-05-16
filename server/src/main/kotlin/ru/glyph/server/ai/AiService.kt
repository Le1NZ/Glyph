package ru.glyph.server.ai

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import ru.glyph.server.model.AiGenerateRequest
import ru.glyph.server.model.AiGenerateResponse

class AiService {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
    }

    private val apiKey = System.getenv("OPENROUTER_API_KEY") ?: ""
    private val model = "google/gemini-2.0-flash-lite-001"

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun generateText(request: AiGenerateRequest): AiGenerateResponse {
        if (apiKey.isEmpty()) {
            return AiGenerateResponse(error = "Error: OPENROUTER_API_KEY is not set on the server.")
        }

        val systemPrompt = """
            You are a helpful AI assistant integrated into a note-taking app.
            The user will provide a prompt and the current content of their note.
            Your task is to generate text based on the prompt, which will be inserted into the note.
            Do not include any conversational filler, just return the generated text.
        """.trimIndent()

        val userMessage = buildString {
            appendLine("User prompt: ${request.prompt}")
            if (request.noteContent.isNotBlank()) {
                appendLine("Current note content:")
                appendLine(request.noteContent)
            }
        }

        val openRouterRequest = OpenRouterRequest(
            model = model,
            messages = listOf(
                OpenRouterMessage(role = "system", content = systemPrompt),
                OpenRouterMessage(role = "user", content = userMessage)
            )
        )

        return try {
            val response = client.post("https://openrouter.ai/api/v1/chat/completions") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer ${apiKey}")
                header("HTTP-Referer", "https://glyph.ru") // Required by OpenRouter
                header("X-Title", "Glyph Notes") // Required by OpenRouter
                setBody(openRouterRequest)
            }

            val responseBody = response.bodyAsText()
            println("OpenRouter Response: ${response.status} - $responseBody")

            if (response.status.isSuccess()) {
                val openRouterResponse = json.decodeFromString<OpenRouterResponse>(responseBody)
                val generatedText = openRouterResponse.choices?.firstOrNull()?.message?.content
                if (generatedText != null) {
                    AiGenerateResponse(generatedText = generatedText)
                } else {
                    AiGenerateResponse(error = openRouterResponse.error?.message ?: "Error: Empty response from AI")
                }
            } else {
                AiGenerateResponse(error = "Error: HTTP ${response.status.value} - $responseBody")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AiGenerateResponse(error = "Error: ${e.message}")
        }
    }
}
