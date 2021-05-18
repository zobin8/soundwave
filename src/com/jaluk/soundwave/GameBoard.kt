package com.jaluk.soundwave

import kotlin.math.abs

class GameBoard {
    private var bubbles: MutableList<Bubble> = ArrayList()
    private var ripples: MutableList<Ripple> = ArrayList()
    private var transition: Ripple? = null
    private var missed = 0
    var label = ""

    fun update(delta: Double) {
        for (b in bubbles) {
            b.update(delta)
        }

        for (r in ripples) {
            r.update(delta)
        }
        transition?.update(delta)

        ripples.removeIf { x -> x.isDead() }
        killMissedBubbles()
        if (transition?.isDead() == true)
        {
            transition = null
            bubbles.clear()
        }

        if (hasCollision()) {
            missed++
            clear()
        }
    }

    fun setPulseProperties(pulseAmount: Double? = null, pulseFactor: Double? = null) {
        for (b in bubbles) {
            if (pulseAmount != null) b.pulseAmount = pulseAmount
            if (pulseFactor != null) b.pulseFactor = pulseFactor
        }
    }

    fun autoPop() {
        val popList = ArrayList<Bubble>()
        for (b in bubbles) {
            if (b.autoPop()) popList.add(b)
        }

        for (b in popList) {
            val r = b.pop()
            r.pos = b.popPos!!
            ripples.add(r)
            bubbles.remove(b)
        }
    }

    private fun popBubble(b: Bubble) {
        bubbles.remove(b)
        val newRipple = b.pop()
        val visualRadius = b.visualRadius()
        if (!hasCollision()) {
            ripples.add(newRipple)
            val collision = ripples.firstOrNull { r -> (r.pos - newRipple.pos).norm() > r.radius } ?: return
            val diff = collision.pos - b.pos
            if (diff.norm() < visualRadius) {
                newRipple.pos = collision.pos
            } else if (diff.norm() - collision.radius <= visualRadius) {
                val offset = (visualRadius * 0.1) + (diff.norm() - collision.radius)
                newRipple.pos += diff.normalized() * offset
            }
        }
    }

    fun popAt(pos: Vector2D, tune: Drawable.Tune?, requireTouch: Boolean = false): Boolean {
        val b = bubbles.minBy { b -> b.poppableBy(pos, tune) }
        if (b != null) {
            if (requireTouch && (pos - b.pos).norm() > b.radius) return false
            if (requireTouch && ripples.any { r -> (pos - r.pos).norm() > r.radius }) return false
            popBubble(b)
            return true
        }
        return false
    }

    fun clear() {
        ripples.clear()
    }

    fun reset() {
        ripples.clear()
        bubbles.clear()
        missed = 0
        transition = null
        label = ""
    }

    fun addBubble(b: Bubble) {
        bubbles.add(b)
    }

    fun addTransition(r: Ripple) {
        r.isTransition = true
        transition = r
        clear()
    }

    fun isEmpty(): Boolean {
        return ripples.isEmpty() && bubbles.isEmpty() && transition == null
    }

    fun hasCollision(): Boolean {
        for (i in 0 until ripples.size) {
            if (collidesWith(i) != null) return true
        }
        return false
    }

    private fun collidesWith(i: Int): Ripple? {
        val r1 = ripples[i]
        for (j in i - 1 downTo 0) {
            val r2 = ripples[j]
            val dist = (r1.pos - r2.pos).norm()
            val allowedDist = r1.radius + r2.radius
            val diffRadius = abs(r1.radius - r2.radius)
            if (dist <= allowedDist && dist > diffRadius) return r2
        }

        return null
    }

    private fun killMissedBubbles() {
        val toRemove = ArrayList<Bubble>()
        for (b in bubbles) {
            val min = Vector2D(-b.radius, -b.radius)
            val max = GraphicsConstants.SIZE + Vector2D(b.radius, b.radius)
            var outOfBounds = b.pos.x < min.x || b.pos.y < min.y || b.pos.x > max.x || b.pos.y > max.y
            val t = transition
            if (t != null) {
                outOfBounds = outOfBounds || (t.pos - b.pos).norm() + b.radius < t.radius
            }
            if (outOfBounds) {
                val toCenter = (GraphicsConstants.SIZE / 2.0) - b.pos
                if (b.vel.dot(toCenter) < 0) {
                    toRemove.add(b)
                    missed++
                }
            }
        }
        bubbles.removeAll(toRemove)
    }

    fun getMissed(): Int {
        return missed
    }

    fun render(gm: GraphicsManager) {
        for (r in ripples) {
            r.render(gm)
        }
        for (b in bubbles) {
            b.render(gm)
        }

        gm.labelRectangle(Vector2D(0.0, GraphicsConstants.SIZE.y), Vector2D(GraphicsConstants.SIZE.x, 0.5),
            label, Vector3D(1.0, 1.0,1.0))

        transition?.render(gm)
    }
}