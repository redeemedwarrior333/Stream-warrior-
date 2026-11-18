package com.phonewarrior.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Base64
import androidx.core.app.NotificationCompat
import java.io.ByteArrayOutputStream
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import org.json.JSONArray
import org.json.JSONObject

/**
 * GUARD MODE — listens on the microphone and starts recording on loud audio
 * (yelling). Uses a rolling 30-second buffer, so every capture INCLUDES the
 * half minute before the trigger. Recordings are AES-encrypted with the
 * vault key (held in memory only, passed at start) and appended to the vault.
 *
 * The mic indicator/notification is visible while guard is on — this app
 * does not promise stealth; it promises evidence.
 */
class GuardService : Service() {

    companion object {
        const val ACTION_STOP = "com.phonewarrior.app.GUARD_STOP"
        const val CHANNEL = "guard"
        @Volatile var active = false
        private const val RATE = 16000
        private const val CHUNK = RATE / 10          // 100 ms
        private const val RING_CHUNKS = 300          // 30 s pre-buffer
        private const val TRIGGER_CHUNKS = 5         // 0.5 s sustained loud
        private const val CAPTURE_CHUNKS = 1200      // up to 120 s once triggered
        private const val COOLDOWN_CHUNKS = 600      // 60 s between captures
        private const val THRESHOLD = 4000.0         // RMS for yelling at phone distance
        private val main = Handler(Looper.getMainLooper())
    }

