package com.zobin.soundwave

interface Drawable {
    enum class Tune {
        T1,
        T2,
        T3,
        T4,
        T5,
        T6,
        T7,
        T8
    }

    fun color(tune: Tune): Vector3D {
        return when (tune) {
            Tune.T1 -> Vector3D(0.5, 0.0, 1.0)
            Tune.T2 -> Vector3D(0.0, 0.0, 1.0)
            Tune.T3 -> Vector3D(0.0, 1.0, 1.0)
            Tune.T4 -> Vector3D(0.0, 1.0, 0.0)
            Tune.T5 -> Vector3D(1.0, 1.0, 0.0)
            Tune.T6 -> Vector3D(1.0, 0.5, 0.0)
            Tune.T7 -> Vector3D(1.0, 0.0, 0.0)
            Tune.T8 -> Vector3D(1.0, 0.0, 1.0)
        }
    }

    fun update(delta: Double)
    fun render(gm: GraphicsManager)
}