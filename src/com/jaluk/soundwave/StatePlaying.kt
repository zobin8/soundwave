package com.jaluk.soundwave

import kotlin.math.max
import kotlin.math.min

class StatePlaying(private val level: Level, private val audio: AudioReader, private val auto: Boolean): GameState {
    var prePlayFrames = level.firstFrame() - 3 * audio.getSampleRate()
    var lastTime = Int.MIN_VALUE
    var playing = false
    var lastMissed = 0
    var failureQuota = 0.05

    var poppedSinceLastMiss = 100
    var popped = 1

    private fun updatePulseFactor(board: GameBoard) {
        var pulseFactor = 0.0
        val allowMissed = failureQuota * level.getNoteCount()
        if (allowMissed < lastMissed + 1) {
            pulseFactor = 1.0
        } else if (poppedSinceLastMiss < 1 / failureQuota) {
            pulseFactor = min(1.0, lastMissed.toDouble() / popped / failureQuota)
        }
        board.setPulseProperties(pulseFactor=pulseFactor)
    }

    override fun update(delta: Double, board: GameBoard, events: List<KeyEvent>): GameState? {
        var newPopped = 0

        for (e in events) {
            when (e.key) {
                KeyEvent.Key.ESC -> {
                    println(lastMissed)
                    return null
                }
                KeyEvent.Key.None -> {
                    //val pop = board.popAt(e.pos, null, true)
                    //if (pop) newPopped++
                }
                else -> {
                    val pop = board.popAt(e.pos, null)
                    if (pop) newPopped++
                }
            }
        }

        if (newPopped > 0) {
            poppedSinceLastMiss += newPopped
            popped += newPopped
            updatePulseFactor(board)
        }

        if (!playing) {
            prePlayFrames += delta * audio.getSampleRate()
            if (prePlayFrames >= 0.0) {
                audio.play()
                playing = true
                updatePulseFactor(board)
            }
        }

        val frame = if (playing) {
            audio.getCurrentFrame()
        } else {
            prePlayFrames.toInt()
        }

        val newBubbles = level.getDataBetween(lastTime, frame)
        lastTime = frame
        for (b in newBubbles) {
            board.addBubble(b)
        }

        board.update(delta)
        if (auto) board.autoPop()

        val pulseAmount = level.getPulseAmount(frame)
        board.setPulseProperties(pulseAmount=pulseAmount)

        if (board.getMissed() > lastMissed) {
            lastMissed = board.getMissed()
            poppedSinceLastMiss = 0
            updatePulseFactor(board)
        }

        if (failureQuota * level.getNoteCount() < lastMissed) {
            val nextState = StatePlaying(level, audio, auto)
            audio.stop()
            return StateTransition(nextState)
        }

        if (frame >= audio.getData().size) {
            return null
        }

        return this
    }
}