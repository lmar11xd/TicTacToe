package com.lmar.tictactoe.ui.screen.game

import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.lmar.tictactoe.core.Constants.BOARDS_REFERENCE
import com.lmar.tictactoe.core.Constants.DATABASE_REFERENCE
import com.lmar.tictactoe.core.Constants.ROOMS_REFERENCE
import com.lmar.tictactoe.core.enums.GameStatusEnum
import com.lmar.tictactoe.core.enums.GameTypeEnum
import com.lmar.tictactoe.core.enums.PlayerStatusEnum
import com.lmar.tictactoe.core.enums.PlayerTypeEnum
import com.lmar.tictactoe.core.enums.RoomStatusEnum
import com.lmar.tictactoe.core.state.GameState
import com.lmar.tictactoe.core.state.RoomState
import com.lmar.tictactoe.feature.ia.GameIA
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID

class GameViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TAG = "GameViewModel"
    }

    private var database: DatabaseReference = Firebase.database.getReference(DATABASE_REFERENCE)

    private val _roomId = MutableLiveData(savedStateHandle.get<String?>("roomId"))
    private val roomId: LiveData<String?> = _roomId

    private val _playerType = MutableLiveData(savedStateHandle.get<String?>("playerType"))
    val playerType: LiveData<String?> = _playerType

    private val _gameState = MutableLiveData<GameState>()
    val gameState: LiveData<GameState> = _gameState

    private val _roomState = MutableLiveData<RoomState>()
    val roomState: LiveData<RoomState> = _roomState

    private val _roomReady = MediatorLiveData<Pair<String, String>>().apply {
        addSource(_roomId) { id ->
            val type = _playerType.value
            if (!id.isNullOrEmpty() && !type.isNullOrEmpty()) {
                val newValue = Pair(id, type)
                if (value != newValue) value = newValue
            }
        }
        addSource(_playerType) { type ->
            val id = _roomId.value
            if (!id.isNullOrEmpty() && !type.isNullOrEmpty()) {
                val newValue = Pair(id, type)
                if (value != newValue) value = newValue
            }
        }
    }

    private val deviceInfo: String = "${Build.MANUFACTURER} ${Build.MODEL}"
    private val androidVersion: String = Build.VERSION.RELEASE

    private val _showDialogWinner = MutableLiveData(false)
    val showDialogWinner: LiveData<Boolean> = _showDialogWinner

    private val _showDialogDraw = MutableLiveData(false)
    val showDialogDraw: LiveData<Boolean> = _showDialogDraw

    private val _showDialogLoser = MutableLiveData(false)
    val showDialogLoser: LiveData<Boolean> = _showDialogLoser

    private val _winCells = MutableLiveData<List<Pair<Int, Int>>>()
    val winCells: MutableLiveData<List<Pair<Int, Int>>> = _winCells

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    sealed class UiEvent {
        data object SoundTap: UiEvent()
    }

    init {
        _roomReady.observeForever { (roomId, playerType) ->
            Log.d(TAG, "RoomId: $roomId, PlayerType: $playerType")
            if (roomId != "0") {
                getRoomById(roomId) {
                    if (playerType == PlayerTypeEnum.X.name) { // Player 1 (Creador)
                        Log.d(TAG, "Jugador X va a crear la partida")
                        listenForRoomUpdates(roomId)
                        createNewGame(roomId)
                    } else { // Player 2 (Se une)
                        Log.d(TAG, "Jugador O va a unirse a la partida")
                        getGameByRoomId(roomId) { gameId ->
                            listenForRoomUpdates(roomId)
                            listenForUpdates(gameId)
                        }
                    }
                }
            }
        }
    }

    private fun updateStatusPlayer2(gameState: GameState) {
        Log.d(TAG, "Actualizando status del Jugador O ${gameState.gameId}")
        val currentGame = gameState
        currentGame.player2.playerStatus = PlayerStatusEnum.ONLINE

        val playerTypeInit = PlayerTypeEnum.entries.toTypedArray().random()
        val updatedGame = currentGame.copy(
            player2 = currentGame.player2,
            currentPlayerType = playerTypeInit,
            updatedAt = System.currentTimeMillis(),
            modificationUser = "$deviceInfo/$androidVersion/updateStatusPlayer2"
        )

        database.child(BOARDS_REFERENCE).child(currentGame.gameId).setValue(updatedGame)
    }

    private fun createNewGame(roomId: String) {
        val gameId = UUID.randomUUID().toString()
        val newGame = GameState(gameId = gameId, roomId = roomId, GameTypeEnum.MULTIPLAYER)
        newGame.modificationUser = "$deviceInfo/$androidVersion/createNewGame"

        _winCells.value = emptyList()

        Log.d(TAG, "Creando Juego: $gameId")

        database.child(BOARDS_REFERENCE)
            .child(gameId)
            .setValue(newGame)
            .addOnSuccessListener {
                Log.d(TAG, "Juego creado con éxito: $gameId")

                // Solo escuchar si se creó con éxito
                listenForUpdates(gameId)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al crear el juego $gameId", e)
            }
    }

    fun onNewGame() {
        roomId.value?.let {
            createNewGame(it)
        }
    }

    private fun listenForUpdates(gameId: String) {
        database.child(BOARDS_REFERENCE).child(gameId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(GameState::class.java)?.let {
                        _gameState.value = it
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

    private fun listenForRoomUpdates(gameId: String) {
        database.child(ROOMS_REFERENCE).child(gameId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(RoomState::class.java)?.let {
                        _roomState.value = it
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error: ${error.message}")
                }
            })
    }

    private suspend fun playerMove(row: Int, col: Int) {
        val currentGame = _gameState.value ?: return
        if (currentGame.board[row][col].isNotEmpty() || currentGame.winner.isNotEmpty()) return

        if(playerType.value != currentGame.currentPlayerType.name) {
            Log.e(TAG, "No es tu turno")
            return
        }

        val newBoard = currentGame.board.toMutableList()
        newBoard[row][col] = currentGame.currentPlayerType.name
        val nextPlayerType =
            if (currentGame.currentPlayerType.name == currentGame.player1.playerType.name)
                currentGame.player2.playerType
            else
                currentGame.player1.playerType

        val result = GameIA.checkWinner(newBoard)
        val isFinished = result != null // Si hay ganador o empate, el juego termina

        var winner = ""
        when(result) {
            1 -> {
                winner = PlayerTypeEnum.O.name
            }
            0 -> {
                winner = "Draw"
            }
            -1 -> {
                winner = PlayerTypeEnum.X.name
            }
        }

        val updatedGame = currentGame.copy(
            board = newBoard,
            currentPlayerType = if (isFinished) currentGame.currentPlayerType else nextPlayerType,
            winner = winner,
            gameStatus = if (isFinished) GameStatusEnum.FINISHED else GameStatusEnum.IN_PROGRESS,
            modificationUser = "$deviceInfo/$androidVersion/makeMove",
            updatedAt = System.currentTimeMillis()
        )

        _eventFlow.emit(UiEvent.SoundTap)

        database.child(BOARDS_REFERENCE).child(currentGame.gameId).setValue(updatedGame)
    }

    private fun getRoomById(roomId: String, onComplete: () -> Unit) {
        database.child(ROOMS_REFERENCE).child(roomId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        snapshot.getValue(RoomState::class.java)?.let {
                            _roomState.value = it
                            Log.d(TAG,"Sala con ID ${it.roomId} encontrada")
                            onComplete()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error al obtener la sala: ${error.message}")
                }
            })
    }

    private fun getGameByRoomId(roomId: String, onComplete: (gameId: String) -> Unit) {
        database.child(BOARDS_REFERENCE).orderByChild("roomId").equalTo(roomId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val gameSnapshot = snapshot.children.firstOrNull()
                        Log.e(TAG, "Snapshot Game: $snapshot")
                        if (gameSnapshot != null) {
                            gameSnapshot.getValue(GameState::class.java)?.let {
                                _gameState.value = it
                                Log.e(TAG, "Game: $it")
                                Log.d(TAG, "Partida con roomId $roomId encontrada")

                                updateStatusPlayer2(it)
                                Log.d(TAG, "Status del Jugador O actualizado ")

                                onComplete(it.gameId)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error al obtener la sala: ${error.message}")
                }
            })
    }

    fun getTurnMessage(): String {
        if(_roomState.value?.roomStatus == RoomStatusEnum.OPENED) {
            if(_gameState.value?.currentPlayerType?.name == _playerType.value) {
                return  "esperando al Jugador 0"
            }
        }

        if(_roomState.value?.roomStatus == RoomStatusEnum.COMPLETED) {

            if(_gameState.value?.gameStatus == GameStatusEnum.CREATED
                || _gameState.value?.gameStatus == GameStatusEnum.IN_PROGRESS) {
                return if(_gameState.value?.currentPlayerType?.name == _playerType.value) {
                    "es tu turno"
                } else {
                    "es turno del Jugador ${_gameState.value?.currentPlayerType?.name}"
                }
            }

            if(_gameState.value?.gameStatus == GameStatusEnum.FINISHED) {
                return when (_gameState.value?.winner) {
                    "Draw" -> {
                        "El juego ha terminado en empate"
                    }
                    _playerType.value -> {
                        "¡Felicidades has ganado!"
                    }
                    else -> {
                        "El juego ha terminado, has perdido"
                    }
                }
            }
        }

        return ""
    }

    fun closeDialogs() {
        _showDialogWinner.value = false
        _showDialogDraw.value = false
        _showDialogLoser.value = false
    }

    fun onPlayerMove(row: Int, col: Int) {
        viewModelScope.launch {
            playerMove(row, col)
        }
    }

    private fun getWinCells() {
        gameState.value?.let { gameState ->
            _winCells.value = GameIA.getWinCells(gameState.board, gameState.winner)
        }
    }

    private fun endGame(winner: String) {
        getWinCells()
        viewModelScope.launch {
            delay(1500)
            when(winner) {
                _playerType.value -> _showDialogWinner.value = true
                "Draw" -> _showDialogDraw.value = true
                else -> _showDialogLoser.value = true
            }
        }
    }
}