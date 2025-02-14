package com.lmar.tictactoe.ui.screen.game

import com.lmar.tictactoe.core.enums.PlayerEnum

sealed class GameState {
    data object Start : GameState()
    data class Playing(
        val board: List<List<String>>,
        val currentPlayer: PlayerEnum,
        val moves: Int
    ) : GameState()
    data class Finished(val winner: String) : GameState()
}