package com.jaluk.soundwave

object GraphicsConstants {
    val SIZE = Vector2D(16.0, 9.0)
}

interface GraphicsManager {
    fun init()
    fun getDelta(): Double
    fun render()
    fun sync()
    fun getShouldTerminate(): Boolean
    fun terminate()
    fun pollEvents(): List<KeyEvent>

    fun fillCircle(pos: Vector2D, radius: Double, color: Vector3D)
    fun outlineCircle(pos: Vector2D, innerRadius: Double, radius: Double, color: Vector3D)
}
