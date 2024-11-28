package com.zobin.soundwave

import java.io.File

interface AudioReader {
    fun supportedExtensions(): List<String>
    fun readFile(file: File)
    fun getSampleRate(): Double
    fun pollData(): DoubleArray
    fun play()
    fun stop()
    fun getCurrentFrame(): Int
    fun maxFrame(): Int
}

fun constructAudioReader(name: String): AudioReader? {
    val readers = arrayOf(MP3Reader(), WAVReader())
    val extension = name.split(".").last()
    return readers.find { r -> r.supportedExtensions().contains(extension) }
}