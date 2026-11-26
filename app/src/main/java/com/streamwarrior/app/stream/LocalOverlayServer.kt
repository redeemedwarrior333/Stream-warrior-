package com.streamwarrior.app.stream

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException

private const val TAG = "LocalOverlayServer"

class LocalOverlayServer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var serverSocket: ServerSocket? = null
    private var acceptJob: Job? = null

    @Volatile
    var boundPort: Int = 8765
        private set

    val isRunning: Boolean
        get() = serverSocket != null && !(serverSocket?.isClosed ?: true)

    fun start(preferredPort: Int = 8765): Result<Int> {
        if (isRunning) return Result.success(boundPort)
        var lastError: Exception? = null
        for (port in preferredPort..(preferredPort + 10)) {
            try {
                val socket = ServerSocket(port, 16, InetAddress.getByName("0.0.0.0"))
                serverSocket = socket
                boundPort = port
                acceptJob = scope.launch {
                    while (isActive && !socket.isClosed) {
                        try {
                            val client = socket.accept()
                            launch { handleClient(client) }
                        } catch (_: SocketException) {
                            break
                        } catch (e: Exception) {
                            Log.w(TAG, "Accept error", e)
                        }
                    }
                }
                return Result.success(port)
            } catch (e: Exception) {
                lastError = e
            }
        }
        return Result.failure(lastError ?: IllegalStateException("Could not bind overlay port"))
    }

    fun stop() {
        acceptJob?.cancel()
        acceptJob = null
        try {
            serverSocket?.close()
        } catch (_: Exception) {
        }
        serverSocket = null
    }

    private fun handleClient(socket: Socket) {
        socket.use { s ->
            s.soTimeout = 4000
            val reader = BufferedReader(InputStreamReader(s.getInputStream(), Charsets.UTF_8))
            val requestLine = reader.readLine().orEmpty()
            val rawPath = requestLine.split(" ").getOrNull(1)?.substringBefore('?') ?: "/"

            val (status, contentType, body) = when (rawPath) {
                "/api/state" -> Triple("200 OK", "application/json; charset=utf-8", LiveOverlayBus.snapshot.value.toJsonString())
                "/captions" -> Triple("200 OK", "text/html; charset=utf-8", buildOverlayHtml(captionsOnly = true))
                "/", "/overlay" -> Triple("200 OK", "text/html; charset=utf-8", buildOverlayHtml(captionsOnly = false))
                "/favicon.ico" -> Triple("204 No Content", "text/plain", "")
                else -> Triple("404 Not Found", "text/plain; charset=utf-8", "Not found")
            }

            writeHttpResponse(s.getOutputStream(), status, contentType, body)
        }
    }

    private fun writeHttpResponse(
        out: OutputStream,
        status: String,
        contentType: String,
        body: String
    ) {
        val bodyBytes = body.toByteArray(Charsets.UTF_8)
        val headers = buildString {
            append("HTTP/1.1 $status\r\n")
            append("Content-Type: $contentType\r\n")
            append("Content-Length: ${bodyBytes.size}\r\n")
            append("Cache-Control: no-store, no-cache\r\n")
            append("Access-Control-Allow-Origin: *\r\n")
            append("Connection: close\r\n\r\n")
        }.toByteArray(Charsets.UTF_8)
        out.write(headers)
        if (bodyBytes.isNotEmpty()) {
            out.write(bodyBytes)
        }
        out.flush()
    }

    companion object {
        fun resolveLocalIpAddress(): String {
            return try {
                val interfaces = NetworkInterface.getNetworkInterfaces()?.toList().orEmpty()
                val candidates = interfaces
                    .filter { it.isUp && !it.isLoopback && !it.isVirtual }
                    .flatMap { iface ->
                        iface.inetAddresses.toList()
                            .filterIsInstance<Inet4Address>()
                            .filter { !it.isLoopbackAddress && !it.isLinkLocalAddress }
                            .map { iface.name to it.hostAddress.orEmpty() }
                    }
                val wifiMatch = candidates.firstOrNull { (name, _) ->
                    name.startsWith("wlan", ignoreCase = true) || name.startsWith("en", ignoreCase = true)
                }
                (wifiMatch ?: candidates.firstOrNull())?.second?.ifBlank { "127.0.0.1" } ?: "127.0.0.1"
            } catch (_: Exception) {
                "127.0.0.1"
            }
        }
    }
}

