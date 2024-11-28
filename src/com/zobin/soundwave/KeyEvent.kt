package com.zobin.soundwave

class KeyEvent(val key: Key, val pos: Vector2D) {
    enum class Key {
        ESC, K1, K2, K3, K4, K5, K6, K7, K8, M1, M2, M3, None
    }
}