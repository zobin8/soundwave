package com.jaluk.soundwave

import com.meapsoft.FFT
import kotlin.math.*
import kotlin.random.Random

object LevelConstants {
    const val distErrorAllowed = 1.0
}

class Level(private val reader: AudioReader) {
    private val fftRate = 30.0
    private var realRate = 30.0
    private var samplesPerFFT = 0

    private var noteSpeed = 1.0
    private var appearSpeed = 1.0

    private val smoothValue = 1E-2

    private var waitingBubbles: List<WaitingBubble> = ArrayList()
    private var pulses: MutableList<Double> = ArrayList()

    private class Note(var frequency: Int, var amplitude: Double, var index: Int, var delta: Double)

    private class WaitingBubble(val index: Int, val bubble: Bubble)

    private fun fft(): List<Note> {
        val samplesPerFFTLog = log2(reader.getSampleRate() / fftRate).toInt()
        samplesPerFFT = (2.0).pow(samplesPerFFTLog).roundToInt()
        realRate = reader.getSampleRate() / samplesPerFFT
        val timeSmoothing = smoothValue.pow(1 / realRate)

        val fft = FFT(samplesPerFFT)
        val data = reader.getData()
        var prevBins = DoubleArray(8)
        val fftData = ArrayList<Note>()

        var averageMax = 0.0
        var overallMax = Double.NEGATIVE_INFINITY

        for (i in 0 until data.size / samplesPerFFT) {
            val real = data.copyOfRange(i * samplesPerFFT, (i + 1) * samplesPerFFT)
            val imaginary = DoubleArray(real.size)

            fft.fft(real, imaginary)

            val currBins = DoubleArray(8)
            var maxV = Double.NEGATIVE_INFINITY
            var maxI = 0
            for (j in real.indices) {
                val maxFreqIndex = data.size / 2
                val freqIndex = maxFreqIndex - abs(j - maxFreqIndex)
                val binIndex = ((freqIndex.toDouble() / (maxFreqIndex + 1.0)).pow(0.33) * currBins.size).toInt()
                val fftVal = real[j] * real[j] + imaginary[j] * imaginary[j]
                if (fftVal > currBins[binIndex]) {
                    currBins[binIndex] = fftVal
                }
                if (fftVal > maxV) {
                    maxV = fftVal
                    maxI = j
                }
            }

            averageMax += maxV
            overallMax = max(overallMax, maxV)

            var delta = 0.0
            for (j in currBins.indices) {
                delta += abs(currBins[j] - prevBins[j])
            }
            prevBins = currBins

            fftData.add(Note(maxI, maxV, i, delta))
        }

        averageMax /= fftData.size / 7.0

        val divisor = (averageMax + overallMax) / 2

        var runningPulse = 0.0
        for (n in fftData) {
            n.amplitude /= divisor
            n.delta /= divisor
            runningPulse *= timeSmoothing
            runningPulse += (1 - timeSmoothing) * log2(n.delta + 1)
            pulses.add(runningPulse)
        }

        val maxDelta = pulses.max()!!
        pulses.forEachIndexed { index, d -> pulses[index] = d / maxDelta }

        return fftData
    }

    private fun setLevelConstants(notes: List<Note>) {
        val totalDelta = notes.sumByDouble { n -> n.delta }
        val songLength = notes.size / realRate
        val avgDelta = totalDelta / songLength
        noteSpeed = 3.0 * avgDelta
        appearSpeed = avgDelta
        println(totalDelta)
        println(avgDelta)
    }

    private fun filterNotes(notes: List<Note>): List<Note> {
        val filteredNotes = ArrayList<Note>()
        var lastTime = 0.0
        val runningNotes = ArrayList<Note>()
        var runningDelta = 0.0
        for (n in notes) {//.filter { n -> n.amplitude > 0.1 }) {
            val time = n.index / realRate
            val timeDiff = time - lastTime

            val threshold = min(0.1, 0.2 / timeDiff)
            if (n.amplitude < threshold) continue

            runningDelta += n.delta
            if (timeDiff < 0.25 || timeDiff < 1.0 / (filteredNotes.size + 1)) continue

            runningNotes.add(n)
            val cutoff = min(0.5, 1 / timeDiff)
            if (runningDelta < cutoff && timeDiff < 5.0) continue

            val loudest = runningNotes.maxBy { x -> x.amplitude }!!
            val loudestTime = loudest.index / realRate
            lastTime = loudestTime
            filteredNotes.add(loudest)
            runningDelta = 0.0
            runningNotes.clear()
        }

        println(filteredNotes.size)

        return filteredNotes
    }

