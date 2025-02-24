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

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    sealed class UiEvent {
        data object SoundTap: UiEvent()
    }

    init {
        _roomReady.observeForever { (roomId, playerType) ->
            Log.d(TAG, "RoomId: $roomId, PlayerType: $playerType")

            if (roomId == "0") {
                createNewGame()
            } else {
                getRoomById(roomId) {
                    if (playerType == PlayerTypeEnum.X.name) { // Player 1 (Creador)
                        Log.d(TAG, "Jugador X va a crear la partida")
                        listenForRoomUpdates(roomId)
                        createNewGameMultiPlayer(roomId)
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

    private fun createNewGame() {
        val gameId = UUID.randomUUID().toString()
        val newGame = GameState(gameId = gameId)

        newGame.modificationUser = "$deviceInfo/$androidVersion/createNewGame"

        Log.d(TAG, "Creando Juego: $gameId")

        database.child(BOARDS_REFERENCE)
            .child(gameId)
            .setValue(newGame)
            .addOnSuccessListener {
                Log.d(TAG, "Juego creado con éxito: $gameId")
                listenForUpdates(gameId) // Solo escuchar si se creó con éxito
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al crear el juego $gameId", e)
            }
    }

    private fun createNewGameMultiPlayer(roomId: String) {
        val gameId = UUID.randomUUID().toString()
        val newGame = GameState(gameId = gameId, roomId = roomId, GameTypeEnum.MULTIPLAYER)
        newGame.modificationUser = "$deviceInfo/$androidVersion/createNewGameMultiPlayer"

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
        if(roomId.value?.isEmpty() == true) {
            createNewGame()
        } else {
            roomId.value?.let { createNewGameMultiPlayer(it) }
        }
    }

    private fun listenForUpdates(gameId: String) {
        database.child(BOARDS_REFERENCE).child(gameId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(GameState::class.java)?.let {
                        _gameState.value = it
                        if(it.gameStatus == GameStatusEnum.FINISHED) {
                            openDialogs(it.winner)
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

    private suspend fun makeMove(index: Int) {
        val currentGame = _gameState.value ?: return
        if (currentGame.board[index].isNotEmpty() || currentGame.winner.isNotEmpty()) return

        if(playerType.value != currentGame.currentPlayerType.name) {
            Log.e(TAG, "No es tu turno")
            return
        }

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
            modificationUser = "$deviceInfo/$androidVersion/makeMove",
            updatedAt = System.currentTimeMillis()
        )

        _eventFlow.emit(UiEvent.SoundTap)

        database.child(BOARDS_REFERENCE).child(currentGame.gameId).setValue(updatedGame)
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

    private fun openDialogs(winner: String) {
        Log.e(TAG, "${_playerType.value} - Ganador: $winner")
        when (winner) {
            "Draw" -> {
                Log.e(TAG, "Empate")
                _showDialogDraw.value = true
            }
            _playerType.value -> {
                Log.e(TAG, "Ganaste")
                _showDialogWinner.value = true
            }
            else -> {
                Log.e(TAG, "Perdiste")
                _showDialogLoser.value = true
            }
        }
    }

    fun closeDialogs() {
        _showDialogWinner.value = false
        _showDialogDraw.value = false
        _showDialogLoser.value = false
    }

    fun onMoveMade(index: Int) {
        viewModelScope.launch {
            makeMove(index)  // Ahora se ejecuta dentro de una corrutina
        }
    }
}