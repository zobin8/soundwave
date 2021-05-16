package com.jaluk.soundwave

import java.io.File
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem


class MP3Reader : AbstractStreamReader() {
    override fun streamFile(file: File): AudioInputStream {
        val mp3stream = AudioSystem.getAudioInputStream(file)
        val baseFormat = mp3stream.format
        val decodedFormat = AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            baseFormat.sampleRate,
            16,
            baseFormat.channels,
            baseFormat.channels * 2,
            baseFormat.sampleRate,
            false
        )
        return AudioSystem.getAudioInputStream(decodedFormat, mp3stream)
    }

    override fun supportedExtensions(): List<String> {
        return arrayListOf("mp3")
    }
}