package com.zobin.soundwave

import kotlin.math.sqrt

class Vector2D(val x: Double = 0.0, val y: Double = 0.0) {
    operator fun unaryMinus(): Vector2D {
        return Vector2D(-x, -y)
    }

    operator fun plus(v: Vector2D): Vector2D {
        return Vector2D(x + v.x, y + v.y)
    }

    operator fun minus(v: Vector2D): Vector2D {
        return Vector2D(x - v.x, y - v.y)
    }

    operator fun times(v: Vector2D): Vector2D {
        return Vector2D(x * v.x, y * v.y)
    }

    operator fun div(v: Vector2D): Vector2D {
        return Vector2D(x / v.x, y / v.y)
    }

    operator fun times(d: Double): Vector2D {
        return Vector2D(x * d, y * d)
    }

    operator fun div(d: Double): Vector2D {
        return Vector2D(x / d, y / d)
    }

    fun dot(v: Vector2D): Double {
        return x * v.x + y * v.y
    }

    fun normalized(): Vector2D {
        return this / norm()
    }

    fun norm2(): Double {
        return x * x + y * y;
    }

    fun norm(): Double {
        return sqrt(norm2())
    }

    fun copy(): Vector2D {
        return Vector2D(x, y)
    }
}