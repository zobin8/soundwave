package com.zobin.soundwave

class GameEngine(private val graphicsManager: GraphicsManager, private var gameState: GameState, private var gameBoard: GameBoard) {
    private var shouldTerminate: Boolean = false

    fun Run() {
        graphicsManager.init()

        while (!shouldTerminate) {
            val delta = graphicsManager.getDelta()
            val events = graphicsManager.pollEvents()
            val nextState = gameState.update(delta, gameBoard, events)
            gameBoard.render(graphicsManager)
            graphicsManager.render()
            shouldTerminate = shouldTerminate || graphicsManager.getShouldTerminate() || nextState == null
            if (nextState != null) {
                gameState = nextState
            }
        }

        graphicsManager.terminate()
    }
}