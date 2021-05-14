package com.jaluk.soundwave

import java.io.File

interface AudioReader {
    fun readFile(file: File)
    fun getSampleRate(): Double
    fun getData(): DoubleArray
    fun play()
    fun stop()
    fun getCurrentFrame(): Int
}