package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.sin

/**
 * Robust procedural retro-futuristic sound synthesizer.
 * Uses pooled static AudioTracks for instantaneous SFX response with zero allocation,
 * and a streaming AudioTrack for ambient cosmic music.
 */
class SoundSystem {

    var sfxEnabled: Boolean = true
    var musicEnabled: Boolean = true
        set(value) {
            field = value
            if (value) {
                startMusic()
            } else {
                stopMusic()
            }
        }

    private val sampleRate = 44100
    private val sfxExecutor = Executors.newSingleThreadExecutor()
    private val scope = CoroutineScope(Dispatchers.Default)
    private var musicJob: Job? = null
    private var musicTrack: AudioTrack? = null

    // Pre-allocated AudioTracks for instantaneous sound response
    private var jumpTrack: AudioTrack? = null
    private var doubleJumpTrack: AudioTrack? = null
    private var crystalTrack: AudioTrack? = null
    private var powerUpTrack: AudioTrack? = null
    private var hazardTrack: AudioTrack? = null
    private var dashTrack: AudioTrack? = null
    private var levelCompleteTrack: AudioTrack? = null
    private var clickTrack: AudioTrack? = null

    init {
        initStaticTracks()
    }

    private fun initStaticTracks() {
        sfxExecutor.execute {
            try {
                jumpTrack = createStaticTrack(generateSweep(320.0, 700.0, 0.12, WaveType.PULSE))
                doubleJumpTrack = createStaticTrack(generateSweep(500.0, 1100.0, 0.14, WaveType.SINE))
                crystalTrack = createStaticTrack(generateChord(doubleArrayOf(880.0, 1318.5, 1760.0), 0.18))
                powerUpTrack = createStaticTrack(generateArpeggio(doubleArrayOf(440.0, 554.3, 659.2, 880.0), 0.28))
                hazardTrack = createStaticTrack(generateSweep(180.0, 45.0, 0.22, WaveType.SAWTOOTH))
                dashTrack = createStaticTrack(generateNoiseBurst(0.12))
                levelCompleteTrack = createStaticTrack(generateArpeggio(doubleArrayOf(523.25, 659.25, 783.99, 1046.50, 1318.51), 0.45))
                clickTrack = createStaticTrack(generateTone(800.0, 0.03, WaveType.SINE))
            } catch (_: Exception) {}
        }
    }

