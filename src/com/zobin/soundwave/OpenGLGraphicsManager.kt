package com.zobin.soundwave

import org.lwjgl.BufferUtils
import org.lwjgl.glfw.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import kotlin.math.*

class OpenGLGraphicsManager : GraphicsManager {
    private var errorCallback: GLFWErrorCallback = GLFWErrorCallback.createPrint()
    private var window: Long = 0
    private var eventQueue: MutableList<KeyEvent> = ArrayList()
    private lateinit var fontManager: OpenGLFontManager

    override fun terminate() {
        Callbacks.glfwFreeCallbacks(window)
        GLFW.glfwTerminate()
        errorCallback.free()
    }

    override fun init() {
        GLFW.glfwSetErrorCallback(errorCallback)
        check(GLFW.glfwInit()) { "Unable to initialize GLFW" }

        window = GLFW.glfwCreateWindow(1920, 1080, "Soundwave", 0, 0)

        if (window == 0L) {
            GLFW.glfwTerminate()
            throw RuntimeException("Failed to create the GLFW window")
        }

        class KeyCallback() : GLFWKeyCallback() {
            override fun invoke(window: Long, key: Int, scancode: Int, action: Int, mods: Int) =
                keyCallbackInvoke(window, key, scancode, action, mods)
        }

        class MouseCallback() : GLFWMouseButtonCallback() {
            override fun invoke(window: Long, button: Int, action: Int, mods: Int) =
                mouseCallbackInvoke(window, button, action, mods)
        }

        GLFW.glfwSetKeyCallback(window, KeyCallback())
        GLFW.glfwSetMouseButtonCallback(window, MouseCallback())
        GLFW.glfwMakeContextCurrent(window)
        GLFW.glfwSwapInterval(1)
        GLFW.glfwShowWindow(window)
        GL.createCapabilities()

        fontManager = OpenGLFontManager()
        fontManager.init()
    }

    override fun getDelta(): Double {
        val elapsed = GLFW.glfwGetTime()
        GLFW.glfwSetTime(0.0)
        return elapsed
    }

    override fun render() {
        GLFW.glfwSwapBuffers(window)
        GLFW.glfwPollEvents()
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
    }

    override fun sync() {
        // Should be handled by VSync in LWJGL
    }

    override fun getShouldTerminate(): Boolean {
        return GLFW.glfwWindowShouldClose(window)
    }

    override fun pollEvents(): List<KeyEvent> {
        val events = ArrayList<KeyEvent>()
        events.addAll(eventQueue)
        eventQueue.clear()
        events.add(KeyEvent(KeyEvent.Key.None, getMousePos()))
        return events
    }

    override fun labelCircle(pos: Vector2D, radius: Double, string: String, color: Vector3D) {
        val p = gameToWindowPos(pos)
        val s = gameToWindowSize(Vector2D(radius, radius))
        fontManager.drawString(string, p, s.x, color)
    }

    override fun labelRectangle(pos: Vector2D, size: Vector2D, string: String, color: Vector3D) {
        val p = gameToWindowPos(pos)
        val s = gameToWindowSize(size)
        fontManager.drawString(string, p, s, color)
    }

    override fun fillCircle(pos: Vector2D, radius: Double, color: Vector3D) {
        val p = gameToWindowPos(pos)
        val r = gameToWindowSize(Vector2D(radius, radius))

        val segments = max(64.0, radius * 64.0).roundToInt()
        GL11.glBegin(GL11.GL_TRIANGLE_FAN)
        GL11.glColor3d(color.x, color.y, color.z)
        GL11.glVertex2d(p.x, p.y)
        for (i in 0..segments) {
            val angle: Double = i * 2 * PI / segments
            GL11.glVertex2d(p.x + (r.x * cos(angle)), p.y - (r.y * sin(angle)))
        }
        GL11.glEnd()
    }

    override fun outlineCircle(pos: Vector2D, innerRadius: Double, radius: Double, color: Vector3D) {
        val p = gameToWindowPos(pos)
        val ir = gameToWindowSize(Vector2D(innerRadius, innerRadius))
        val r = gameToWindowSize(Vector2D(radius, radius))

        val segments = max(64.0, radius * 64.0).roundToInt()
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP)
        GL11.glColor3d(color.x, color.y, color.z)
        for (i in 0..segments) {
            var angle: Double = i * 2 * PI / segments
            GL11.glVertex2d(p.x + (ir.x * cos(angle)), p.y - (ir.y * sin(angle)))
            angle += PI / segments
            GL11.glVertex2d(p.x + (r.x * cos(angle)), p.y - (r.y * sin(angle)))
        }
        GL11.glEnd()
    }

    private fun gameToWindowPos(pos: Vector2D): Vector2D {
        return (gameToWindowSize(pos) - Vector2D(1.0, 1.0)) * Vector2D(1.0, -1.0)
    }

    private fun gameToWindowSize(size: Vector2D): Vector2D {
        return size * 2.0 / GraphicsConstants.SIZE
    }

    private fun mouseToGamePos(pos: Vector2D): Vector2D {
        return mouseToGameSize(pos)
    }

    private fun mouseToGameSize(size: Vector2D): Vector2D {
        return size * GraphicsConstants.SIZE / Vector2D(1920.0, 1080.0);
    }

    private fun getMousePos(): Vector2D {
        val x  = BufferUtils.createDoubleBuffer(1);
        val y  = BufferUtils.createDoubleBuffer(1);
        GLFW.glfwGetCursorPos(window, x, y)
        return mouseToGamePos(Vector2D(x.get(), y.get()))
    }

    private fun keyCallbackInvoke(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {
        if (action != GLFW.GLFW_PRESS) return
        val pressed = when (key) {
            GLFW.GLFW_KEY_ESCAPE -> KeyEvent.Key.ESC
            GLFW.GLFW_KEY_A -> KeyEvent.Key.K1
            GLFW.GLFW_KEY_S -> KeyEvent.Key.K2
            GLFW.GLFW_KEY_D -> KeyEvent.Key.K3
            GLFW.GLFW_KEY_F -> KeyEvent.Key.K4
            GLFW.GLFW_KEY_J -> KeyEvent.Key.K5
            GLFW.GLFW_KEY_K -> KeyEvent.Key.K6
            GLFW.GLFW_KEY_L -> KeyEvent.Key.K7
            GLFW.GLFW_KEY_SEMICOLON -> KeyEvent.Key.K8
            else -> {
                return
            }
        }
        val event = KeyEvent(pressed, getMousePos())
        eventQueue.add(event)
    }

    private fun mouseCallbackInvoke(window: Long, button: Int, action: Int, mods: Int) {
        if (action != GLFW.GLFW_PRESS) return
        val clicked = when (button) {
            GLFW.GLFW_MOUSE_BUTTON_1 -> KeyEvent.Key.M1
            GLFW.GLFW_MOUSE_BUTTON_2 -> KeyEvent.Key.M2
            GLFW.GLFW_MOUSE_BUTTON_3 -> KeyEvent.Key.M3
            else -> {
                return
            }
        }
        val event = KeyEvent(clicked, getMousePos())
        eventQueue.add(event)
    }
}
