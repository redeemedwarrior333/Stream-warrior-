package com.streamwarrior.app.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.streamwarrior.app.data.ChatTurn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

private const val TAG = "FirebaseAiService"
private const val DEFAULT_MODEL = "gemini-3.8-flash"
private const val AI_TIMEOUT_MS = 60_000L

object FirebaseAiService {

    private val generalModel by lazy {
        Firebase.ai.generativeModel(
            modelName = DEFAULT_MODEL,
            generationConfig = generationConfig {
                temperature = 0.8f
                maxOutputTokens = 600
            }
        )
    }

    fun streamConversation(
        systemPrompt: String,
        history: List<ChatTurn>,
        userMessage: String
    ): Flow<String> = flow {
        val model = Firebase.ai.generativeModel(
            modelName = DEFAULT_MODEL,
            generationConfig = generationConfig {
                temperature = 0.85f
                maxOutputTokens = 360
            },
            systemInstruction = content { text(systemPrompt) }
        )
        val mappedHistory = history.takeLast(12).map { turn ->
            val role = if (turn.role == "user") "user" else "model"
            content(role = role) { text(turn.content) }
        }
        val chat = model.startChat(history = mappedHistory)
        val accumulated = StringBuilder()

        withTimeout(AI_TIMEOUT_MS) {
            chat.sendMessageStream(userMessage).collect { chunk ->
                val piece = chunk.text.orEmpty()
                if (piece.isNotEmpty()) {
                    accumulated.append(piece)
                    emit(accumulated.toString())
                }
            }
        }
    }

    suspend fun generateReply(
        systemPrompt: String,
        history: List<ChatTurn> = emptyList(),
        userMessage: String
    ): Result<String> {
        return try {
            val mappedHistory = history.takeLast(10).map { turn ->
                val role = if (turn.role == "user") "user" else "model"
                content(role = role) { text(turn.content) }
            }
            val modelWithSystem = if (systemPrompt.isNotBlank()) {
                Firebase.ai.generativeModel(
                    modelName = DEFAULT_MODEL,
                    generationConfig = generationConfig {
                        temperature = 0.8f
                        maxOutputTokens = 550
                    },
                    systemInstruction = content { text(systemPrompt) }
                )
            } else {
                generalModel
            }
            val response = withTimeout(AI_TIMEOUT_MS) {
                if (mappedHistory.isEmpty()) {
                    modelWithSystem.generateContent(userMessage)
                } else {
                    modelWithSystem.startChat(history = mappedHistory).sendMessage(userMessage)
                }
            }
            val text = response.text?.trim()
            if (text.isNullOrBlank()) {
                Result.failure(IllegalStateException("Gemini returned an empty response."))
            } else {
                Result.success(text)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini generation failed", e)
            Result.failure(e)
        }
    }
}
