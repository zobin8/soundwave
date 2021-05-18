package com.jaluk.soundwave

import java.io.File

fun main(args : Array<String>) {
    val reader = constructAudioReader(args[0])!!
    reader.readFile(File(args[0]))
    val level = Level(reader)
    level.init()
    var difficulty = PlayDifficulty.Normal
    if (args.size > 1) {
        difficulty = when (args[1]) {
            "auto" -> PlayDifficulty.Auto
            "zen" -> PlayDifficulty.Zen
            "hardcore" -> PlayDifficulty.Hardcore
            else -> PlayDifficulty.Normal
        }
    }

    val gm: GraphicsManager = OpenGLGraphicsManager()
    val gs = StatePlaying(level, reader, difficulty)
    val gb = GameBoard()
    val engine = GameEngine(gm, StateTransition(gs), gb)
    engine.Run()
}