private fun buildOverlayHtml(captionsOnly: Boolean): String = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Stream Warrior Live Overlay</title>
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; }
  html, body {
    width: 100%; height: 100%; overflow: hidden;
    background: transparent; color: #F5F2FF;
    font-family: system-ui, -apple-system, "Segoe UI", Roboto, sans-serif;
  }
  .stage { position: relative; width: 100%; height: 100%; padding: 24px; }
  .badge {
    position: absolute; top: 24px; left: 24px;
    background: rgba(14, 11, 29, 0.86); border: 1.5px solid rgba(240, 200, 120, 0.65);
    border-radius: 14px; padding: 10px 16px; backdrop-filter: blur(8px);
    display: flex; align-items: center; gap: 10px;
  }
  .live-pill {
    background: #FF465A; color: #fff; font-weight: 800; font-size: 12px;
    padding: 3px 8px; border-radius: 6px; letter-spacing: 1px;
  }
  .streamer { font-weight: 800; font-size: 18px; color: #F0C878; }
  .tagline { font-size: 12px; color: #C9C2E8; }
  .alert-wrap {
    position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);
    min-width: 320px; max-width: 560px; text-align: center;
    background: rgba(20, 16, 46, 0.94); border: 2.5px solid #F0C878;
    border-radius: 20px; padding: 22px 28px;
    box-shadow: 0 18px 48px rgba(0,0,0,0.55);
    transition: opacity 0.25s ease;
  }
  .alert-icon { font-size: 40px; margin-bottom: 6px; }
  .alert-title { font-size: 24px; font-weight: 800; color: #F0C878; }
  .alert-msg { font-size: 16px; color: #EAE4FF; margin-top: 6px; }
  .captions {
    position: absolute; left: 50%; transform: translateX(-50%);
    bottom: ${if (captionsOnly) "28px" else "68px"};
    width: min(860px, calc(100% - 48px));
    background: rgba(14, 11, 29, 0.88); border: 1.5px solid rgba(124, 58, 237, 0.75);
    border-radius: 16px; padding: 14px 20px;
    box-shadow: 0 10px 30px rgba(0,0,0,0.45);
  }
  .cap-user { font-size: 13px; font-weight: 700; color: #F0C878; margin-bottom: 4px; }
  .cap-role { font-size: 11px; font-weight: 800; letter-spacing: 1.4px; color: #22D3EE; text-transform: uppercase; }
  .cap-ai { font-size: 22px; font-weight: 700; line-height: 1.35; color: #FFFFFF; margin-top: 2px; }
  .ticker {
    position: absolute; bottom: 0; left: 0; right: 0;
    background: rgba(20, 16, 46, 0.92); border-top: 1.5px solid rgba(240, 200, 120, 0.45);
    padding: 10px 24px; font-size: 15px; font-weight: 700; color: #F0C878;
    white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
  }
  .hidden { display: none !important; }
</style>
</head>
<body>
<div class="stage">
  <div id="badge" class="badge ${if (captionsOnly) "hidden" else ""}">
    <span class="live-pill">LIVE</span>
    <div>
      <div id="streamer" class="streamer"></div>
      <div id="tagline" class="tagline"></div>
    </div>
  </div>
  <div id="alert" class="alert-wrap hidden">
    <div id="alertIcon" class="alert-icon"></div>
    <div id="alertTitle" class="alert-title"></div>
    <div id="alertMsg" class="alert-msg"></div>
  </div>
  <div id="captions" class="captions hidden">
    <div id="capUser" class="cap-user hidden"></div>
    <div id="capRole" class="cap-role"></div>
    <div id="capAi" class="cap-ai"></div>
  </div>
  <div id="ticker" class="ticker ${if (captionsOnly) "hidden" else ""}"></div>
</div>
<script>
const CAPTIONS_ONLY = ${if (captionsOnly) "true" else "false"};
const ICONS = { follow: "\uD83D\uDD14", sub: "\u2B50", tip: "\uD83D\uDCB0", raid: "\uD83D\uDE80" };
async function refresh() {
  try {
    const r = await fetch("/api/state", { cache: "no-store" });
    if (!r.ok) return;
    const s = await r.json();
    if (!CAPTIONS_ONLY) {
      document.getElementById("badge").classList.toggle("hidden", !s.showCornerBadge);
      document.getElementById("streamer").textContent = s.streamerName || "";
      document.getElementById("tagline").textContent = s.tagline || "";
      const alertEl = document.getElementById("alert");
      if (s.showAlerts && s.activeAlert) {
        const a = s.activeAlert;
        document.getElementById("alertIcon").textContent = ICONS[a.kind] || "\uD83D\uDD25";
        const label = a.kind === "follow" ? a.viewerName + " just followed!"
          : a.kind === "sub" ? a.viewerName + " subscribed!"
          : a.kind === "tip" ? a.viewerName + " tipped " + (a.amount || "")
          : a.viewerName + " raided!";
        document.getElementById("alertTitle").textContent = label;
        document.getElementById("alertMsg").textContent = a.message || "";
        alertEl.classList.remove("hidden");
      } else {
        alertEl.classList.add("hidden");
      }
      const tickerEl = document.getElementById("ticker");
      tickerEl.classList.toggle("hidden", !s.showTicker);
      tickerEl.textContent = (s.tickerMessages || []).join("   \u25C6   ");
    }
    const capEl = document.getElementById("captions");
    const hasCap = s.showVoiceCaptions && (s.voiceAiCaption || s.voiceUserCaption);
    capEl.classList.toggle("hidden", !hasCap);
    if (hasCap) {
      const cu = document.getElementById("capUser");
      cu.classList.toggle("hidden", !s.voiceUserCaption);
      cu.textContent = s.voiceUserCaption ? ("YOU: " + s.voiceUserCaption) : "";
      document.getElementById("capRole").textContent = (s.voicePersona || "AI CO-HOST") + " \u2022 " + (s.voiceStatus || "LIVE");
      document.getElementById("capAi").textContent = s.voiceAiCaption || "";
    }
  } catch (e) {}
}
refresh();
setInterval(refresh, 600);
</script>
</body>
</html>
""".trimIndent()
