package com.streamwarrior.app.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

data class SoundButtonDef(
    val id: String,
    val emoji: String,
    val label: String
)

object SoundSynth {
    private const val SAMPLE_RATE = 22050
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    private var currentJob: Job? = null

    @Volatile
    private var currentTrack: AudioTrack? = null

    @Volatile
    private var currentPlayer: MediaPlayer? = null

    val builtInSounds = listOf(
        SoundButtonDef("airhorn", "\uD83D\uDCE2", "Airhorn"),
        SoundButtonDef("drumroll", "\uD83E\uDD41", "Drumroll"),
        SoundButtonDef("applause", "\uD83D\uDC4F", "Applause"),
        SoundButtonDef("trombone", "\uD83C\uDFBA", "Sad Trombone"),
        SoundButtonDef("buzzer", "\u274C", "Buzzer"),
        SoundButtonDef("ding", "\uD83D\uDD14", "Ding"),
        SoundButtonDef("laser", "\uD83D\uDD2B", "Laser"),
        SoundButtonDef("whoosh", "\uD83D\uDCA8", "Whoosh"),
        SoundButtonDef("boing", "\uD83E\uDD98", "Boing"),
        SoundButtonDef("heartbeat", "\uD83D\uDC93", "Heartbeat"),
        SoundButtonDef("tada", "\uD83C\uDF89", "Ta-da"),
        SoundButtonDef("crickets", "\uD83E\uDD97", "Crickets"),
        SoundButtonDef("drumhit", "\uD83D\uDCA5", "Drum Hit"),
        SoundButtonDef("rimshot", "\uD83E\uDD41", "Rimshot")
    )

    fun play(id: String) {
        stopCurrent()
        currentJob = scope.launch {
            val pcm = synthesize(id) ?: return@launch
            if (!isActive) return@launch
            playPcm(pcm)
        }
    }

