package com.jaluk.soundwave

class StateTransition(private val nextState: GameState): GameState {
    private var initialized = false
    private val transitionSpeed = 4.0

    override fun update(delta: Double, board: GameBoard, events: List<KeyEvent>): GameState? {
        if (!initialized) {
            initialized = true
            val center = GraphicsConstants.SIZE / 2.0
            board.addTransition(Ripple(center, transitionSpeed, 0.0, Drawable.Tune.T1))
        }

        board.update(delta)

        return if (board.isEmpty()) {
            board.reset()
            nextState
        } else {
            this
        }
    }
}