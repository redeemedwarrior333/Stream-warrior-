package com.streamwarrior.app

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Runtime sound synthesis — no audio assets needed.
 * Ports the web app's WebAudio soundboard to raw PCM via AudioTrack.
 */
object SoundSynth {

    private const val RATE = 44100
    private var track: AudioTrack? = null

    private fun getTrack(): AudioTrack {
        val t = track
        if (t != null) return t
        val minBuf = AudioTrack.getMinBufferSize(
            RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        val at = AudioTrack(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
            AudioFormat.Builder()
                .setSampleRate(RATE)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build(),
            maxOf(minBuf, RATE * 2),
            AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )
        at.play()
        track = at
        return at
    }

    private fun saw(f: Double, t: Double) = 2.0 * ((t * f) % 1.0) - 1.0
    private fun sine(f: Double, t: Double) = sin(2 * PI * f * t)
    private fun square(f: Double, t: Double) = if (sin(2 * PI * f * t) >= 0) 0.7 else -0.7
    private fun noise() = Random.nextDouble(-1.0, 1.0)
    private fun env(t: Double, dur: Double, attack: Double = 0.01): Double {
        val a = if (t < attack) t / attack else exp(-(t - attack) * 4.0 / dur)
        return a.coerceIn(0.0, 1.0)
    }

    private fun play(dur: Double, gen: (t: Double) -> Double) {
        val n = (RATE * dur).toInt()
        val buf = ShortArray(n)
        for (i in 0 until n) {
            val t = i.toDouble() / RATE
            buf[i] = (gen(t).coerceIn(-1.0, 1.0) * Short.MAX_VALUE * 0.9).toInt().toShort()
        }
        getTrack().write(buf, 0, n)
    }

    private fun mix(t: Double, dur: Double, attack: Double, vararg parts: Pair<Double, (Double) -> Double>): Double {
        val e = env(t, dur, attack)
        return parts.fold(0.0) { acc, (vol, wave) -> acc + vol * wave(t) } * e
    }

    fun play(name: String) = try {
        when (name) {
            "airhorn" -> play(1.0) { t ->
                mix(t, 1.0, 0.02, 0.22 to { saw(233.0, t) }, 0.22 to { saw(466.0, t) }, 0.22 to { saw(587.0, t) }, 0.12 to { square(349.0, t) })
            }
            "drumroll" -> play(2.2) { t ->
                val roll = if ((t % 0.045) < 0.04) noise() * 0.28 * env(t % 0.045, 0.04, 0.002) else 0.0
                val thump = if (t > 1.8) sine(70.0, t) * 0.6 * env(t - 1.8, 0.4, 0.005) else 0.0
                roll + thump
            }
            "applause" -> play(2.4) { t ->
                val clap = if (Random.nextDouble() < 0.02) noise() * 0.35 * env(t % 0.05, 0.05, 0.001) else 0.0
                clap * env(t, 2.4, 0.05)
            }
            "trombone" -> play(2.1) { t ->
                val f = when {
                    t < 0.35 -> 196.0
                    t < 0.7 -> 185.0
                    t < 1.2 -> 165.0
                    else -> 110.0 - (t - 1.2) * 40
                }
                mix(t, 2.1, 0.03, 0.3 to { saw(f, t) })
            }
            "buzzer" -> play(0.7) { t -> mix(t, 0.7, 0.01, 0.3 to { square(110.0, t) }, 0.2 to { square(104.0, t) }) }
            "ding" -> play(0.6) { t -> mix(t, 0.6, 0.005, 0.35 to { sine(1319.0, t) }, 0.2 to { sine(1760.0, t) }) }
            "laser" -> play(0.4) { t -> mix(t, 0.4, 0.005, 0.3 to { saw(1800.0 - t * 4300.0, t) }) }
            "whoosh" -> play(0.6) { t -> mix(t, 0.6, 0.05, 0.35 to { noise() * (1.0 - t / 0.6) }) }
            "boing" -> play(0.6) { t -> mix(t, 0.6, 0.005, 0.4 to { sine(400.0 - t * 550.0, t) }) }
            "heartbeat" -> play(0.7) { t ->
                val b1 = if (t < 0.15) sine(55.0, t) * 0.8 * env(t, 0.15, 0.005) else 0.0
                val b2 = if (t in 0.28..0.43) sine(55.0, t - 0.28) * 0.7 * env(t - 0.28, 0.15, 0.005) else 0.0
                b1 + b2
            }
            "tada" -> play(0.8) { t ->
                val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.5)
                val idx = (t / 0.09).toInt().coerceAtMost(3)
                val local = t - idx * 0.09
                sine(notes[idx], local) * 0.32 * env(local, 0.45, 0.005)
            }
            "crickets" -> play(1.8) { t ->
                val chirp = (t % 0.3)
                if (chirp < 0.05 || (chirp in 0.08..0.13)) sine(4200.0, t) * 0.14 * env(chirp % 0.05, 0.05, 0.002) else 0.0
            }
            "drum" -> play(0.3) { t -> mix(t, 0.3, 0.002, 0.7 to { sine(150.0 - t * 350.0, t) }, 0.25 to { noise() * env(t, 0.08, 0.001) }) }
            "rimshot" -> play(0.8) { t ->
                val click = if (t < 0.05) noise() * 0.5 * env(t, 0.05, 0.001) else 0.0
                val thump = if (t > 0.35) sine(180.0 - (t - 0.35) * 300.0, t - 0.35) * 0.7 * env(t - 0.35, 0.3, 0.002) else 0.0
                click + thump
            }
            else -> Unit
        }
    } catch (e: Exception) {
        // audio is best-effort; never crash the overlay
    }

    val SOUNDS = listOf(
        "Airhorn", "Drumroll", "Applause", "Sad Trombone", "Buzzer", "Ding",
        "Laser", "Whoosh", "Boing", "Heartbeat", "Ta-da", "Crickets", "Drum Hit", "Rimshot"
    )

    fun key(label: String) = label.lowercase()
        .replace("sad trombone", "trombone").replace("ta-da", "tada")
        .replace("drum hit", "drum").replace("rimshot", "rimshot").replace(" ", "")
}
