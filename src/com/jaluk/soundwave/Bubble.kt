package com.jaluk.soundwave

import kotlin.math.pow

class Bubble(var pos: Vector2D, val vel: Vector2D, val radius: Double, val tune: Drawable.Tune): Drawable {
    var popPos: Vector2D? = null
    var pulseFactor = 0.0
    var pulseAmount = 0.0

    override fun update(delta: Double) {
        pos += vel * delta
    }

    override fun render(gm: GraphicsManager) {
        val distToPop = popPos?.minus(pos)?.norm()
        val maxDist = 2 * GraphicsConstants.SIZE.norm()
        val popShade = if (distToPop != null) {
            ((maxDist - distToPop) / maxDist).pow(4)
        } else {
            1.0
        }

        val pulseRadius = radius + pulseAmount * pulseFactor

        val color = color(tune) * popShade
        gm.fillCircle(pos, pulseRadius, color)
    }

    fun autoPop(): Boolean {
        val diff = popPos?.minus(pos)
        val dot = diff?.dot(vel)
        if (dot != null && dot <= 0) {
            return true
        }
        return false
    }

    fun poppableBy(popPos: Vector2D, popTune: Drawable.Tune?): Double {
        if (popTune != null && popTune != tune) return Double.MAX_VALUE
        return (popPos - pos).norm2()
    }

    fun pop(): Ripple {
        return Ripple(pos, vel, 0.0, tune)
    }

    fun copy(): Bubble {
        val b = Bubble(pos.copy(), vel.copy(), radius, tune)
        b.popPos = popPos?.copy()
        return b
    }
}