    private fun createStaticTrack(buffer: ShortArray): AudioTrack? {
        return try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track
        } catch (_: Exception) {
            null
        }
    }

    private fun playTrack(track: AudioTrack?) {
        if (!sfxEnabled || track == null) return
        sfxExecutor.execute {
            try {
                if (track.state == AudioTrack.STATE_INITIALIZED) {
                    track.stop()
                    track.reloadStaticData()
                    track.play()
                }
            } catch (_: Exception) {}
        }
    }

    fun playJump() = playTrack(jumpTrack)
    fun playDoubleJump() = playTrack(doubleJumpTrack)
    fun playCrystal() = playTrack(crystalTrack)
    fun playPowerUp() = playTrack(powerUpTrack)
    fun playHazard() = playTrack(hazardTrack)
    fun playDash() = playTrack(dashTrack)
    fun playLevelComplete() = playTrack(levelCompleteTrack)
    fun playClick() = playTrack(clickTrack)

    fun startMusic() {
        if (!musicEnabled || musicJob?.isActive == true) return
        musicJob = scope.launch {
            try {
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufferSize.coerceAtLeast(4096) * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                musicTrack = track
                track.play()

                // Neon synth arpeggio progression (A minor 9th: A3, C4, E4, G4, B4, C5, E5, C5)
                val notes = doubleArrayOf(220.0, 261.63, 329.63, 392.00, 493.88, 523.25, 659.25, 523.25)
                var index = 0

                while (isActive && musicEnabled) {
                    val freq = notes[index % notes.size]
                    val tone = generateTone(freq, 0.16, WaveType.SINE, volume = 0.20f)
                    track.write(tone, 0, tone.size)
                    delay(175)
                    index++
                }
            } catch (_: Exception) {
            } finally {
                try {
                    musicTrack?.stop()
                    musicTrack?.release()
                    musicTrack = null
                } catch (_: Exception) {}
            }
        }
    }

    fun stopMusic() {
        musicJob?.cancel()
        musicJob = null
        try {
            musicTrack?.stop()
            musicTrack?.release()
            musicTrack = null
        } catch (_: Exception) {}
    }

    fun pause() {
        stopMusic()
    }

    fun resume() {
        if (musicEnabled) {
            startMusic()
        }
    }

    fun release() {
        stopMusic()
        sfxExecutor.execute {
            listOf(
                jumpTrack, doubleJumpTrack, crystalTrack, powerUpTrack,
                hazardTrack, dashTrack, levelCompleteTrack, clickTrack
            ).forEach {
                try {
                    it?.stop()
                    it?.release()
                } catch (_: Exception) {}
            }
        }
        sfxExecutor.shutdown()
    }

    private enum class WaveType { SINE, PULSE, SAWTOOTH }

    private fun generateTone(freq: Double, durationSec: Double, type: WaveType, volume: Float = 0.6f): ShortArray {
        val totalSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / totalSamples
            val envelope = (1.0 - progress) * volume
            val wave = when (type) {
                WaveType.SINE -> sin(2.0 * PI * freq * t)
                WaveType.PULSE -> if (sin(2.0 * PI * freq * t) > 0.0) 0.8 else -0.8
                WaveType.SAWTOOTH -> 2.0 * ((t * freq) % 1.0) - 1.0
            }
            buffer[i] = (wave * envelope * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateSweep(startFreq: Double, endFreq: Double, durationSec: Double, type: WaveType): ShortArray {
        val totalSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val progress = i.toDouble() / totalSamples
            val currentFreq = startFreq + (endFreq - startFreq) * progress
            phase += 2.0 * PI * currentFreq / sampleRate
            val envelope = (1.0 - progress) * 0.7
            val wave = when (type) {
                WaveType.SINE -> sin(phase)
                WaveType.PULSE -> if (sin(phase) > 0.0) 0.8 else -0.8
                WaveType.SAWTOOTH -> 2.0 * ((phase / (2.0 * PI)) % 1.0) - 1.0
            }
            buffer[i] = (wave * envelope * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateChord(freqs: DoubleArray, durationSec: Double): ShortArray {
        val totalSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / totalSamples
            val envelope = (1.0 - progress) * 0.55
            var waveSum = 0.0
            for (f in freqs) {
                waveSum += sin(2.0 * PI * f * t)
            }
            waveSum /= freqs.size
            buffer[i] = (waveSum * envelope * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateArpeggio(notes: DoubleArray, durationSec: Double): ShortArray {
        val totalSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(totalSamples)
        val noteLength = totalSamples / notes.size
        for (i in 0 until totalSamples) {
            val noteIndex = (i / noteLength).coerceAtMost(notes.size - 1)
            val freq = notes[noteIndex]
            val t = i.toDouble() / sampleRate
            val noteProgress = (i % noteLength).toDouble() / noteLength
            val overallProgress = i.toDouble() / totalSamples
            val envelope = (1.0 - noteProgress * 0.6) * (1.0 - overallProgress * 0.3) * 0.6
            val wave = sin(2.0 * PI * freq * t)
            buffer[i] = (wave * envelope * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateNoiseBurst(durationSec: Double): ShortArray {
        val totalSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(totalSamples)
        var lastVal = 0.0
        for (i in 0 until totalSamples) {
            val progress = i.toDouble() / totalSamples
            val envelope = (1.0 - progress) * 0.4
            val white = (Math.random() * 2.0 - 1.0)
            lastVal = (lastVal + (0.3 * white)) / 1.3
            buffer[i] = (lastVal * envelope * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }
}
