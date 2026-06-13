package com.example.audio

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.sin

class SynthEngine {

    private val sampleRate = 44100
    private var audioTrack: AudioTrack? = null
    private var playJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    // System state
    private var isPlaying = false
    private var currentStyle = "CHILL_AMBIENT"
    private var baseFreq = 130f
    private var userSpeedMultiplier = 1.0f

    // Live wave visualizer data (16 bands)
    private val _visualizerFlow = MutableStateFlow(FloatArray(16) { 0.1f })
    val visualizerFlow: StateFlow<FloatArray> = _visualizerFlow

    // Track position state in ms
    private val _playbackProgressMs = MutableStateFlow(0L)
    val playbackProgressMs: StateFlow<Long> = _playbackProgressMs

    private var durationMs = 180000L

    @Synchronized
    fun start(style: String, frequency: Float, duration: Long) {
        stop()
        currentStyle = style
        baseFreq = frequency
        durationMs = duration
        isPlaying = true
        _playbackProgressMs.value = 0L

        // Initialize Audio Track
        val minBufSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        try {
            audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufSize.coerceAtLeast(8192),
                AudioTrack.MODE_STREAM
            )
            audioTrack?.play()
        } catch (e: Exception) {
            Log.e("SynthEngine", "Error building or playing AudioTrack: ${e.message}")
            return
        }

