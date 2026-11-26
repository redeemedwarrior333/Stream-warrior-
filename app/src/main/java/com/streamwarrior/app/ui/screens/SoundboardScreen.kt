package com.streamwarrior.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamwarrior.app.R
import com.streamwarrior.app.audio.SoundSynth
import com.streamwarrior.app.data.CustomSound
import com.streamwarrior.app.ui.theme.CardBorder
import com.streamwarrior.app.ui.theme.CardSurface
import com.streamwarrior.app.ui.theme.CardSurfaceElevated
import com.streamwarrior.app.ui.theme.CrimsonAlert
import com.streamwarrior.app.ui.theme.GoldAccent
import com.streamwarrior.app.ui.theme.GoldInk
import com.streamwarrior.app.ui.theme.TextMuted
import com.streamwarrior.app.ui.theme.TextPrimary
import com.streamwarrior.app.ui.theme.VioletPrimary

@Composable
fun SoundboardScreen(
    customSounds: List<CustomSound>,
    isWideLayout: Boolean,
    onOpenSettingsToAddSound: () -> Unit,
    onDeleteCustomSound: (CustomSound) -> Unit
) {
    var lastPlayed by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (isWideLayout) 4 else 2),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 800.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "\uD83D\uDD0A Stream Soundboard",
                            style = MaterialTheme.typography.headlineMedium,
                            color = GoldAccent
                        )
                        Text(
                            text = lastPlayed?.let { "Last hit: $it" } ?: "Tap any pad for instant SFX",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                    Button(
                        onClick = {
                            SoundSynth.stopCurrent()
                            lastPlayed = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert),
                        modifier = Modifier.testTag("soundboard_stop_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = stringResource(R.string.cd_stop_sound),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.size(4.dp))
                        Text("Stop")
                    }
                }
            }

            items(SoundSynth.builtInSounds, key = { it.id }) { sfx ->
                val active = lastPlayed == sfx.label
                Surface(
                    onClick = {
                        lastPlayed = sfx.label
                        SoundSynth.play(sfx.id)
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = if (active) VioletPrimary else CardSurface,
                    border = BorderStroke(1.2.dp, if (active) GoldAccent else CardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(86.dp)
                        .testTag("sfx_pad_${sfx.id}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = sfx.emoji, fontSize = 26.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = sfx.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "CUSTOM CLIPS (${customSounds.size}/12)",
                        style = MaterialTheme.typography.labelLarge,
                        color = GoldAccent
                    )
                    OutlinedButton(
                        onClick = onOpenSettingsToAddSound,
                        modifier = Modifier.testTag("soundboard_import_button")
                    ) {
                        Icon(Icons.Default.AudioFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Add Clip")
                    }
                }
            }

            if (customSounds.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = CardSurfaceElevated,
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Text(
                            text = "No custom audio clips added yet. Tap \"Add Clip\" to import MP3/WAV/OGG files from your device.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            } else {
                items(customSounds, key = { it.id }) { sound ->
                    Surface(
                        onClick = {
                            lastPlayed = sound.name
                            SoundSynth.playCustomFile(sound.filePath)
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = CardSurfaceElevated,
                        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_sfx_${sound.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "\uD83C\uDFB5 ${sound.name}",
                                style = MaterialTheme.typography.labelLarge,
                                color = GoldAccent,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onDeleteCustomSound(sound) },
                                modifier = Modifier.testTag("soundboard_delete_${sound.id}")
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

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
