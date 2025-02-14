package com.lmar.tictactoe.ui.screen.game

import androidx.lifecycle.ViewModel
import com.lmar.tictactoe.core.enums.PlayerEnum
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GameViewModel : ViewModel() {
    private val _gameState = MutableStateFlow<GameState>(GameState.Start)
    val gameState: StateFlow<GameState> = _gameState

    private fun checkWinner(board: List<List<String>>): String? {
        for (i in 0..2) {
            if (board[i][0] == board[i][1] && board[i][1] == board[i][2] && board[i][0].isNotEmpty()) {
                return board[i][0]
            }
            if (board[0][i] == board[1][i] && board[1][i] == board[2][i] && board[0][i].isNotEmpty()) {
                return board[0][i]
            }
        }
        if (board[0][0] == board[1][1] && board[1][1] == board[2][2] && board[0][0].isNotEmpty()) {
            return board[0][0]
        }
        if (board[0][2] == board[1][1] && board[1][1] == board[2][0] && board[0][2].isNotEmpty()) {
            return board[0][2]
        }
        return null
    }

    fun startGame() {
        _gameState.value = GameState.Playing(
            board = List(3) { MutableList(3) { "" } },
            currentPlayer = PlayerEnum.X,
            moves = 0
        )
    }

    fun makeMove(row: Int, col: Int) {
        val currentGameState = _gameState.value
        if (currentGameState is GameState.Playing) {
            val board = currentGameState.board.map { it.toMutableList() }
            if (board[row][col].isEmpty()) {
                board[row][col] = currentGameState.currentPlayer.name
                val winner = checkWinner(board)
                val moves = currentGameState.moves + 1

                _gameState.value = when {
                    winner != null -> GameState.Finished("$winner ganó!")
                    moves == 9 -> GameState.Finished("¡Empate!")
                    else -> {
                        val nextPlayer = if (currentGameState.currentPlayer == PlayerEnum.X) PlayerEnum.O else PlayerEnum.X
                        GameState.Playing(board, nextPlayer, moves)
                    }
                }
            }
        }
    }

    fun resetGame() {
        _gameState.value = GameState.Start
    }
}