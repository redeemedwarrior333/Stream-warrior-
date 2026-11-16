package com.streamwarrior.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import kotlin.random.Random

/**
 * Floating overlay: a draggable bubble that expands into the mod menu
 * (soundboard + game cards) on top of any app — MeetMe included.
 * The overlay never steals focus, so the streaming app keeps working.
 */
class OverlayService : Service() {

    private lateinit var wm: WindowManager
    private var bubble: View? = null
    private var panel: View? = null
    private lateinit var contentText: TextView
    private var expanded = false
    private var category: String = "Joke"
    private var deckLove = false
    private val indices = mutableMapOf<String, Int>()
    private var lastLawState: String? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        startForeground()
        showBubble()
    }

    private fun startForeground() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel("sw_overlay", "Stream Warrior", NotificationManager.IMPORTANCE_LOW)
            )
        }
        val b = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            Notification.Builder(this, "sw_overlay") else Notification.Builder(this)
        b.setContentTitle("Stream Warrior active")
            .setContentText("Mod menu floating — tap to open")
            .setSmallIcon(android.R.drawable.ic_media_play)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, b.build(), android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(1, b.build())
        }
    }

    private fun dp(v: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()

    private fun params(x: Int, y: Int, w: Int = WindowManager.LayoutParams.WRAP_CONTENT): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            w, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START; this.x = x; this.y = y }
    }

    private fun panelBg(): GradientDrawable =
        GradientDrawable().apply {
            setColor(Color.parseColor("#EE171728"))
            cornerRadius = dp(16).toFloat()
            setStroke(dp(1), Color.parseColor("#7C3AED"))
        }

    private fun makeButton(label: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            text = label
            textSize = 12f
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#1F1F36"))
                cornerRadius = dp(10).toFloat()
                setStroke(dp(1), Color.parseColor("#2A2A45"))
            }
            setPadding(dp(10), dp(8), dp(10), dp(8))
            isAllCaps = false
            setOnClickListener { onClick() }
        }
    }

    /* ---------- bubble ---------- */
    private fun showBubble() {
        val size = dp(56)
        val v = TextView(this).apply {
            text = "⚡"
            textSize = 26f
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                colors = intArrayOf(Color.parseColor("#7C3AED"), Color.parseColor("#EC4899"))
                orientation = GradientDrawable.Orientation.TL_BR
                cornerRadius = dp(28).toFloat()
            }
        }
        val p = params(30, 300, size)
        var downX = 0f; var downY = 0f; var startX = 0; var startY = 0; var moved = false
        v.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> { downX = e.rawX; downY = e.rawY; startX = p.x; startY = p.y; moved = false; true }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (e.rawX - downX).toInt(); val dy = (e.rawY - downY).toInt()
                    if (moved || dx * dx + dy * dy > dp(8) * dp(8)) {
                        moved = true; p.x = startX + dx; p.y = startY + dy
                        wm.updateViewLayout(v, p)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> { if (!moved) togglePanel(); true }
                else -> false
            }
        }
        wm.addView(v, p)
        bubble = v
    }

    private fun togglePanel() {
        SoundSynth.play(if (expanded) "whoosh" else "ding")
        if (expanded) hidePanel() else showPanel()
    }

    /* ---------- panel ---------- */
    private fun showPanel() {
        val root = ScrollView(this).apply { background = panelBg() }
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(14))
        }
        root.addView(col)

        val head = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        head.addView(TextView(this).apply {
            text = "⚔️ Stream Warrior"
            textSize = 14f; setTextColor(Color.WHITE); setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })
        head.addView(makeButton("X") { hidePanel() })
        col.addView(head)

        contentText = TextView(this).apply {
            textSize = 15f
            setTextColor(Color.parseColor("#F0F0FF"))
            setPadding(dp(4), dp(10), dp(4), dp(10))
            text = "Pick a category, then tap Next to run your stream."
        }
        col.addView(contentText)

        col.addView(makeButton("Next") { nextContent() })

        val cats = HorizontalScrollView(this)
        val catRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0, dp(8), 0, dp(4)) }
        listOf("Joke", "Fact", "Line", "WYR", "Law", "Tarot", "Love Tarot").forEach { c ->
            val b = makeButton(c) {
                if (c == "Love Tarot") deckLove = true
                if (c == "Tarot") deckLove = false
                category = c; nextContent()
            }
            b.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = dp(6) }
            catRow.addView(b)
        }
        cats.addView(catRow)
        col.addView(cats)

        col.addView(TextView(this).apply {
            text = "Soundboard"; textSize = 12f; setTextColor(Color.parseColor("#8A8AB0"))
            setPadding(dp(4), dp(10), 0, dp(4))
        })
        val sounds = SoundSynth.SOUNDS
        var i = 0
        while (i < sounds.size) {
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            for (j in 0 until 3) {
                if (i + j >= sounds.size) break
                val b = makeButton(sounds[i + j]) { SoundSynth.play(SoundSynth.key(sounds[i + j])) }
                b.layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                ).apply { marginEnd = dp(6); bottomMargin = dp(6) }
                row.addView(b)
            }
            col.addView(row)
            i += 3
        }

        val p = params(30, 120, dp(320))
        wm.addView(root, p)
        panel = root
        expanded = true
        bubble?.visibility = View.GONE
    }

    private fun nextContent() {
        val text = when (category) {
            "Joke" -> pick("Joke", Content.JOKES)
            "Fact" -> pick("Fact", Content.FACTS)
            "Line" -> pick("Line", Content.LINES)
            "WYR" -> "Would you rather: " + pick("WYR", Content.WYR)
            "Law" -> {
                val state = Content.LAWS.keys.filter { it != lastLawState }.random()
                lastLawState = state
                "Weird Law — $state: " + Content.LAWS[state]!!.random()
            }
            "Tarot", "Love Tarot" -> {
                val card = Content.TAROT.random()
                val rev = Random.nextBoolean()
                val meaning = when {
                    deckLove && rev -> card.loveRev
                    deckLove -> card.loveUp
                    rev -> card.rev
                    else -> card.up
                }
                "${card.sym} ${card.name}${if (rev) " (Reversed)" else ""}\n$meaning"
            }
            else -> "Pick a category"
        }
        contentText.text = text
        SoundSynth.play("boing")
    }

    private fun pick(key: String, list: List<String>): String {
        val last = indices[key] ?: -1
        var i = Random.nextInt(list.size)
        if (i == last && list.size > 1) i = (i + 1 + Random.nextInt(list.size - 1)) % list.size
        indices[key] = i
        return list[i]
    }

    private fun hidePanel() {
        panel?.let { wm.removeView(it) }
        panel = null
        expanded = false
        bubble?.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        panel?.let { try { wm.removeView(it) } catch (e: Exception) {} }
        bubble?.let { try { wm.removeView(it) } catch (e: Exception) {} }
        panel = null; bubble = null
        super.onDestroy()
    }
}
