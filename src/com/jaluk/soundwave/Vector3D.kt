package com.jaluk.soundwave

import kotlin.math.sqrt

class Vector3D(val x: Double = 0.0, val y: Double = 0.0, val z: Double = 0.0) {
    operator fun unaryMinus(): Vector3D {
        return Vector3D(-x, -y, -z)
    }

    operator fun plus(v: Vector3D): Vector3D {
        return Vector3D(x + v.x, y + v.y, z + v.z)
    }

    operator fun minus(v: Vector3D): Vector3D {
        return Vector3D(x - v.x, y - v.y, z - v.z)
    }

    operator fun times(v: Vector3D): Vector3D {
        return Vector3D(x * v.x, y * v.y, z * v.z)
    }

    operator fun div(v: Vector3D): Vector3D {
        return Vector3D(x / v.x, y / v.y, z / v.z)
    }

    operator fun times(d: Double): Vector3D {
        return Vector3D(x * d, y * d, z * d)
    }

    operator fun div(d: Double): Vector3D {
        return Vector3D(x / d, y / d, z / d)
    }

    fun normalized(): Vector3D {
        return this / norm()
    }

    fun norm2(): Double {
        return x * x + y * y + z * z;
    }

    fun norm(): Double {
        return sqrt(norm2())
    }
}