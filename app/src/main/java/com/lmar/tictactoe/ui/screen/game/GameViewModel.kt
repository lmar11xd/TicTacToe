package com.lmar.tictactoe.ui.screen.game

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.lmar.tictactoe.core.enums.GameStatusEnum
import com.lmar.tictactoe.core.state.GameState
import com.lmar.tictactoe.core.util.generarCodigoUnico
import java.util.UUID

class GameViewModel : ViewModel() {

    private var database: DatabaseReference = Firebase.database.getReference("games/tictactoe")

    private val _gameState = MutableLiveData<GameState>()
    val gameState: LiveData<GameState> = _gameState

    init {
        Log.d("Firebase", "Database Reference: $database") // Verifica que no sea null
        createNewGame()
    }

    fun createNewGame() {
        val gameId = UUID.randomUUID().toString()
        val codigo = generarCodigoUnico() //Generar código para unirse a la partida
        val newGame = GameState(gameId = gameId, codigo)

        Log.e("Firebase", "Creando Juego: $gameId")

        database.child("boards").child(gameId).setValue(newGame)
            .addOnSuccessListener {
                Log.e("Firebase", "Juego creado con éxito: $gameId")
                listenForUpdates(gameId) // Solo escuchar si se creó con éxito
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al crear el juego", e)
            }
    }

    private fun listenForUpdates(gameId: String) {
        database.child("boards").child(gameId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(GameState::class.java)?.let {
                    Log.e("Firebase", "$it")
                    _gameState.value = it
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error: ${error.message}")
            }
        })
    }

    fun makeMove(index: Int) {
        val currentGame = _gameState.value ?: return
        if (currentGame.board[index].isNotEmpty() || currentGame.winner.isNotEmpty()) return

        val newBoard = currentGame.board.toMutableList()
        newBoard[index] = currentGame.currentPlayerType.name
        val nextPlayerType =
            if (currentGame.currentPlayerType.name == currentGame.player1.playerType.name)
                currentGame.player2.playerType
            else
                currentGame.player1.playerType

        val winner = checkWinner(newBoard)
        val isFinished = winner.isNotEmpty() // Si hay ganador o empate, el juego termina

        val updatedGame = currentGame.copy(
            board = newBoard,
            currentPlayerType = if (isFinished) currentGame.currentPlayerType else nextPlayerType,
            winner = winner,
            gameStatus = if (isFinished) GameStatusEnum.FINISHED else GameStatusEnum.IN_PROGRESS,
            updatedAt = System.currentTimeMillis()
        )

        database.child("boards").child(currentGame.gameId).setValue(updatedGame)
    }

    private fun checkWinner(board: List<String>): String {
        val winningPositions = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6)
        )
        for (positions in winningPositions) {
            val (a, b, c) = positions
            if (board[a].isNotEmpty() && board[a] == board[b] && board[b] == board[c]) {
                return board[a]
            }
        }
        return if (board.all { it.isNotEmpty() }) "Draw" else ""
    }
}