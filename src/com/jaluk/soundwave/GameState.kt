package com.jaluk.soundwave

interface GameState {
    fun update(delta: Double, board: GameBoard, events: List<KeyEvent>): GameState?
}