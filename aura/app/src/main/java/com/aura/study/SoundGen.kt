package com.aura.study

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/** Ambient sound generated in code (no audio files). [Generator] makes the samples, [SoundPlayer] streams them. */
object Sounds {
    val KINDS = listOf("off" to "Off", "rain" to "Rain", "waves" to "Waves", "wind" to "Wind", "fire" to "Fire", "crickets" to "Night", "brown" to "Deep", "white" to "Static")
}

const val SAMPLE_RATE = 24000

class Generator(private val kind: String, seed: Int = 1) {
    private val rnd = Random(seed)
    private var t = 0L
    private var b0 = 0f; private var b1 = 0f; private var b2 = 0f; private var b3 = 0f; private var b4 = 0f; private var b5 = 0f; private var b6 = 0f
    private var brown = 0f; private var lp = 0f; private var lp2 = 0f
    private var drop = 0f; private var dropFreq = 0f; private var dropPhase = 0f
    private var crackle = 0f
    private var chirpLeft = 0; private var chirpPhase = 0f; private var chirpFreq = 4300f; private var nextChirp = 4000

    private fun white() = rnd.nextFloat() * 2f - 1f
    private fun pink(): Float {
        val w = white()
        b0 = .99886f * b0 + w * .0555179f; b1 = .99332f * b1 + w * .0750759f; b2 = .96900f * b2 + w * .1538520f
        b3 = .86650f * b3 + w * .3104856f; b4 = .55000f * b4 + w * .5329522f; b5 = -.7616f * b5 - w * .0168980f
        val out = b0 + b1 + b2 + b3 + b4 + b5 + b6 + w * .5362f; b6 = w * .115926f
        return out * .11f
    }
    private fun brown(): Float { brown = (brown + white() * .02f) / 1.02f; return brown * 3.5f }

    fun fill(out: ShortArray, count: Int = out.size) {
        for (i in 0 until count) {
            val sec = t / SAMPLE_RATE.toFloat()
            val v: Float = when (kind) {
                "white" -> white() * .35f
                "brown" -> brown() * .8f
                "rain" -> {
                    lp += (pink() - lp) * .35f
                    if (rnd.nextInt(SAMPLE_RATE / 55) == 0) { drop = .25f * rnd.nextFloat(); dropFreq = 1800f + rnd.nextFloat() * 2600f; dropPhase = 0f }
                    drop *= .9965f; dropPhase += (2 * PI.toFloat() * dropFreq / SAMPLE_RATE)
                    lp * .9f + sin(dropPhase) * drop * .5f + white() * .05f
                }
                "wind" -> {
                    val cut = .02f + .05f * (.5f + .5f * sin(sec * .23f)) + .03f * (.5f + .5f * sin(sec * .61f + 1f))
                    lp += (white() - lp) * cut; lp2 += (lp - lp2) * .3f
                    lp2 * (2.2f + 1.4f * sin(sec * .17f))
                }
                "waves" -> {
                    val swell = (.5f + .5f * sin(sec * 2f * PI.toFloat() / 9f)).let { it * it }
                    lp += (white() - lp) * (.01f + .05f * swell)
                    brown() * .35f + lp * (.9f + 2.2f * swell)
                }
                "fire" -> {
                    if (rnd.nextInt(SAMPLE_RATE / 9) == 0) crackle = .35f + rnd.nextFloat() * .5f
                    crackle *= .993f
                    brown() * .5f + white() * crackle * .6f * (if (rnd.nextInt(3) == 0) 1f else .3f)
                }
                "crickets" -> {
                    nextChirp--
                    if (nextChirp <= 0 && chirpLeft <= 0) { chirpLeft = (SAMPLE_RATE * .09f).toInt(); chirpFreq = 4000f + rnd.nextFloat() * 700f; nextChirp = (SAMPLE_RATE * (.12f + rnd.nextFloat() * .6f)).toInt() }
                    var c = 0f
                    if (chirpLeft > 0) { chirpLeft--; chirpPhase += 2 * PI.toFloat() * chirpFreq / SAMPLE_RATE; c = sin(chirpPhase) * (.5f + .5f * sin(chirpLeft * .03f)) * .12f }
                    lp += (pink() - lp) * .1f
                    c + lp * .5f
                }
                else -> 0f
            }
            out[i] = (v.coerceIn(-1f, 1f) * 30000f).toInt().toShort()
            t++
        }
    }
}

class SoundPlayer {
    private var track: AudioTrack? = null
    private var thread: Thread? = null
    @Volatile private var running = false
    @Volatile private var gain = .6f
    @Volatile var kind = "off"; private set

    fun setVolume(percent: Int) { gain = percent / 100f; track?.setVolume(gain) }

    fun play(k: String, percent: Int) {
        if (k == kind && running) { setVolume(percent); return }
        stop()
        kind = k
        if (k == "off") return
        gain = percent / 100f
        val min = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT).coerceAtLeast(4096)
        val t = try {
            AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                .setAudioFormat(AudioFormat.Builder().setSampleRate(SAMPLE_RATE).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).setEncoding(AudioFormat.ENCODING_PCM_16BIT).build())
                .setBufferSizeInBytes(min * 2).setTransferMode(AudioTrack.MODE_STREAM).build()
        } catch (e: Exception) { return }
        t.setVolume(gain); t.play(); track = t; running = true
        val gen = Generator(k, (System.nanoTime() and 0xFFFF).toInt())
        thread = Thread {
            val buf = ShortArray(2048)
            while (running) { gen.fill(buf); if (t.write(buf, 0, buf.size) < 0) break }
        }.apply { isDaemon = true; start() }
    }

    fun stop() {
        running = false
        runCatching { thread?.join(300) }
        runCatching { track?.stop(); track?.release() }
        track = null; thread = null; kind = "off"
    }
}