    private fun applyColors(notes: List<Note>) {
        val count = ArrayList<Int>()
        for (n in notes) {
            while (count.size <= n.frequency) {
                count.add(0)
            }
            count[n.frequency]++
        }

        val cutoffs = ArrayList<Int>()
        var c = 0
        for (i in count.indices) {
            c += count[i]
            if (c >= cutoffs.size * notes.size / 8) {
                cutoffs.add(i)
            }
        }

        for (n in notes) {
            var newFreq = 0
            for (i in cutoffs.indices) {
                if (n.frequency <= cutoffs[i]) break
                newFreq = i
            }
            n.frequency = newFreq
        }
    }

    private fun randDir(r: Random): Vector2D {
        val a = r.nextDouble(0.0, 2.0 * PI)
        return Vector2D(cos(a), sin(a))
    }

    private fun makeBubbles(notes: List<Note>): List<WaitingBubble> {
        val bubbles = ArrayList<WaitingBubble>()

        val tunes = arrayOf(Drawable.Tune.T1, Drawable.Tune.T2, Drawable.Tune.T3, Drawable.Tune.T4, Drawable.Tune.T5, Drawable.Tune.T6, Drawable.Tune.T7, Drawable.Tune.T8)

        val r = Random(notes.size * appearSpeed.toInt())

        var lastPos = GraphicsConstants.SIZE / 2.0
        var lastTime = -100.0
        var lastAppearDir = randDir(r)
        for (n in notes) {
            val popTime = n.index / realRate
            var popPos = Vector2D(r.nextDouble(1.0, GraphicsConstants.SIZE.x - 1),
                r.nextDouble(1.0, GraphicsConstants.SIZE.y - 1))

            val posDiff = (popPos - lastPos).norm() + LevelConstants.distErrorAllowed
            val rippleTime = posDiff / noteSpeed
            val actualTime = popTime - lastTime
            val timeScale = rippleTime / (actualTime)
            if (timeScale > 1) {
                val diff = popPos - lastPos
                popPos = lastPos + diff / timeScale
            }

            val scale = (popTime - lastTime) * appearSpeed
            lastAppearDir = (lastAppearDir + (randDir(r) * scale)).normalized()
            val appearPos = (GraphicsConstants.SIZE / 2.0) + lastAppearDir * (GraphicsConstants.SIZE.norm() + 1.0)
            val velDir = (popPos - appearPos).normalized()
            val vel = velDir * noteSpeed
            val appearTime = popTime - (appearPos - popPos).norm() / noteSpeed

            val b = Bubble(appearPos, vel, 1.0, tunes[n.frequency])
            b.popPos = popPos
            val appearIndex = (appearTime * realRate).toInt()

            bubbles.add(WaitingBubble(appearIndex, b))

            lastPos = popPos
            lastTime = popTime
        }

        return bubbles
    }

    fun init() {
        var notes = fft()
        setLevelConstants(notes)
        notes = filterNotes(notes)
        applyColors(notes)
        waitingBubbles = makeBubbles(notes)
    }

    fun getPulseAmount(frame: Int): Double {
        val index = frame / samplesPerFFT
        if (index < 0 || index >= pulses.size) return 0.0
        return pulses[index]
    }

    fun getDataBetween(startFrame: Int, endFrame: Int): List<Bubble> {
        val startIndex = startFrame / samplesPerFFT
        val endIndex = endFrame / samplesPerFFT

        return waitingBubbles.filter { b -> b.index in startIndex until endIndex }.map { b -> b.bubble.copy() }
    }

    fun firstFrame(): Int {
        return waitingBubbles.minBy { b -> b.index }!!.index
    }

    fun getNoteCount(): Int {
        return waitingBubbles.size
    }
}