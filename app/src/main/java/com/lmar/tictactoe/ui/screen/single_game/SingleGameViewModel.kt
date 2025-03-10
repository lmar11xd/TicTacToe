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
import com.lmar.tictactoe.core.enums.GameLevelEnum
import com.lmar.tictactoe.core.enums.GameStatusEnum
import com.lmar.tictactoe.core.enums.PlayerTypeEnum
import com.lmar.tictactoe.core.state.GameState
import com.lmar.tictactoe.feature.ia.GameIA
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private lateinit var ia: GameIA

    private val _gameState = MutableLiveData<GameState>()
    val gameState: LiveData<GameState> = _gameState

    private val _winCells = MutableLiveData<List<Pair<Int, Int>>>()
    val winCells: MutableLiveData<List<Pair<Int, Int>>> = _winCells

    private val _isBoardDisabled = MutableStateFlow(false)
    val isBoardDisabled: StateFlow<Boolean> = _isBoardDisabled

    private val _turnMessage = MutableLiveData<String>()
    val turnMessage: MutableLiveData<String> = _turnMessage

    val isLoading = MutableLiveData(true)

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    sealed class UiEvent {
        data object SoundTap: UiEvent()
    }

    init {
        savedStateHandle.get<String>("level")?.let { level ->

            //Dificultad del Juego
            val gameLevel = when(level) {
                GameLevelEnum.EASY.name -> GameLevelEnum.EASY
                GameLevelEnum.MEDIUM.name -> GameLevelEnum.MEDIUM
                GameLevelEnum.HARD.name -> GameLevelEnum.HARD
                else ->  GameLevelEnum.EASY
            }

            createNewGame(gameLevel)
        }
    }

    fun createNewGame(level: GameLevelEnum) {
        isLoading.value = true
        _winCells.value = emptyList()

        ia = GameIA(userId, level)

        val gameId = UUID.randomUUID().toString()
        val newGame = GameState(gameId = gameId)

        newGame.level = level
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
                        getTurnMessage(it)
                        if(it.gameStatus == GameStatusEnum.FINISHED) {
                            endGame(it.winner)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error: ${error.message}")
                }
            })
    }

    private fun endGame(winner: String) {
        getWinCells()
        viewModelScope.launch {
            delay(1500)
            when(winner) {
                PlayerTypeEnum.X.name -> _showDialogWinner.value = true
                PlayerTypeEnum.O.name -> _showDialogLoser.value = true
                else -> _showDialogDraw.value = true
            }
        }
    }

    fun onPlayerMove(row: Int, col: Int) {
        viewModelScope.launch {
            _isBoardDisabled.value = true // 🔒 Desactiva el tablero
            playerMove(row, col)
            _isBoardDisabled.value = false // 🔓 Reactiva el tablero después del turno IA
        }
    }

    private suspend fun playerMove(row: Int, col: Int) {
        _gameState.value?.let { state ->
            if(state.gameStatus != GameStatusEnum.FINISHED) {
                if (state.board[row][col].isEmpty()) {
                    Log.e(TAG, "Movimiento del Jugador X")
                    state.board[row][col] = PlayerTypeEnum.X.name
                    ia.recordPlayerMove(row, col) // Guardamos el movimiento del jugador

                    _eventFlow.emit(UiEvent.SoundTap)

                    checkGameState()

                    if (state.gameStatus != GameStatusEnum.FINISHED) {
                        delay(750)
                        makeAIMove()
                    }
                }
            }
        }
    }

    private suspend fun makeAIMove() {
        _gameState.value?.let { state ->
            Log.e(TAG, "Movimiento del Jugador O - IA")
            val move = ia.getNextMove(state.board)
            state.board[move.first][move.second] = PlayerTypeEnum.O.name

            _eventFlow.emit(UiEvent.SoundTap)

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
                    state.gameStatus = GameStatusEnum.FINISHED
                    Log.e(TAG, "Perdiste")
                }
                0 -> {
                    winner = "Draw"
                    state.gameStatus = GameStatusEnum.FINISHED
                    Log.e(TAG, "Empate")
                }
                -1 -> {
                    winner = PlayerTypeEnum.X.name
                    state.gameStatus = GameStatusEnum.FINISHED
                    Log.e(TAG, "Ganaste")
                }
                else -> {
                    Log.e(TAG,"Juego en progreso")
                    //Cambiar turno
                    state.currentPlayerType =
                        if (state.currentPlayerType == PlayerTypeEnum.X)
                            PlayerTypeEnum.O
                        else
                            PlayerTypeEnum.X
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

    private fun getWinCells() {
        gameState.value?.let { gameState ->
            _winCells.value = GameIA.getWinCells(gameState.board, gameState.winner)
        }
    }

    fun closeDialogs() {
        _showDialogWinner.value = false
        _showDialogDraw.value = false
        _showDialogLoser.value = false
    }

    fun getTurnMessage(gameState: GameState): String {

        if(gameState.gameStatus == GameStatusEnum.CREATED
            || gameState.gameStatus == GameStatusEnum.IN_PROGRESS) {
            _turnMessage.value = if(gameState.currentPlayerType == PlayerTypeEnum.X) {
                "¡Es tu turno!"
            } else {
                "¡Turno de la Computadora!"
            }
        }

        if(gameState.gameStatus == GameStatusEnum.FINISHED) {
            _turnMessage.value = when (gameState.winner) {
                "Draw" -> {
                    "El juego ha terminado en empate"
                }
                PlayerTypeEnum.X.name -> {
                    "¡Felicidades has ganado!"
                }
                else -> {
                    "El juego ha terminado, has perdido"
                }
            }
        }

        return ""
    }

}