    fun playCustomFile(path: String, onError: () -> Unit = {}) {
        stopCurrent()
        currentJob = scope.launch(Dispatchers.IO) {
            try {
                val file = File(path)
                if (!file.exists()) {
                    onError()
                    return@launch
                }
                val player = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    setDataSource(file.absolutePath)
                    prepare()
                    setOnCompletionListener { mp ->
                        mp.release()
                        if (currentPlayer === mp) currentPlayer = null
                    }
                    start()
                }
                currentPlayer = player
            } catch (_: Exception) {
                onError()
            }
        }
    }

    fun stopCurrent() {
        currentJob?.cancel()
        currentJob = null
        try {
            currentTrack?.let { tr ->
                if (tr.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    tr.pause()
                    tr.flush()
                }
                tr.release()
            }
        } catch (_: Exception) {
        } finally {
            currentTrack = null
        }
        try {
            currentPlayer?.let { mp ->
                if (mp.isPlaying) mp.stop()
                mp.release()
            }
        } catch (_: Exception) {
        } finally {
            currentPlayer = null
        }
    }

    private fun playPcm(pcm: ShortArray) {
        try {
            val minBuf = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val byteCount = max(minBuf, pcm.size * 2)
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(byteCount)
                .build()
            track.write(pcm, 0, pcm.size)
            currentTrack = track
            track.play()
        } catch (_: Exception) {
        }
    }

    private fun synthesize(id: String): ShortArray? {
        return when (id) {
            "airhorn" -> buildAirhorn()
            "drumroll" -> buildDrumroll()
            "applause" -> buildApplause()
            "trombone" -> buildTrombone()
            "buzzer" -> buildBuzzer()
            "ding" -> buildDing()
            "laser" -> buildLaser()
            "whoosh" -> buildWhoosh()
            "boing" -> buildBoing()
            "heartbeat" -> buildHeartbeat()
            "tada" -> buildTada()
            "crickets" -> buildCrickets()
            "drumhit" -> buildDrumHit()
            "rimshot" -> buildRimshot()
            "alert_follow" -> buildSequence(listOf(660.0 to 0.12, 880.0 to 0.20), wave = 1)
            "alert_sub" -> buildSequence(listOf(523.25 to 0.11, 659.25 to 0.11, 783.99 to 0.24), wave = 1)
            "alert_tip" -> buildSequence(listOf(987.77 to 0.09, 1318.5 to 0.16, 1567.98 to 0.24), wave = 2)
            "alert_raid" -> buildSequence(listOf(196.0 to 0.20, 392.0 to 0.20, 523.25 to 0.35), wave = 0)
            else -> null
        }
    }

    private fun toShort(samples: DoubleArray): ShortArray {
        return ShortArray(samples.size) { i ->
            val clamped = max(-1.0, min(1.0, samples[i]))
            (clamped * Short.MAX_VALUE).toInt().toShort()
        }
    }

    private fun saw(phase: Double): Double = 2.0 * (phase - kotlin.math.floor(phase + 0.5))

    private fun buildAirhorn(): ShortArray {
        val len = (SAMPLE_RATE * 0.90).toInt()
        val out = DoubleArray(len)
        val freqs = doubleArrayOf(349.23, 466.16, 523.25)
        for (i in 0 until len) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = when {
                t < 0.02 -> t / 0.02
                t > 0.78 -> max(0.0, (0.90 - t) / 0.12)
                else -> 1.0
            }
            var sum = 0.0
            for (f in freqs) sum += saw(t * f)
            out[i] = (sum / freqs.size) * 0.65 * env
        }
        return toShort(out)
    }

    private fun buildDrumroll(): ShortArray {
        val len = (SAMPLE_RATE * 1.45).toInt()
        val out = DoubleArray(len)
        val rng = Random(42)
        val hitInterval = (SAMPLE_RATE * 0.055).toInt()
        var i = 0
        while (i < len) {
            val hitLen = min(hitInterval, len - i)
            val progress = i.toDouble() / len
            val amp = 0.25 + 0.55 * progress
            for (j in 0 until hitLen) {
                val t = j.toDouble() / SAMPLE_RATE
                val decay = exp(-t * 45.0)
                val noise = (rng.nextDouble() * 2.0 - 1.0)
                val body = sin(2.0 * PI * 140.0 * t)
                out[i + j] = (noise * 0.65 + body * 0.35) * decay * amp
            }
            i += hitInterval
        }
        // Final crash
        val crashStart = (SAMPLE_RATE * 1.25).toInt()
        for (j in crashStart until len) {
            val t = (j - crashStart).toDouble() / SAMPLE_RATE
            out[j] += (rng.nextDouble() * 2.0 - 1.0) * exp(-t * 6.0) * 0.55
        }
        return toShort(out)
    }

    private fun buildApplause(): ShortArray {
        val len = (SAMPLE_RATE * 1.80).toInt()
        val out = DoubleArray(len)
        val rng = Random(7)
        for (clap in 0 until 42) {
            val start = rng.nextInt(0, (SAMPLE_RATE * 1.45).toInt())
            val dur = (SAMPLE_RATE * 0.07).toInt()
            for (j in 0 until dur) {
                val idx = start + j
                if (idx >= len) break
                val t = j.toDouble() / SAMPLE_RATE
                val env = exp(-t * 55.0)
                out[idx] += (rng.nextDouble() * 2.0 - 1.0) * env * 0.28
            }
        }
        return toShort(out)
    }

    private fun buildTrombone(): ShortArray {
        val notes = listOf(311.13 to 0.32, 293.66 to 0.32, 277.18 to 0.32, 261.63 to 0.85)
        val totalSamples = notes.sumOf { (it.second * SAMPLE_RATE).toInt() }
        val out = DoubleArray(totalSamples)
        var offset = 0
        notes.forEachIndexed { idx, (freq, dur) ->
            val nLen = (dur * SAMPLE_RATE).toInt()
            var phase = 0.0
            for (j in 0 until nLen) {
                val t = j.toDouble() / SAMPLE_RATE
                val f = if (idx == 3 && t > 0.2) freq * (1.0 - 0.14 * ((t - 0.2) / 0.65)) else freq
                phase += f / SAMPLE_RATE
                val env = min(1.0, t / 0.025) * max(0.0, 1.0 - t / dur)
                out[offset + j] = (saw(phase) * 0.6 + sin(2.0 * PI * phase) * 0.4) * env * 0.65
            }
            offset += nLen
        }
        return toShort(out)
    }

    private fun buildBuzzer(): ShortArray {
        val len = (SAMPLE_RATE * 0.55).toInt()
        val out = DoubleArray(len)
        for (i in 0 until len) {
            val t = i.toDouble() / SAMPLE_RATE
            val sq1 = if (sin(2.0 * PI * 145.0 * t) >= 0) 1.0 else -1.0
            val sq2 = if (sin(2.0 * PI * 151.0 * t) >= 0) 1.0 else -1.0
            val env = max(0.0, 1.0 - t / 0.55)
            out[i] = (sq1 + sq2) * 0.28 * env
        }
        return toShort(out)
    }

    private fun buildDing(): ShortArray {
        val len = (SAMPLE_RATE * 0.80).toInt()
        val out = DoubleArray(len)
        for (i in 0 until len) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = exp(-t * 5.2)
            out[i] = (sin(2.0 * PI * 1046.5 * t) * 0.7 + sin(2.0 * PI * 2093.0 * t) * 0.3) * env * 0.75
        }
        return toShort(out)
    }

    private fun buildLaser(): ShortArray {
        val len = (SAMPLE_RATE * 0.36).toInt()
        val out = DoubleArray(len)
        var phase = 0.0
        for (i in 0 until len) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = 1400.0 * exp(-t * 7.5) + 120.0
            phase += freq / SAMPLE_RATE
            val env = max(0.0, 1.0 - t / 0.36)
            out[i] = saw(phase) * env * 0.6
        }
        return toShort(out)
    }

    private fun buildWhoosh(): ShortArray {
        val len = (SAMPLE_RATE * 0.48).toInt()
        val out = DoubleArray(len)
        val rng = Random(99)
        var lp = 0.0
        for (i in 0 until len) {
            val p = i.toDouble() / len
            val bell = sin(PI * p)
            val n = rng.nextDouble() * 2.0 - 1.0
            val alpha = 0.08 + 0.45 * bell
            lp += alpha * (n - lp)
            out[i] = lp * bell * 0.85
        }
        return toShort(out)
    }

    private fun buildBoing(): ShortArray {
        val len = (SAMPLE_RATE * 0.52).toInt()
        val out = DoubleArray(len)
        var phase = 0.0
        for (i in 0 until len) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = 220.0 + 340.0 * t + 65.0 * sin(2.0 * PI * 16.0 * t)
            phase += freq / SAMPLE_RATE
            val env = exp(-t * 4.2)
            out[i] = sin(2.0 * PI * phase) * env * 0.8
        }
        return toShort(out)
    }

    private fun buildHeartbeat(): ShortArray {
        val len = (SAMPLE_RATE * 0.90).toInt()
        val out = DoubleArray(len)
        val thumps = listOf(0.0 to 1.0, 0.25 to 0.72)
        for ((startSec, amp) in thumps) {
            val s = (startSec * SAMPLE_RATE).toInt()
            val d = (0.22 * SAMPLE_RATE).toInt()
            for (j in 0 until d) {
                val idx = s + j
                if (idx >= len) break
                val t = j.toDouble() / SAMPLE_RATE
                val freq = 75.0 - 30.0 * (t / 0.22)
                out[idx] += sin(2.0 * PI * freq * t) * exp(-t * 14.0) * amp * 0.9
            }
        }
        return toShort(out)
    }

    private fun buildTada(): ShortArray =
        buildSequence(
            listOf(523.25 to 0.11, 659.25 to 0.11, 783.99 to 0.11, 1046.5 to 0.48),
            wave = 1
        )

    private fun buildCrickets(): ShortArray {
        val len = (SAMPLE_RATE * 1.35).toInt()
        val out = DoubleArray(len)
        val bursts = listOf(0.05, 0.22, 0.65, 0.82)
        for (startSec in bursts) {
            val s = (startSec * SAMPLE_RATE).toInt()
            val d = (0.11 * SAMPLE_RATE).toInt()
            for (j in 0 until d) {
                val idx = s + j
                if (idx >= len) break
                val t = j.toDouble() / SAMPLE_RATE
                val pulse = max(0.0, sin(2.0 * PI * 36.0 * t))
                out[idx] += sin(2.0 * PI * 4100.0 * t) * pulse * 0.35
            }
        }
        return toShort(out)
    }

    private fun buildDrumHit(): ShortArray {
        val len = (SAMPLE_RATE * 0.32).toInt()
        val out = DoubleArray(len)
        val rng = Random(17)
        for (i in 0 until len) {
            val t = i.toDouble() / SAMPLE_RATE
            val freq = 180.0 * exp(-t * 16.0) + 52.0
            val thump = sin(2.0 * PI * freq * t) * exp(-t * 9.0)
            val snap = (rng.nextDouble() * 2.0 - 1.0) * exp(-t * 45.0)
            out[i] = (thump * 0.85 + snap * 0.35) * 0.9
        }
        return toShort(out)
    }

    private fun buildRimshot(): ShortArray {
        val len = (SAMPLE_RATE * 0.65).toInt()
        val out = DoubleArray(len)
        val rng = Random(23)
        // Ba-dum-tss
        val hits = listOf(0.0 to 130.0, 0.16 to 105.0)
        for ((startSec, f) in hits) {
            val s = (startSec * SAMPLE_RATE).toInt()
            val d = (0.14 * SAMPLE_RATE).toInt()
            for (j in 0 until d) {
                val idx = s + j
                if (idx >= len) break
                val t = j.toDouble() / SAMPLE_RATE
                out[idx] += (sin(2.0 * PI * f * t) + (rng.nextDouble() * 2 - 1) * 0.35) * exp(-t * 22.0) * 0.75
            }
        }
        val tssStart = (0.33 * SAMPLE_RATE).toInt()
        for (idx in tssStart until len) {
            val t = (idx - tssStart).toDouble() / SAMPLE_RATE
            out[idx] += (rng.nextDouble() * 2.0 - 1.0) * exp(-t * 10.0) * 0.55
        }
        return toShort(out)
    }

    private fun buildSequence(notes: List<Pair<Double, Double>>, wave: Int): ShortArray {
        val total = notes.sumOf { (it.second * SAMPLE_RATE).toInt() }
        val out = DoubleArray(total)
        var offset = 0
        for ((freq, dur) in notes) {
            val nLen = (dur * SAMPLE_RATE).toInt()
            for (j in 0 until nLen) {
                val t = j.toDouble() / SAMPLE_RATE
                val env = min(1.0, t / 0.015) * exp(-t * (2.6 / dur))
                val s = when (wave) {
                    0 -> saw(t * freq)
                    2 -> if (sin(2.0 * PI * freq * t) >= 0) 0.7 else -0.7
                    else -> sin(2.0 * PI * freq * t) + 0.3 * sin(4.0 * PI * freq * t)
                }
                out[offset + j] = s * env * 0.62
            }
            offset += nLen
        }
        return toShort(out)
    }
}
