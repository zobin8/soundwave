package com.zobin.soundwave

import java.io.File
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

class WAVReader: AbstractStreamReader() {
    override fun streamFile(file: File): AudioInputStream {
        return AudioSystem.getAudioInputStream(file)
    }

    override fun supportedExtensions(): List<String> {
        return arrayListOf("wav")
    }
}
