package com.streamwarrior.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.streamwarrior.app.R
import com.streamwarrior.app.data.CoachesRepository
import com.streamwarrior.app.data.ForgeTool
import com.streamwarrior.app.ui.theme.CardBorder
import com.streamwarrior.app.ui.theme.CardSurface
import com.streamwarrior.app.ui.theme.CardSurfaceElevated
import com.streamwarrior.app.ui.theme.CrimsonAlert
import com.streamwarrior.app.ui.theme.GoldAccent
import com.streamwarrior.app.ui.theme.GoldInk
import com.streamwarrior.app.ui.theme.PinkAccent
import com.streamwarrior.app.ui.theme.TextMuted
import com.streamwarrior.app.ui.theme.TextPrimary
import com.streamwarrior.app.ui.theme.VioletPrimary
import java.util.UUID

@Composable
fun CoachesHubScreen(
    customTools: List<ForgeTool>,
    onOpenCoach: (String) -> Unit,
    onSaveCustomTool: (ForgeTool) -> Unit,
    onDeleteCustomTool: (String) -> Unit
) {
    var showForgeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.widthIn(max = 820.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "\uD83D\uDD25 Warrior Coaches & Forge",
                        style = MaterialTheme.typography.headlineMedium,
                        color = GoldAccent
                    )
                    Text(
                        text = "Built-in offline playbooks + optional AI upgrade & custom persona builder.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
                Button(
                    onClick = { showForgeDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = GoldInk),
                    modifier = Modifier.testTag("forge_create_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Forge", fontWeight = FontWeight.Bold)
                }
            }

            if (customTools.isNotEmpty()) {
                Text(
                    text = "YOUR FORGED TOOLS",
                    style = MaterialTheme.typography.labelLarge,
                    color = PinkAccent
                )
                customTools.forEach { tool ->
                    Surface(
                        onClick = { onOpenCoach("custom:${tool.id}") },
                        shape = RoundedCornerShape(16.dp),
                        color = CardSurfaceElevated,
                        border = BorderStroke(1.dp, PinkAccent.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_tool_${tool.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(tool.icon, fontSize = 24.sp)
                                Column {
                                    Text(tool.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                    if (tool.description.isNotBlank()) {
                                        Text(tool.description, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                                    }
                                }
                            }
                            IconButton(
                                onClick = { onDeleteCustomTool(tool.id) },
                                modifier = Modifier.testTag("delete_forge_${tool.id}")
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

            val grouped = remember { CoachesRepository.coaches.groupBy { it.category } }
            grouped.forEach { (category, list) ->
                Text(
                    text = category.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = GoldAccent,
                    modifier = Modifier.padding(top = 6.dp)
                )
                list.forEach { coach ->
                    Surface(
                        onClick = { onOpenCoach(coach.id) },
                        shape = RoundedCornerShape(16.dp),
                        color = CardSurface,
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("coach_card_${coach.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(text = coach.emoji, fontSize = 26.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = coach.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = coach.tagline,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showForgeDialog) {
        ForgeBuilderDialog(
            onSave = { tool ->
                onSaveCustomTool(tool)
                showForgeDialog = false
            },
            onDismiss = { showForgeDialog = false }
        )
    }
}

@Composable
private fun ForgeBuilderDialog(
    onSave: (ForgeTool) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("\uD83D\uDD25") }
    var desc by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }
    var temp by remember { mutableFloatStateOf(0.7f) }

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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("\uD83D\uDD25 Warrior Forge — New Tool", style = MaterialTheme.typography.titleLarge, color = GoldAccent)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tool Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("forge_name_input")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = icon,
                        onValueChange = { icon = it.take(4) },
                        label = { Text("Emoji") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(0.35f)
                            .testTag("forge_icon_input")
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Short Description") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(0.65f)
                            .testTag("forge_desc_input")
                    )
                }
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    label = { Text("System Persona / Rules") },
                    minLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("forge_prompt_input")
                )
                Text("Creativity: ${"%.1f".format(temp)}", style = MaterialTheme.typography.labelLarge, color = TextMuted)
                Slider(
                    value = temp,
                    onValueChange = { temp = it },
                    valueRange = 0.2f..1.0f
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("forge_cancel_button")
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (prompt.isNotBlank()) {
                                onSave(
                                    ForgeTool(
                                        id = UUID.randomUUID().toString(),
                                        name = name.trim().ifBlank { "Custom Warrior Tool" },
                                        icon = icon.trim().ifBlank { "\uD83D\uDD25" },
                                        description = desc.trim(),
                                        prompt = prompt.trim(),
                                        temperature = temp
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("forge_save_button")
                    ) {
                        Text("Save Tool")
                    }
                }
            }
        }
    }
}
