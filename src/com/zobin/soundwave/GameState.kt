package com.zobin.soundwave

interface GameState {
    fun update(delta: Double, board: GameBoard, events: List<KeyEvent>): GameState?
}