        playJob = scope.launch {
            audioGeneratorLoop()
        }
    }

    @Synchronized
    fun stop() {
        isPlaying = false
        playJob?.cancel()
        playJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            // Safe ignore
        }
        audioTrack = null
        _visualizerFlow.value = FloatArray(16) { 0.0f }
    }

    fun setSpeed(speed: Float) {
        userSpeedMultiplier = speed.coerceIn(0.5f, 2.0f)
    }

    private suspend fun audioGeneratorLoop() {
        val bufferSize = 1024 // stereo buffer size (512 samples per channel)
        val shortBuffer = ShortArray(bufferSize)
        var phase = 0.0
        var phase2 = 0.0
        var phase3 = 0.0
        var phase4 = 0.0
        
        var ticker = 0L
        val random = Random()
        
        // Tracking playback time simulated
        var elapsedMs = 0L

        while (isPlaying) {
            val track = audioTrack
            if (track == null || track.playState != AudioTrack.PLAYSTATE_PLAYING) {
                break
            }

            val speed = userSpeedMultiplier
            // Fill audio buffer based on current synth style
            for (i in 0 until (bufferSize / 2)) {
                val t = ticker + i
                val sec = t.toDouble() / sampleRate

                var leftSample = 0.0
                var rightSample = 0.0

                when (currentStyle) {
                    "DEEP_SPACE" -> {
                        // Deep space drone: low frequency carrier with sweeping minor chords (7th chord)
                        // LFO for sweep
                        val lfo = sin(2.0 * Math.PI * 0.05 * sec) * 0.15 + 0.85
                        val f = baseFreq * speed

                        // Deep spatial harmonics
                        leftSample += sin(phase) * 0.4 * lfo
                        rightSample += sin(phase * 1.01) * 0.4 * lfo

                        // Minor third
                        leftSample += sin(phase * 1.2) * 0.25 * (1.0 - lfo * 0.3)
                        rightSample += sin(phase * 1.205) * 0.25 * lfo

                        // Perfect fifth
                        leftSample += sin(phase2) * 0.2 * lfo
                        rightSample += sin(phase2 * 0.99) * 0.2 * lfo

                        // Major/Minor seventh
                        leftSample += sin(phase3) * 0.15

                        // Advance phases
                        phase += 2.0 * Math.PI * f / sampleRate
                        phase2 += 2.0 * Math.PI * (f * 1.5f) / sampleRate
                        phase3 += 2.0 * Math.PI * (f * 1.8f) / sampleRate
                    }
                    "RETRO_WAVE" -> {
                        // Retro wave: fast synth pulse + synthetic driving drum kick
                        val tempoBpm = 110.0 * speed
                        val samplesPerBeat = sampleRate * (60.0 / tempoBpm)
                        val beatProgress = (t % samplesPerBeat) / samplesPerBeat

                        // Arpeggiator note selector based on beat
                        val notes = doubleArrayOf(1.0, 1.2, 1.5, 1.2, 1.8, 1.5, 2.0, 1.5)
                        val step = ((t / (samplesPerBeat / 4)) % notes.size).toInt()
                        val currentNoteFreq = baseFreq * notes[step] * speed

                        // Square wave arpeggio (slightly lowpassed by averaging)
                        val rawSquare = if (sin(phase) > 0) 0.18 else -0.18
                        leftSample += rawSquare
                        rightSample += if (sin(phase * 1.002) > 0) 0.18 else -0.18

                        // Driving Synth Kick (percussive sine sweep)
                        val kickEnvelope = Math.exp(-9.0 * beatProgress)
                        val kickFreq = 120.0 * kickEnvelope + 45.0
                        val kickSine = sin(phase2) * 0.45 * kickEnvelope
                        leftSample += kickSine
                        rightSample += kickSine

                        // Phase advances
                        phase += 2.0 * Math.PI * currentNoteFreq / sampleRate
                        phase2 += 2.0 * Math.PI * kickFreq / sampleRate
                    }
                    "DOCK_RAIN" -> {
                        // Soothing brown/white noise for rain
                        val rawNoise = (random.nextFloat() * 2.0 - 1.0) * 0.15
                        // Simulating soft rain by 1-pole low pass filter
                        leftSample += rawNoise
                        rightSample += rawNoise

                        // Dreamy random bells playing soft notes (pentatonic scale)
                        // Trigger a soft bell every 2 seconds randomly
                        val triggerSample = sampleRate * 2.5 / speed
                        val bellTime = t % triggerSample
                        val bellProgress = bellTime / triggerSample
                        if (bellProgress < 0.25) {
                            val bellEnvelope = Math.exp(-5.0 * bellProgress * 4.0)
                            // Bell scale steps
                            val scale = doubleArrayOf(1.0, 1.125, 1.25, 1.5, 1.667, 2.0)
                            val noteIdx = (((t / triggerSample).toInt()) % scale.size)
                            val bellFreq = baseFreq * 3.0f * scale[noteIdx] * speed // higher register for bells
                            val bellSine = sin(phase) * 0.25 * bellEnvelope
                            leftSample += bellSine
                            rightSample += sin(phase * 1.005) * 0.25 * bellEnvelope
                        }

                        // Phase advances
                        val scale = doubleArrayOf(1.0, 1.125, 1.25, 1.5, 1.667, 2.0)
                        val noteIdx = (((t / (sampleRate * 2.5)).toInt()) % scale.size)
                        val bellFreq = baseFreq * 3.0f * scale[noteIdx] * speed
                        phase += 2.0 * Math.PI * bellFreq / sampleRate
                    }
                    "SUNSET_GLOW" -> {
                        // Lush warm sunset chord pad: multiple detuned triangle waves
                        val f = baseFreq * speed
                        val lfo = sin(2.0 * Math.PI * 0.08 * sec) * 0.05 + 0.95

                        // 3 Voice detuned synthesizer
                        val voice1 = Math.abs((t * f / sampleRate % 1.0) - 0.5) * 4.0 - 1.0
                        val voice2 = Math.abs((t * (f * 1.006) / sampleRate % 1.0) - 0.5) * 4.0 - 1.0
                        val voice3 = Math.abs((t * (f * 1.5) / sampleRate % 1.0) - 0.5) * 4.0 - 1.0

                        val mixedVal = (voice1 * 0.22 + voice2 * 0.22 + voice3 * 0.15) * lfo
                        leftSample += mixedVal
                        rightSample += mixedVal
                    }
                    else -> { // "CHILL_AMBIENT" - binaural relaxation beats
                        // Binaural: Left carrier is base frequency, right is offset by 6Hz
                        val fLeft = baseFreq * speed
                        val fRight = (baseFreq + 6f) * speed

                        // Super smooth warm sine swell
                        leftSample += sin(phase) * 0.35
                        rightSample += sin(phase2) * 0.35

                        // Add a slow-moving background ambient wash
                        leftSample += sin(phase3) * 0.15
                        rightSample += sin(phase3) * 0.15

                        // Phase advances
                        phase += 2.0 * Math.PI * fLeft / sampleRate
                        phase2 += 2.0 * Math.PI * fRight / sampleRate
                        phase3 += 2.0 * Math.PI * (fLeft * 0.5) / sampleRate
                    }
                }

                // Compress and soft limit samples
                leftSample = leftSample.coerceIn(-1.0, 1.0)
                rightSample = rightSample.coerceIn(-1.0, 1.0)

                // Fill final SHORT buffer (convert to 16bit PCM)
                shortBuffer[i * 2] = (leftSample * 32767).toInt().toShort()
                shortBuffer[i * 2 + 1] = (rightSample * 32767).toInt().toShort()
            }

            ticker += bufferSize / 2

            // Write chunk to physical audio output
            track.write(shortBuffer, 0, bufferSize)

            // Calculate progress in ms
            elapsedMs += ((bufferSize / 2) * 1000L) / sampleRate
            _playbackProgressMs.value = elapsedMs.coerceAtMost(durationMs)
            if (elapsedMs >= durationMs) {
                // Track finished! Loop logic handled by ViewModel, or let's notify.
                elapsedMs = 0L
                _playbackProgressMs.value = 0L
            }

            // Expose updated data to live visualizer (using some wave variance)
            val visData = FloatArray(16)
            for (v in 0 until 16) {
                // Grab some samples from buffer and map to [0,1] range for visualization
                val idx = (v * (bufferSize / 32)) % (bufferSize - 1)
                val sampleVal = Math.abs(shortBuffer[idx].toFloat() / 32768f)
                visData[v] = (sampleVal * 1.2f).coerceIn(0.05f, 1.0f)
            }
            _visualizerFlow.value = visData
        }
    }
}
