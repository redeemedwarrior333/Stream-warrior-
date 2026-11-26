package com.streamwarrior.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamwarrior.app.R
import com.streamwarrior.app.data.DenAlertEvent
import com.streamwarrior.app.data.DenConfig
import com.streamwarrior.app.data.TipLink
import com.streamwarrior.app.ui.theme.BgDeep
import com.streamwarrior.app.ui.theme.CardBorder
import com.streamwarrior.app.ui.theme.CardSurface
import com.streamwarrior.app.ui.theme.CardSurfaceElevated
import com.streamwarrior.app.ui.theme.CrimsonAlert
import com.streamwarrior.app.ui.theme.CyanAccent
import com.streamwarrior.app.ui.theme.EmeraldGood
import com.streamwarrior.app.ui.theme.GoldAccent
import com.streamwarrior.app.ui.theme.GoldInk
import com.streamwarrior.app.ui.theme.TextMuted
import com.streamwarrior.app.ui.theme.TextPrimary
import com.streamwarrior.app.ui.theme.TextSecondary
import com.streamwarrior.app.ui.theme.VioletPrimary
import java.util.UUID

private enum class DenTab(val label: String) {
    STAGE("\uD83D\uDD34 Live Stage"),
    CONFIG("\uD83C\uDF9B\uFE0F Ticker & Brand"),
    TIPS("\uD83D\uDCB0 Tip Links")
}

