package com.jaluk.soundwave

import java.io.File

fun main(args : Array<String>) {
    val reader = WAVReader()
    reader.readFile(File(args[0]))
    val level = Level(reader)
    level.init()
    val auto = args.size > 1 && args[1] == "auto"

    val gm: GraphicsManager = JavaGraphicsManager()
    val gs = StatePlaying(level, reader, auto)
    val gb = GameBoard()
    val engine = GameEngine(gm, StateTransition(gs), gb)
    engine.Run()
}
