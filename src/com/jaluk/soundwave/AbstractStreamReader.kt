package com.jaluk.soundwave

import java.io.File
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.DataLine

abstract class AbstractStreamReader : AudioReader {
    private var sampleRate = 0.0
    private var data: DoubleArray = DoubleArray(0)
    private lateinit var clipStream: AudioInputStream
    private lateinit var clip: Clip
    private lateinit var file: File
    private var maxFrame = 0

    private fun preparePlay(file: File) {
        clipStream = streamFile(file)
        val info = DataLine.Info(Clip::class.java, clipStream.format)
        clip = AudioSystem.getLine(info) as Clip
        clip.open(clipStream)
    }

    abstract fun streamFile(file: File): AudioInputStream

    override fun readFile(file: File) {
        this.file = file
        val audioInputStream = streamFile(file)

        val bytesPerFrame = audioInputStream.format.frameSize
        val channels = audioInputStream.format.channels
        val bytesPerSample = bytesPerFrame / channels
        val bigEndian = audioInputStream.format.isBigEndian
        val numBytes = 1024 * bytesPerFrame
        val audioBytes = ByteArray(numBytes)
        var numBytesRead: Int

        val samples = ArrayList<Double>()

        while (audioInputStream.read(audioBytes).also { numBytesRead = it } != -1) {
            val framesRead = numBytesRead / bytesPerFrame
            for (i in 0 until framesRead) {
                val frameI = i * bytesPerFrame
                var sample = 0.0
                for (j in 0 until channels) {
                    val sampleBytes = audioBytes.copyOfRange(frameI + (j * bytesPerSample), frameI + ((j + 1) * bytesPerSample))
                    if (bigEndian) sampleBytes.reverse()
                    sample += when (bytesPerSample) {
                        1 -> sampleBytes[0].toUByte().toDouble()
                        2 -> (sampleBytes[0].toUByte().toDouble() + 256.0 * sampleBytes[1])
                        else -> Double.NaN
                    }
                }
                sample /= channels
                samples.add(sample)
            }
        }

        data = samples.toDoubleArray()
        sampleRate = audioInputStream.format.frameRate.toDouble()
        maxFrame = data.size

        preparePlay(file)
    }

    override fun getSampleRate(): Double {
        return sampleRate
    }

    override fun pollData(): DoubleArray {
        val out = data.clone()
        data = DoubleArray(0)
        return out
    }

    override fun maxFrame(): Int {
        return maxFrame
    }

    override fun play() {
        clip.start()
    }

    override fun stop() {
        clip.stop()
        clip.framePosition = 0
        preparePlay(file)
    }

    override fun getCurrentFrame(): Int {
        return clip.framePosition
    }
}