@Composable
fun DenToolkitScreen(
    denConfig: DenConfig,
    activeAlert: DenAlertEvent?,
    onSaveDenConfig: (DenConfig) -> Unit,
    onFireAlert: (kind: String, viewerName: String, message: String, amount: String) -> Unit,
    onDismissAlert: () -> Unit,
    onOpenStreamConnect: () -> Unit = {}
) {
    val context = LocalContext.current
    var tab by remember { mutableStateOf(DenTab.STAGE) }

    // Alert inputs
    var viewerName by remember { mutableStateOf("NightOwl_77") }
    var alertMessage by remember { mutableStateOf("Keep going, warrior!") }
    var tipAmount by remember { mutableStateOf("$5") }

    // Brand/ticker inputs
    var brandName by remember(denConfig.streamerName) { mutableStateOf(denConfig.streamerName) }
    var brandTagline by remember(denConfig.tagline) { mutableStateOf(denConfig.tagline) }
    var tickerBlock by remember(denConfig.tickerMessages) {
        mutableStateOf(denConfig.tickerMessages.joinToString("\n"))
    }
    var savedBanner by remember { mutableStateOf(false) }

    // Tip link inputs
    var newTipLabel by remember { mutableStateOf("") }
    var newTipUrl by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.widthIn(max = 760.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "\uD83D\uDD34 Warrior's Den Studio",
                    style = MaterialTheme.typography.headlineMedium,
                    color = GoldAccent
                )
                OutlinedButton(
                    onClick = onOpenStreamConnect,
                    modifier = Modifier.testTag("den_open_connect_button")
                ) {
                    Text("\uD83D\uDCE1 OBS / Live Link")
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DenTab.entries.forEach { t ->
                    FilterChip(
                        selected = tab == t,
                        onClick = { tab = t },
                        label = { Text(t.label) },
                        modifier = Modifier.testTag("den_tab_${t.name.lowercase()}")
                    )
                }
            }

            when (tab) {
                DenTab.STAGE -> {
                    // Live stream canvas preview (Corner Badge + Alert Stage + Scrolling Ticker)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = BgDeep,
                        border = BorderStroke(1.5.dp, CardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Corner Live Badge
                            Column(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(14.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = CrimsonAlert
                                ) {
                                    Text(
                                        text = "● LIVE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = denConfig.streamerName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GoldAccent
                                )
                                if (denConfig.tagline.isNotBlank()) {
                                    Text(
                                        text = denConfig.tagline,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            // Center Alert Pop-up
                            androidx.compose.animation.AnimatedVisibility(
                                visible = activeAlert != null,
                                enter = fadeIn() + slideInVertically { -it / 2 },
                                exit = fadeOut() + slideOutVertically { -it / 2 },
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(horizontal = 16.dp)
                            ) {
                                activeAlert?.let { ev ->
                                    val accent = when (ev.kind) {
                                        "follow" -> EmeraldGood
                                        "sub" -> CyanAccent
                                        "raid" -> CrimsonAlert
                                        else -> GoldAccent
                                    }
                                    val icon = when (ev.kind) {
                                        "follow" -> "\uD83D\uDD14"
                                        "sub" -> "\u2B50"
                                        "raid" -> "\uD83D\uDE80"
                                        else -> "\uD83D\uDCB0"
                                    }
                                    val line = when (ev.kind) {
                                        "follow" -> "${ev.viewerName} just followed!"
                                        "sub" -> "${ev.viewerName} subscribed!"
                                        "tip" -> "${ev.viewerName} tipped ${ev.amount}"
                                        else -> "${ev.viewerName} raided with their crew!"
                                    }
                                    Surface(
                                        onClick = onDismissAlert,
                                        shape = RoundedCornerShape(16.dp),
                                        color = CardSurface,
                                        border = BorderStroke(2.dp, accent),
                                        modifier = Modifier.fillMaxWidth(0.9f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(text = icon, fontSize = 28.sp)
                                            Text(
                                                text = line,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = accent,
                                                textAlign = TextAlign.Center
                                            )
                                            if (ev.message.isNotBlank()) {
                                                Text(
                                                    text = ev.message,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = TextSecondary,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Bottom Scrolling Ticker
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(CardSurface.copy(alpha = 0.94f))
                                    .padding(vertical = 8.dp, horizontal = 12.dp)
                            ) {
                                val joined = denConfig.tickerMessages.joinToString("   ◆   ")
                                Text(
                                    text = joined,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = GoldAccent,
                                    maxLines = 1,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .basicMarquee()
                                )
                            }
                        }
                    }

                    // Fire Alerts Deck
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = CardSurface,
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Fire Stream Alerts (Audio + Visual)", style = MaterialTheme.typography.titleMedium, color = GoldAccent)
                            OutlinedTextField(
                                value = viewerName,
                                onValueChange = { viewerName = it },
                                label = { Text("Viewer Name") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("den_viewer_name_input")
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = alertMessage,
                                    onValueChange = { alertMessage = it },
                                    label = { Text("Message") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("den_alert_msg_input")
                                )
                                OutlinedTextField(
                                    value = tipAmount,
                                    onValueChange = { tipAmount = it },
                                    label = { Text("Tip Amt") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .widthIn(max = 110.dp)
                                        .testTag("den_tip_amt_input")
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    "follow" to "\uD83D\uDD14 Follow",
                                    "sub" to "\u2B50 Sub",
                                    "tip" to "\uD83D\uDCB0 Tip",
                                    "raid" to "\uD83D\uDE80 Raid"
                                ).forEach { (kind, label) ->
                                    Button(
                                        onClick = { onFireAlert(kind, viewerName, alertMessage, tipAmount) },
                                        colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("den_fire_$kind")
                                    ) {
                                        Text(label, maxLines = 1, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                DenTab.CONFIG -> {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = CardSurface,
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = brandName,
                                onValueChange = { brandName = it },
                                label = { Text("Overlay Streamer Name") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("den_brand_name_input")
                            )
                            OutlinedTextField(
                                value = brandTagline,
                                onValueChange = { brandTagline = it },
                                label = { Text("Brand Tagline") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("den_brand_tagline_input")
                            )
                            OutlinedTextField(
                                value = tickerBlock,
                                onValueChange = { tickerBlock = it },
                                label = { Text("Ticker Messages (one per line)") },
                                minLines = 4,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("den_ticker_lines_input")
                            )
                            Button(
                                onClick = {
                                    val msgs = tickerBlock.lines().map { it.trim() }.filter { it.isNotBlank() }
                                    onSaveDenConfig(
                                        denConfig.copy(
                                            streamerName = brandName.trim().ifBlank { "Redeemed Warrior" },
                                            tagline = brandTagline.trim(),
                                            tickerMessages = msgs.ifEmpty { DenConfig().tickerMessages }
                                        )
                                    )
                                    savedBanner = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = GoldInk),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("den_save_config_button")
                            ) {
                                Text("Save Overlay & Ticker", fontWeight = FontWeight.Bold)
                            }
                            if (savedBanner) {
                                Text(
                                    text = "✓ Overlay & Ticker updated on Live Stage!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = EmeraldGood
                                )
                            }
                        }
                    }
                }

                DenTab.TIPS -> {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = CardSurface,
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Your Payment / Support Links", style = MaterialTheme.typography.titleMedium, color = GoldAccent)
                            OutlinedTextField(
                                value = newTipLabel,
                                onValueChange = { newTipLabel = it },
                                label = { Text("Platform Label (e.g. Ko-fi, PayPal, CashApp)") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("den_tip_label_input")
                            )
                            OutlinedTextField(
                                value = newTipUrl,
                                onValueChange = { newTipUrl = it },
                                label = { Text("HTTPS Link (https://...)") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("den_tip_url_input")
                            )
                            Button(
                                onClick = {
                                    val label = newTipLabel.trim()
                                    val url = newTipUrl.trim()
                                    if (label.isNotBlank() && url.startsWith("https://")) {
                                        val updated = denConfig.tipLinks + TipLink(UUID.randomUUID().toString(), label, url)
                                        onSaveDenConfig(denConfig.copy(tipLinks = updated))
                                        newTipLabel = ""
                                        newTipUrl = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("den_add_tip_button")
                            ) {
                                Text("+ Add Tip Link")
                            }

                            denConfig.tipLinks.forEach { link ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = CardSurfaceElevated,
                                    border = BorderStroke(1.dp, CardBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(link.label, style = MaterialTheme.typography.titleMedium, color = GoldAccent)
                                            Text(link.url, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        }
                                        IconButton(
                                            onClick = {
                                                runCatching {
                                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link.url)))
                                                }
                                            },
                                            modifier = Modifier.testTag("den_open_tip_${link.id}")
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Open link", tint = CyanAccent)
                                        }
                                        IconButton(
                                            onClick = {
                                                onSaveDenConfig(
                                                    denConfig.copy(tipLinks = denConfig.tipLinks.filterNot { it.id == link.id })
                                                )
                                            },
                                            modifier = Modifier.testTag("den_delete_tip_${link.id}")
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.cd_delete_item), tint = CrimsonAlert)
                                        }
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CardSurfaceElevated,
                                border = BorderStroke(1.dp, CrimsonAlert)
                            ) {
                                Text(
                                    text = "\uD83D\uDEA8 Tip Safety: Never share passwords, never send money back to \"receive\" a tip, and never pay a fee to claim support.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
