package com.jaluk.soundwave

import kotlin.math.max

class Ripple(var pos: Vector2D, val speed: Double, var radius: Double, val tune: Drawable.Tune): Drawable {
    private var dead = false
    var isTransition = false

    fun isDead(): Boolean {
        return dead
    }

    override fun update(delta: Double) {
        radius += speed * delta
        val corners = arrayOf(Vector2D(), Vector2D(0.0, GraphicsConstants.SIZE.y),
            Vector2D(GraphicsConstants.SIZE.x, 0.0), GraphicsConstants.SIZE)
        val furthestCorner = corners.maxBy { c -> (c - pos).norm2() }!!
        dead = (furthestCorner - pos).norm() < radius
    }

    override fun render(gm: GraphicsManager) {
        var col = color(tune)
        if (isTransition) {
            gm.fillCircle(pos, radius, Vector3D())
            col = Vector3D(1.0, 1.0, 1.0)
        }
        gm.outlineCircle(pos, max(0.0, radius - 0.2), radius, col)
    }
}