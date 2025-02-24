package com.lmar.tictactoe.feature.sounds

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.lmar.tictactoe.R

class SoundEffectPlayer(context: Context) {
    private val soundPool: SoundPool
    private val winSound: Int
    private val drawSound: Int
    private val loseSound: Int
    private val clickSound: Int

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(audioAttributes)
            .build()

        winSound = soundPool.load(context, R.raw.winner_sound, 1)
        drawSound = soundPool.load(context, R.raw.draw_sound, 1)
        loseSound = soundPool.load(context, R.raw.loser_sound, 1)
        clickSound = soundPool.load(context, R.raw.screentap_sound, 1)
    }

    fun playWin() {
        soundPool.play(winSound, 1f, 1f, 1, 0, 1f)
    }

    fun playDraw() {
        soundPool.play(drawSound, 1f, 1f, 1, 0, 1f)
    }

    fun playLose() {
        soundPool.play(loseSound, 1f, 1f, 1, 0, 1f)
    }

    fun playClick() {
        soundPool.play(clickSound, 1f, 1f, 1, 0, 1f)
    }
}