package com.streamwarrior.app

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

/**
 * Setup screen: grant overlay permission, start/stop the floating mod menu.
 * The menu then floats over any streaming app (MeetMe, TikTok, BIGO...).
 */
class MainActivity : AppCompatActivity() {

    private lateinit var status: TextView
    private lateinit var startBtn: Button
    private lateinit var stopBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pad = (16 * resources.displayMetrics.density).toInt()
        val root = ScrollView(this).apply { setBackgroundColor(Color.parseColor("#0B0B14")) }
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad * 2, pad, pad * 2)
        }
        root.addView(col)
        setContentView(root)

        fun text(s: String, size: Float, bold: Boolean = false, color: String = "#F0F0FF"): TextView =
            TextView(this).apply {
                text = s; textSize = size; setTextColor(Color.parseColor(color))
                if (bold) setTypeface(null, Typeface.BOLD)
                setPadding(0, pad / 2, 0, pad / 2)
            }

        fun button(label: String, onClick: () -> Unit): Button =
            Button(this).apply {
                text = label; isAllCaps = false
                setTextColor(Color.WHITE)
                background = GradientDrawable().apply {
                    colors = intArrayOf(Color.parseColor("#7C3AED"), Color.parseColor("#EC4899"))
                    orientation = GradientDrawable.Orientation.TL_BR
                    cornerRadius = (14 * resources.displayMetrics.density)
                }
                setPadding(0, pad / 2, 0, pad / 2)
                setOnClickListener { onClick() }
            }

        col.addView(text("Stream Warrior", 26f, true))
        col.addView(text("Your streamer mod menu. The floating menu runs ON TOP of MeetMe, TikTok, BIGO or any app — soundboard and game cards while your camera stays on you.", 14f, color = "#8A8AB0"))

        status = text("", 14f, true, "#10B981")
        col.addView(status)

        startBtn = button("Start floating mod menu") { startOverlay() }
        col.addView(startBtn)
        stopBtn = button("Stop", {}).apply {
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#1F1F36")); cornerRadius = (14 * resources.displayMetrics.density)
                setStroke(2, Color.parseColor("#EF4444"))
            }
            setOnClickListener {
                stopService(Intent(this@MainActivity, OverlayService::class.java)); refresh()
            }
        }
        col.addView(stopBtn)

        col.addView(text("First time setup", 16f, true))
        col.addView(text("1. Tap below and allow \"Display over other apps\" for Stream Warrior.", 14f, color = "#8A8AB0"))
        col.addView(button("Grant overlay permission") {
            startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            )
        })
        if (Build.VERSION.SDK_INT >= 33) {
            col.addView(text("2. Allow notifications so the floating service stays alive.", 14f, color = "#8A8AB0"))
            col.addView(button("Allow notifications") {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            })
        }

        col.addView(text("How to use on MeetMe", 16f, true))
        col.addView(text(
            "• Start your MeetMe live as usual (camera on your face).\n" +
            "• A small bubble floats on your screen — drag it anywhere.\n" +
            "• Tap it: the mod menu opens. Sounds play through your mic to viewers.\n" +
            "• Jokes, facts, pickup lines, WYR, weird laws and tarot readings appear as cards — read them aloud in your own style.\n" +
            "• Tap X to close the menu; the bubble stays for instant access.\n" +
            "• For apps WITH screen share (TikTok, BIGO, Twitch): use the web version for the full on-screen canvas.",
            14f, color = "#8A8AB0"
        ))

        col.addView(text("Not legal advice: weird laws are for entertainment.", 12f, color = "#55556F"))

        showCrashReport(col, pad)
    }

    private fun showCrashReport(col: LinearLayout, pad: Int) {
        val f = File(filesDir, "crash.log")
        if (!f.exists()) return
        val trace = try { f.readText() } catch (e: Exception) { return }
        val firstLines = trace.lineSequence().take(6).joinToString("\n")
        col.addView(TextView(this).apply {
            text = "LAST ERROR (tap to copy — send this to the developer)\n\n$firstLines"
            textSize = 12f
            setTextColor(Color.parseColor("#EF4444"))
            setPadding(0, pad, 0, pad / 2)
            setOnClickListener {
                val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("crash", trace))
                android.widget.Toast.makeText(this@MainActivity, "Crash report copied", android.widget.Toast.LENGTH_SHORT).show()
            }
        })
        col.addView(Button(this).apply {
            text = "Clear error"; isAllCaps = false
            setOnClickListener { f.delete(); recreate() }
        })
    }

    override fun onResume() { super.onResume(); refresh() }

    private fun refresh() {
        val canOverlay = Settings.canDrawOverlays(this)
        startBtn.isEnabled = canOverlay
        startBtn.alpha = if (canOverlay) 1f else 0.4f
        status.text = when {
            !canOverlay -> "Overlay permission needed — tap below"
            else -> "Ready! Tap \"Start floating mod menu\", then open MeetMe and go live."
        }
    }

    private fun startOverlay() {
        if (!Settings.canDrawOverlays(this)) return
        val i = Intent(this, OverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(i) else startService(i)
        refresh()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            refresh()
        }
    }
}
