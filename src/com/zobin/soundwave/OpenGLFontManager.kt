package com.zobin.soundwave

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import kotlin.collections.HashMap
import kotlin.collections.set
import kotlin.math.min
import kotlin.math.sqrt


class OpenGLFontManager {
    val size = 256
    private lateinit var font: Font
    private var fontHeight: Int = 0
    private val glyphs = HashMap<Char, Glyph>()

    fun init() {
        font = Font(Font.SANS_SERIF, Font.PLAIN, size)

        var imageHeight = 0

        for (i in 32..255) {
            if (i == 127) {
                continue
            }
            val c = i.toChar()
            val g = initChar(c) ?: continue
            imageHeight = imageHeight.coerceAtLeast(g.h)
        }

        fontHeight = imageHeight
    }

    private fun initChar(c: Char): Glyph? {
        var image = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
        var g = image.createGraphics()

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.font = font
        val m = g.fontMetrics
        g.dispose()

        val w: Int = m.charWidth(c)
        val h: Int = m.height
        if (w == 0) return null

        image = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
        g = image.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.font = font
        g.paint = Color.WHITE
        g.drawString(c.toString(), 0, m.ascent)
        g.dispose()

        val co = image.colorModel.numComponents
        val data = IntArray(co * w * h)
        image.getRGB(0, 0, w, h, data, 0, w)

        val pixels = BufferUtils.createByteBuffer(data.size * 4)
        for (i in 0 until h) {
            for (j in 0 until w) {
                val pixel: Int = data[i * w + j]
                pixels.put((pixel shr 16 and 0xFF).toByte())
                pixels.put((pixel shr 8 and 0xFF).toByte())
                pixels.put((pixel and 0xFF).toByte())
                pixels.put((pixel shr 24 and 0xFF).toByte())
                //pixels.put((pixel and 0xFF).toByte())
            }
        }
        pixels.flip()

        val textureHandle = BufferUtils.createIntBuffer(1)
        GL11.glGenTextures(textureHandle)
        val id = textureHandle.get()
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id)
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, co, w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels)
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)

        val glyph = Glyph(id, w, h, 0, h, m.ascent)
        glyphs[c] = glyph
        return glyph
    }

    private fun getStringSize(s: String): Vector2D {
        val lines = s.count { c -> c == '\n' } + 1
        val textHeight = lines * fontHeight
        var textWidth = Int.MAX_VALUE
        for (line in s.split('\n')) {
            val width = line.map { c -> glyphs[c]!!.w }.sum()
            textWidth = textWidth.coerceAtMost(width)
        }

        return Vector2D(textWidth.toDouble(), textHeight.toDouble())
    }

    fun drawString(s: String, center: Vector2D, radius: Double, color: Vector3D) {
        val unscaled = getStringSize(s)
        val radicand = 1 / unscaled.norm2()
        val scale = 2 * radius * sqrt(radicand)
        val size = unscaled * scale
        drawString(s, center - (size * 0.5), size, color)
    }

    fun drawString(s: String, pos: Vector2D, size: Vector2D, color: Vector3D) {
        val unscaled = getStringSize(s)

        val yScale = size.y / unscaled.y
        val xScale = size.x / unscaled.x
        val scale = min(xScale, yScale).toFloat()

        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        GL11.glColor3d(color.x, color.y, color.z)

        for (line in s.split('\n')) {
            var cursor = Vector2D(pos.x, pos.y)
            for (c in line) {
                val shift = drawChar(c, cursor, scale)
                cursor += Vector2D(x=shift.toDouble())
            }
        }

        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)
    }

    private fun drawChar(c: Char, pos: Vector2D, scale: Float): Float {
        val g = glyphs[c]!!
        val drawX1 = pos.x.toFloat()
        val drawY1 = pos.y.toFloat()
        val drawX2 = drawX1 + g.w * scale
        val drawY2 = drawY1 + g.h * scale

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, g.id)

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR)

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP)

        GL11.glBegin(GL11.GL_QUADS)
        GL11.glTexCoord2f(0F, 1F); GL11.glVertex2f(drawX1, drawY1)
        GL11.glTexCoord2f(0F, 0F); GL11.glVertex2f(drawX1, drawY2)
        GL11.glTexCoord2f(1F, 0F); GL11.glVertex2f(drawX2, drawY2)
        GL11.glTexCoord2f(1F, 1F); GL11.glVertex2f(drawX2, drawY1)
        GL11.glEnd()

        return g.w * scale
    }

    private data class Glyph(val id: Int, val w: Int, val h: Int, val x: Int, val y: Int, val a: Int)
}