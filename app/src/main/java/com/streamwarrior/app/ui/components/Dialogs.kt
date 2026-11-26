package com.streamwarrior.app.ui.components

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.streamwarrior.app.R
import com.streamwarrior.app.data.AI_PROVIDER_PRESETS
import com.streamwarrior.app.data.AiConfig
import com.streamwarrior.app.data.CustomSound
import com.streamwarrior.app.ui.theme.CardBorder
import com.streamwarrior.app.ui.theme.CardSurface
import com.streamwarrior.app.ui.theme.CardSurfaceElevated
import com.streamwarrior.app.ui.theme.CrimsonAlert
import com.streamwarrior.app.ui.theme.EmeraldGood
import com.streamwarrior.app.ui.theme.GoldAccent
import com.streamwarrior.app.ui.theme.GoldInk
import com.streamwarrior.app.ui.theme.TextMuted
import com.streamwarrior.app.ui.theme.TextPrimary
import com.streamwarrior.app.ui.theme.TextSecondary
import com.streamwarrior.app.ui.theme.VioletPrimary

@Composable
fun StreamHelpDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = CardSurface,
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "\uD83D\uDCFA ${stringResource(R.string.help_title)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = GoldAccent
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("help_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.cd_close_dialog),
                            tint = TextMuted
                        )
                    }
                }

                HelpSection(
                    title = "1. Clean Screen Mode (Hide Bar)",
                    body = "Tap \"Hide\" in the bottom navigation bar to hide the top and bottom bars while filming or screen-mirroring on TikTok Live, BIGO, Twitch, or Kick. Tap \"Show Controls\" at the top anytime to bring them back."
                )
                HelpSection(
                    title = "2. Two-Device Camera Setup",
                    body = "Streaming on a camera-only live app? Keep your camera device facing you and run Stream Warrior on a second phone or tablet beside you for instant Tarot pulls, Soundboard hits, Trivia, and Wheel spins."
                )
                HelpSection(
                    title = "3. Live Voice AI Co-Host + Built-In Tools",
                    body = "Tap Voice AI to speak hands-free with Gemini AI on stream and hear replies read aloud automatically. All game decks, Tarot, Soundboard, Prophet, and Coaches also work built-in, with an optional BYO provider key in Settings."
                )
                HelpSection(
                    title = "4. Custom Soundboard Clips",
                    body = "Import up to 12 MP3, WAV, or OGG sound clips privately from your device using Android's system file picker — no broad storage permissions needed."
                )

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = GoldInk),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("help_done_button")
                ) {
                    Text("Got it", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun HelpSection(title: String, body: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = CardSurfaceElevated,
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text(text = body, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
    }
}

@Composable
fun StreamSettingsDialog(
    streamerName: String,
    aiConfig: AiConfig,
    customSounds: List<CustomSound>,
    aiTestStatus: String?,
    soundUploadStatus: String?,
    onSaveStreamerName: (String) -> Unit,
    onSaveAiConfig: (AiConfig) -> Unit,
    onTestAiConnection: (AiConfig) -> Unit,
    onImportSound: (Uri, String) -> Unit,
    onDeleteCustomSound: (CustomSound) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var nameInput by remember(streamerName) { mutableStateOf(streamerName) }
    var keyInput by remember(aiConfig.apiKey) { mutableStateOf(aiConfig.apiKey) }
    var urlInput by remember(aiConfig.baseUrl) { mutableStateOf(aiConfig.baseUrl) }
    var modelInput by remember(aiConfig.model) { mutableStateOf(aiConfig.model) }

    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val fileName = resolveDisplayName(context, uri)
            onImportSound(uri, fileName)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = CardSurface,
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "\u2699\uFE0F ${stringResource(R.string.settings_title)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = GoldAccent
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("settings_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.cd_close_dialog),
                            tint = TextMuted
                        )
                    }
                }

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Streamer / Host Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_streamer_name_input")
                )

                HorizontalDivider(color = CardBorder)

                Text(
                    text = "Optional AI Upgrade (BYO Key)",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = "App works offline without any key. Pick a provider preset below and paste your own API key if you want endless AI prompts.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AI_PROVIDER_PRESETS.forEach { preset ->
                        val selected = urlInput.trim().equals(preset.baseUrl, ignoreCase = true)
                        FilterChip(
                            selected = selected,
                            onClick = {
                                urlInput = preset.baseUrl
                                modelInput = preset.defaultModel
                            },
                            label = { Text(preset.label) },
                            modifier = Modifier.testTag("preset_${preset.id}")
                        )
                    }
                }

                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    label = { Text("API Key (stored locally on device)") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_api_key_input")
                )

                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("OpenAI-Compatible Base URL") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_base_url_input")
                )

                OutlinedTextField(
                    value = modelInput,
                    onValueChange = { modelInput = it },
                    label = { Text("Model ID") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_model_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onTestAiConnection(
                                AiConfig(keyInput.trim(), urlInput.trim(), modelInput.trim())
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("settings_test_ai_button")
                    ) {
                        Text("Test AI")
                    }
                    Button(
                        onClick = {
                            onSaveStreamerName(nameInput)
                            onSaveAiConfig(
                                AiConfig(keyInput.trim(), urlInput.trim(), modelInput.trim())
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("settings_save_button")
                    ) {
                        Text("Save")
                    }
                }

                if (!aiTestStatus.isNullOrBlank()) {
                    Text(
                        text = aiTestStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (aiTestStatus.startsWith("Connected")) EmeraldGood else CrimsonAlert
                    )
                }

                HorizontalDivider(color = CardBorder)

                Text(
                    text = "Custom Soundboard Clips (${customSounds.size}/12)",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                OutlinedButton(
                    onClick = { audioPicker.launch(arrayOf("audio/*")) },
                    enabled = customSounds.size < 12,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_import_sound_button")
                ) {
                    Icon(Icons.Default.AudioFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Import Audio Clip")
                }
                if (!soundUploadStatus.isNullOrBlank()) {
                    Text(
                        text = soundUploadStatus,
                        style = MaterialTheme.typography.labelLarge,
                        color = EmeraldGood
                    )
                }
                customSounds.forEach { sound ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "\uD83C\uDFB5 ${sound.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        IconButton(
                            onClick = { onDeleteCustomSound(sound) },
                            modifier = Modifier.testTag("delete_sound_${sound.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.cd_delete_item),
                                tint = CrimsonAlert
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun resolveDisplayName(context: Context, uri: Uri): String {
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (idx >= 0 && cursor.moveToFirst()) {
            return cursor.getString(idx) ?: "Custom Sound"
        }
    }
    return uri.lastPathSegment ?: "Custom Sound"
}
