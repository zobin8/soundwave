package com.zobin.soundwave

import kotlin.math.ceil
import kotlin.math.min

enum class PlayDifficulty {
    Auto,
    Zen,
    Normal,
    Hardcore
}

class StatePlaying(private val level: Level, private val audio: AudioReader, private val difficulty: PlayDifficulty): GameState {
    private var prePlayFrames = level.firstFrame() - 3 * audio.getSampleRate()
    private var lastTime = Int.MIN_VALUE
    private var playing = false
    private var lastMissed = 0
    private val failureQuota = when (difficulty) {
        PlayDifficulty.Auto -> 1.0
        PlayDifficulty.Zen -> 1.0
        PlayDifficulty.Normal -> 0.02
        PlayDifficulty.Hardcore -> 0.0
    }

    private var poppedSinceLastMiss = 100
    private var popped = 1

    private fun updatePulseFactor(board: GameBoard) {
        var pulseFactor = 0.0
        val allowMissed = ceil(failureQuota * level.getNoteCount())
        if (allowMissed < lastMissed + 1) {
            pulseFactor = 1.0
        } else if (poppedSinceLastMiss < 1 / failureQuota) {
            pulseFactor = min(1.0, lastMissed.toDouble() / popped / failureQuota)
        }
        board.setPulseProperties(pulseFactor=pulseFactor)

        board.label = when (difficulty) {
            PlayDifficulty.Auto -> "Auto"
            PlayDifficulty.Zen -> lastMissed.toString()
            PlayDifficulty.Normal -> (allowMissed - lastMissed).toInt().toString()
            PlayDifficulty.Hardcore -> ""
        }
    }

    override fun update(delta: Double, board: GameBoard, events: List<KeyEvent>): GameState? {
        var newPopped = 0

        for (e in events) {
            when (e.key) {
                KeyEvent.Key.ESC -> {
                    return null
                }
                KeyEvent.Key.None -> {
                    //val pop = board.popAt(e.pos, null, true)
                    //if (pop) newPopped++
                }
                else -> {
                    if (difficulty != PlayDifficulty.Auto) {
                        val pop = board.popAt(e.pos, null)
                        if (pop) newPopped++
                    }
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
        if (difficulty == PlayDifficulty.Auto) board.autoPop()

        val pulseAmount = level.getPulseAmount(frame)
        board.setPulseProperties(pulseAmount=pulseAmount)

        if (board.getMissed() > lastMissed) {
            lastMissed = board.getMissed()
            poppedSinceLastMiss = 0
            updatePulseFactor(board)
        }

        if (ceil(failureQuota * level.getNoteCount()) < lastMissed) {
            val nextState = StatePlaying(level, audio, difficulty)
            audio.stop()
            board.label = ""
            return StateTransition(nextState)
        }


        if (frame >= audio.maxFrame()) {
            return null
        }

        return this
    }
}