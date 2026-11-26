package com.streamwarrior.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class ChatTurn(
    val role: String,
    val content: String
)

object OpenAiClient {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun chatCompletion(
        config: AiConfig,
        systemPrompt: String,
        history: List<ChatTurn> = emptyList(),
        userMessage: String,
        temperature: Float = 0.75f,
        maxTokens: Int = 550
    ): Result<String> = withContext(Dispatchers.IO) {
        if (config.apiKey.isBlank()) {
            return@withContext Result.failure(IllegalStateException("No API key configured."))
        }
        val cleanBase = config.baseUrl.trim().trimEnd('/')
        val endpoint = "$cleanBase/chat/completions"
        var conn: HttpURLConnection? = null
        try {
            val url = URL(endpoint)
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 12000
                readTimeout = 20000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer ${config.apiKey.trim()}")
            }
            val msgArray = JsonArray(
                buildList {
                    add(buildJsonObject {
                        put("role", "system")
                        put("content", systemPrompt)
                    })
                    history.takeLast(10).forEach { turn ->
                        add(buildJsonObject {
                            put("role", turn.role)
                            put("content", turn.content)
                        })
                    }
                    add(buildJsonObject {
                        put("role", "user")
                        put("content", userMessage)
                    })
                }
            )
            val body = buildJsonObject {
                put("model", config.model.ifBlank { "mistral-small-latest" })
                put("temperature", temperature.toDouble())
                put("max_tokens", maxTokens)
                put("messages", msgArray)
            }.toString()

            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(body)
                writer.flush()
            }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val raw = stream?.let {
                BufferedReader(InputStreamReader(it, Charsets.UTF_8)).use { br -> br.readText() }
            }.orEmpty()

            if (code !in 200..299) {
                val errMsg = runCatching {
                    val obj = json.parseToJsonElement(raw).jsonObject
                    (obj["error"] as? JsonObject)?.get("message")?.jsonPrimitive?.contentOrNull
                        ?: obj["message"]?.jsonPrimitive?.contentOrNull
                }.getOrNull() ?: "HTTP $code"
                return@withContext Result.failure(IllegalStateException(errMsg))
            }

            val parsed = json.parseToJsonElement(raw).jsonObject
            val content = parsed["choices"]
                ?.jsonArray
                ?.firstOrNull()
                ?.jsonObject
                ?.get("message")
                ?.jsonObject
                ?.get("content")
                ?.let { el -> (el as? JsonPrimitive)?.contentOrNull }
                ?.trim()

            if (content.isNullOrBlank()) {
                Result.failure(IllegalStateException("Empty response from AI provider."))
            } else {
                Result.success(content)
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            conn?.disconnect()
        }
    }

    suspend fun testConnection(config: AiConfig): Result<String> {
        return chatCompletion(
            config = config,
            systemPrompt = "You are a concise connection check.",
            userMessage = "Reply with only the two words: Stream Ready",
            temperature = 0.2f,
            maxTokens = 20
        )
    }
}
