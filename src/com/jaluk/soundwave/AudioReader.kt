package com.jaluk.soundwave

import java.io.File

interface AudioReader {
    fun supportedExtensions(): List<String>
    fun readFile(file: File)
    fun getSampleRate(): Double
    fun getData(): DoubleArray
    fun play()
    fun stop()
    fun getCurrentFrame(): Int
}

fun constructAudioReader(name: String): AudioReader? {
    val readers = arrayOf(MP3Reader(), WAVReader())
    val extension = name.split(".").last()
    return readers.find { r -> r.supportedExtensions().contains(extension) }
}