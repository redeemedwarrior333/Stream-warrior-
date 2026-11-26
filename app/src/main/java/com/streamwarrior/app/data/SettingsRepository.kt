package com.streamwarrior.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "stream_warrior_prefs")

class SettingsRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    private object Keys {
        val STREAMER_NAME = stringPreferencesKey("streamer_name")
        val AI_CONFIG = stringPreferencesKey("ai_config")
        val CUSTOM_SOUNDS = stringPreferencesKey("custom_sounds")
        val FORGE_TOOLS = stringPreferencesKey("forge_tools")
        val DEN_CONFIG = stringPreferencesKey("den_config")
        val WEIRD_LAW_SCORE = stringPreferencesKey("weird_law_score")
        val STREAM_CONNECTION = stringPreferencesKey("stream_connection")
    }

    val streamerNameFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.STREAMER_NAME] ?: "Stream Warrior"
    }

    val aiConfigFlow: Flow<AiConfig> = context.dataStore.data.map { prefs ->
        prefs[Keys.AI_CONFIG]?.let {
            runCatching { json.decodeFromString<AiConfig>(it) }.getOrNull()
        } ?: AiConfig()
    }

    val customSoundsFlow: Flow<List<CustomSound>> = context.dataStore.data.map { prefs ->
        prefs[Keys.CUSTOM_SOUNDS]?.let {
            runCatching { json.decodeFromString<List<CustomSound>>(it) }.getOrNull()
        } ?: emptyList()
    }

    val forgeToolsFlow: Flow<List<ForgeTool>> = context.dataStore.data.map { prefs ->
        prefs[Keys.FORGE_TOOLS]?.let {
            runCatching { json.decodeFromString<List<ForgeTool>>(it) }.getOrNull()
        } ?: emptyList()
    }

    val denConfigFlow: Flow<DenConfig> = context.dataStore.data.map { prefs ->
        prefs[Keys.DEN_CONFIG]?.let {
            runCatching { json.decodeFromString<DenConfig>(it) }.getOrNull()
        } ?: DenConfig()
    }

    val weirdLawScoreFlow: Flow<WeirdLawScore> = context.dataStore.data.map { prefs ->
        prefs[Keys.WEIRD_LAW_SCORE]?.let {
            runCatching { json.decodeFromString<WeirdLawScore>(it) }.getOrNull()
        } ?: WeirdLawScore()
    }

    val streamConnectionFlow: Flow<StreamConnectionConfig> = context.dataStore.data.map { prefs ->
        prefs[Keys.STREAM_CONNECTION]?.let {
            runCatching { json.decodeFromString<StreamConnectionConfig>(it) }.getOrNull()
        } ?: StreamConnectionConfig()
    }

    suspend fun saveStreamerName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.STREAMER_NAME] = name.trim().ifBlank { "Stream Warrior" }
        }
    }

    suspend fun saveAiConfig(config: AiConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AI_CONFIG] = json.encodeToString(config)
        }
    }

    suspend fun saveDenConfig(config: DenConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DEN_CONFIG] = json.encodeToString(config)
        }
    }

    suspend fun saveWeirdLawScore(score: WeirdLawScore) {
        context.dataStore.edit { prefs ->
            prefs[Keys.WEIRD_LAW_SCORE] = json.encodeToString(score)
        }
    }

    suspend fun saveStreamConnectionConfig(config: StreamConnectionConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.STREAM_CONNECTION] = json.encodeToString(config)
        }
    }

    suspend fun upsertForgeTool(tool: ForgeTool) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.FORGE_TOOLS]?.let {
                runCatching { json.decodeFromString<List<ForgeTool>>(it) }.getOrNull()
            } ?: emptyList()
            val updated = current.filterNot { it.id == tool.id || it.name.equals(tool.name, ignoreCase = true) } + tool
            prefs[Keys.FORGE_TOOLS] = json.encodeToString(updated)
        }
    }

    suspend fun deleteForgeTool(toolId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.FORGE_TOOLS]?.let {
                runCatching { json.decodeFromString<List<ForgeTool>>(it) }.getOrNull()
            } ?: emptyList()
            prefs[Keys.FORGE_TOOLS] = json.encodeToString(current.filterNot { it.id == toolId })
        }
    }

    suspend fun importCustomSound(
        sourceUri: android.net.Uri,
        displayName: String
    ): Result<CustomSound> = withContext(Dispatchers.IO) {
        try {
            val dir = File(context.filesDir, "custom_sounds").apply { mkdirs() }
            val id = UUID.randomUUID().toString()
            val dest = File(dir, "$id.audio")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            } ?: return@withContext Result.failure(IllegalStateException("Could not open audio file."))

            val cleanName = displayName
                .substringBeforeLast('.')
                .take(24)
                .ifBlank { "Custom Sound" }
            val newSound = CustomSound(id = id, name = cleanName, filePath = dest.absolutePath)
            context.dataStore.edit { prefs ->
                val current = prefs[Keys.CUSTOM_SOUNDS]?.let {
                    runCatching { json.decodeFromString<List<CustomSound>>(it) }.getOrNull()
                } ?: emptyList()
                val updated = (current + newSound).takeLast(12)
                prefs[Keys.CUSTOM_SOUNDS] = json.encodeToString(updated)
            }
            Result.success(newSound)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCustomSound(sound: CustomSound) = withContext(Dispatchers.IO) {
        runCatching { File(sound.filePath).delete() }
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.CUSTOM_SOUNDS]?.let {
                runCatching { json.decodeFromString<List<CustomSound>>(it) }.getOrNull()
            } ?: emptyList()
            prefs[Keys.CUSTOM_SOUNDS] = json.encodeToString(current.filterNot { it.id == sound.id })
        }
    }
}