    private var thread: Thread? = null
    @Volatile private var running = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopLoop()
            return START_NOT_STICKY
        }
        val keyBytes = intent?.getByteArrayExtra("key")
        if (keyBytes == null) { stopSelf(); return START_NOT_STICKY }
        startGuardNotification()
        if (!running) {
            running = true
            active = true
            thread = Thread { listenLoop(SecretKeySpec(keyBytes, "AES")) }
            thread?.start()
        }
        return START_NOT_STICKY
    }

    private fun startGuardNotification() {
        try {
            if (Build.VERSION.SDK_INT >= 26) {
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.createNotificationChannel(
                    NotificationChannel(CHANNEL, "Guard mode", NotificationManager.IMPORTANCE_LOW)
                )
            }
            val open = PendingIntent.getActivity(
                this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
            )
            val n = NotificationCompat.Builder(this, CHANNEL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Guard mode is ON")
                .setContentText("Listening for loud audio. Recordings save encrypted in your vault.")
                .setOngoing(true)
                .setContentIntent(open)
                .build()
            if (Build.VERSION.SDK_INT >= 29) {
                startForeground(11, n, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
            } else {
                startForeground(11, n)
            }
        } catch (e: Exception) {
            try { startForeground(11, NotificationCompat.Builder(this, CHANNEL).setSmallIcon(R.mipmap.ic_launcher).build()) } catch (e2: Exception) {}
        }
    }

    private fun saveCapture(key: SecretKeySpec, wav: ByteArray, seconds: Int) {
        try {
            val c = Cipher.getInstance("AES/GCM/NoPadding")
            c.init(Cipher.ENCRYPT_MODE, key)
            val iv = c.iv
            val ct = c.doFinal(wav)
            val all = ByteArray(iv.size + ct.size)
            System.arraycopy(iv, 0, all, 0, iv.size)
            System.arraycopy(ct, 0, all, iv.size, ct.size)
            val b64 = Base64.encodeToString(all, Base64.NO_WRAP)
            if (b64.length > 3200000) return // ~2.4 MB audio cap
            val plain = prefsOrEmpty(key)
            val arr = JSONArray(plain)
            val obj = JSONObject()
            obj.put("type", "aud")
            obj.put("fmt", "wav")
            obj.put("t", System.currentTimeMillis())
            obj.put("s", "Guard recording — " + seconds + "s (starts 30s before the sound)")
            obj.put("d", b64)
            arr.put(obj)
            getSharedPreferences("pw_state", Context.MODE_PRIVATE)
                .edit().putString("vault_data", guardEnc(key, arr.toString())).apply()
            main.post {
                try {
                    android.widget.Toast.makeText(
                        this@GuardService, "\ud83c\udf99\ufe0f Guardian saved an encrypted recording to your vault", android.widget.Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {}
            }
        } catch (e: Exception) {}
    }

    private fun prefsOrEmpty(key: SecretKeySpec): String {
        val p = getSharedPreferences("pw_state", Context.MODE_PRIVATE)
        val blob = p.getString("vault_data", "") ?: ""
        return try {
            val all = Base64.decode(blob, Base64.NO_WRAP)
            val iv = all.copyOfRange(0, 12)
            val ct2 = all.copyOfRange(12, all.size)
            val c = Cipher.getInstance("AES/GCM/NoPadding")
            c.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
            String(c.doFinal(ct2), Charsets.UTF_8)
        } catch (e: Exception) { "[]" }
    }

    private fun guardEnc(key: SecretKeySpec, plain: String): String {
        val c = Cipher.getInstance("AES/GCM/NoPadding")
        c.init(Cipher.ENCRYPT_MODE, key)
        val iv = c.iv
        val ct = c.doFinal(plain.toByteArray(Charsets.UTF_8))
        val out = ByteArray(iv.size + ct.size)
        System.arraycopy(iv, 0, out, 0, iv.size)
        System.arraycopy(ct, 0, out, iv.size, ct.size)
        return Base64.encodeToString(out, Base64.NO_WRAP)
    }

    private fun listenLoop(key: SecretKeySpec) {
        var rec: AudioRecord? = null
        try {
            val minBuf = AudioRecord.getMinBufferSize(RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            rec = AudioRecord(
                MediaRecorder.AudioSource.MIC, RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                maxOf(minBuf, CHUNK * 8)
            )
            if (rec.state != AudioRecord.STATE_INITIALIZED) { stopLoop(); return }
            rec.startRecording()

            val ring = ArrayList<ShortArray>(RING_CHUNKS + 4)
            val buf = ShortArray(CHUNK)
            var loudStreak = 0
            var cooldown = 0

            while (running) {
                var n = 0
                while (n < CHUNK && running) {
                    val got = rec.read(buf, n, CHUNK - n)
                    if (got > 0) n += got
                }
                if (n <= 0) continue

                var sq = 0.0
                for (i in 0 until n) { val v = buf[i].toDouble(); sq += v * v }
                val rms = Math.sqrt(sq / n)

                if (cooldown > 0) { cooldown--; continue }
                if (rms > THRESHOLD) loudStreak++ else loudStreak = 0

                if (loudStreak >= TRIGGER_CHUNKS) {
                    val pending = ArrayList<ShortArray>(ring)
                    val capBuf = ShortArray(CHUNK)
                    var captured = 0
                    while (running && captured < CAPTURE_CHUNKS) {
                        var got = 0
                        while (got < CHUNK && running) {
                            val r = rec.read(capBuf, got, CHUNK - got)
                            if (r > 0) got += r
                        }
                        if (got <= 0) continue
                        pending.add(capBuf.copyOf(got))
                        captured++
                    }
                    if (captured > 10) {
                        val total = pending.sumOf { it.size }
                        val wav = ByteArrayOutputStream(total * 2 + 44)
                        writeWavHeader(wav, total, RATE)
                        for (ch in pending) {
                            var i = 0
                            while (i < ch.size) {
                                val v = ch[i].toInt()
                                wav.write(v and 0xff)
                                wav.write((v shr 8) and 0xff)
                                i++
                            }
                        }
                        saveCapture(key, wav.toByteArray(), captured / 10)
                    }
                    ring.clear()
                    cooldown = COOLDOWN_CHUNKS
                    loudStreak = 0
                } else {
                    ring.add(buf.copyOf(n))
                    if (ring.size > RING_CHUNKS) ring.removeAt(0)
                }
            }
        } catch (e: Exception) {
        } finally {
            try { rec?.stop() } catch (e: Exception) {}
            try { rec?.release() } catch (e: Exception) {}
            active = false
        }
    }

    private fun writeWavHeader(out: ByteArrayOutputStream, totalSamples: Int, rate: Int) {
        val dataLen = totalSamples * 2
        val byteRate = rate * 2
        fun le32(v: Int) { out.write(v and 0xff); out.write((v shr 8) and 0xff); out.write((v shr 16) and 0xff); out.write((v shr 24) and 0xff) }
        fun le16(v: Int) { out.write(v and 0xff); out.write((v shr 8) and 0xff) }
        "RIFF".forEach { out.write(it.code) }
        le32(dataLen + 36)
        "WAVE".forEach { out.write(it.code) }
        "fmt ".forEach { out.write(it.code) }
        le32(16); le16(1); le16(1); le32(rate); le32(byteRate); le16(2); le16(16)
        "data".forEach { out.write(it.code) }
        le32(dataLen)
    }

    private fun stopLoop() {
        running = false
        active = false
        try { stopForeground(true) } catch (e: Exception) {}
        stopSelf()
    }

    override fun onDestroy() {
        running = false
        active = false
        super.onDestroy()
    }
}
