package com.lmar.tictactoe.ui.screen.single_game

import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.lmar.tictactoe.core.Constants.BOARDS_REFERENCE
import com.lmar.tictactoe.core.Constants.DATABASE_REFERENCE
import com.lmar.tictactoe.core.enums.GameStatusEnum
import com.lmar.tictactoe.core.enums.PlayerTypeEnum
import com.lmar.tictactoe.core.state.GameState
import com.lmar.tictactoe.feature.ia.GameIA
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class SingleGameViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TAG = "SingleGameViewModel"
    }

    private var database: DatabaseReference = Firebase.database.getReference(DATABASE_REFERENCE)

    private val deviceInfo: String = "${Build.MANUFACTURER} ${Build.MODEL}"
    private val androidVersion: String = Build.VERSION.RELEASE

    private val _showDialogWinner = MutableLiveData(false)
    val showDialogWinner: LiveData<Boolean> = _showDialogWinner

    private val _showDialogDraw = MutableLiveData(false)
    val showDialogDraw: LiveData<Boolean> = _showDialogDraw

    private val _showDialogLoser = MutableLiveData(false)
    val showDialogLoser: LiveData<Boolean> = _showDialogLoser

    private val userId = Firebase.auth.currentUser?.uid ?: "default_user"

    private val ia = GameIA(userId, GameIA.Difficulty.EASY)

    private val _gameState = MutableLiveData<GameState>()
    val gameState: LiveData<GameState> = _gameState

    val isLoading = MutableLiveData(true)

    init {
        createNewGame()
    }

    private fun createNewGame() {
        isLoading.value = true
        val gameId = UUID.randomUUID().toString()
        val newGame = GameState(gameId = gameId)

        newGame.modificationUser = "$deviceInfo/$androidVersion/createNewGame"

        Log.d(TAG, "Creando Juego: $gameId")

        database.child(BOARDS_REFERENCE)
            .child(gameId)
            .setValue(newGame)
            .addOnSuccessListener {
                Log.d(TAG, "Juego creado con éxito: $gameId")
                isLoading.value = false
                listenForUpdates(gameId) // Solo escuchar si se creó con éxito
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al crear el juego $gameId", e)
                isLoading.value = false
            }
    }

    private fun listenForUpdates(gameId: String) {
        database.child(BOARDS_REFERENCE).child(gameId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(GameState::class.java)?.let {
                        _gameState.value = it
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error: ${error.message}")
                }
            })
    }

    fun onPlayerMove(row: Int, col: Int) {
        viewModelScope.launch {
            playerMove(row, col)
        }
    }

    private suspend fun playerMove(row: Int, col: Int) {
        _gameState.value?.let { state ->
            if(state.gameStatus != GameStatusEnum.FINISHED) {
                if (state.board[row][col].isEmpty()) {
                    Log.e(TAG, "Movimiento del Jugador X - IA")
                    state.board[row][col] = PlayerTypeEnum.X.name
                    ia.recordPlayerMove(row, col) // Guardamos el movimiento del jugador
                    checkGameState()
                    if (state.gameStatus != GameStatusEnum.FINISHED) {
                        delay(750)
                        makeAIMove()
                    }
                }
            }
        }
    }

    private fun makeAIMove() {
        _gameState.value?.let { state ->
            Log.e(TAG, "Movimiento del Jugador O - IA")
            val move = ia.getNextMove(state.board)
            state.board[move.first][move.second] = PlayerTypeEnum.O.name
            checkGameState()
        }
    }

    private fun checkGameState() {
        _gameState.value?.let { state ->
            val result = GameIA.checkWinner(state.board)
            var winner = ""
            when (result) {
                1 -> {
                    winner = PlayerTypeEnum.O.name
                    _showDialogLoser.value = true
                    state.gameStatus = GameStatusEnum.FINISHED
                    Log.e(TAG, "Perdiste")
                }
                0 -> {
                    winner = "Draw"
                    _showDialogDraw.value = true
                    state.gameStatus = GameStatusEnum.FINISHED
                    Log.e(TAG, "Empate")
                }
                -1 -> {
                    winner = PlayerTypeEnum.X.name
                    _showDialogWinner.value = true
                    state.gameStatus = GameStatusEnum.FINISHED
                    Log.e(TAG, "Ganaste")
                }
                else -> {
                    Log.e(TAG,"Juego en progreso")
                    state.currentPlayerType =
                        if (state.currentPlayerType == state.player1.playerType)
                            state.player2.playerType
                        else
                            state.player1.playerType
                }
            }

            //Actualizar Juego
            val updatedGame = state.copy(
                winner = winner,
                modificationUser = "$deviceInfo/$androidVersion/checkGameState",
                updatedAt = System.currentTimeMillis()
            )
            database.child(BOARDS_REFERENCE).child(state.gameId).setValue(updatedGame)
        }
    }

    fun resetAIMemory() {
        ia.resetMemory()
    }

    fun getAIMemoryStats(): Map<Pair<Int, Int>, Int> {
        return ia.getMemoryStats()
    }

    fun closeDialogs() {
        _showDialogWinner.value = false
        _showDialogDraw.value = false
        _showDialogLoser.value = false
    }

}