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
import com.lmar.tictactoe.core.entity.Player
import com.lmar.tictactoe.core.enums.ActionTypeEnum
import com.lmar.tictactoe.core.enums.GameStatusEnum
import com.lmar.tictactoe.core.enums.GameTypeEnum
import com.lmar.tictactoe.core.enums.PlayerStatusEnum
import com.lmar.tictactoe.core.enums.PlayerTypeEnum
import com.lmar.tictactoe.core.state.GameState
import com.lmar.tictactoe.core.state.RoomState
import com.lmar.tictactoe.feature.ia.GameIA
import kotlinx.coroutines.Dispatchers
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

    private val _actionType = MutableLiveData(savedStateHandle.get<String?>("actionType"))
    private val actionType: LiveData<String?> = _actionType

    private val _roomId = MutableLiveData(savedStateHandle.get<String?>("roomId"))
    private val roomId: LiveData<String?> = _roomId

    private val _playerType = MutableLiveData(savedStateHandle.get<String?>("playerType"))
    val playerType: LiveData<String?> = _playerType

    private val _gameState = MutableLiveData<GameState>()
    val gameState: LiveData<GameState> = _gameState

    private val _roomState = MutableLiveData<RoomState>()
    val roomState: LiveData<RoomState> = _roomState

    private val _roomReady = MediatorLiveData<Pair<String, String>>().apply {
        addSource(_actionType) { action ->
            val id = _roomId.value
            if (!id.isNullOrEmpty() && !action.isNullOrEmpty()) {
                val newValue = Pair(id, action)
                if (value != newValue) value = newValue
            }
        }

        addSource(_roomId) { id ->
            val action = _actionType.value
            if (!id.isNullOrEmpty() && !action.isNullOrEmpty()) {
                val newValue = Pair(id, action)
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

    private val _turnMessage = MutableLiveData<String>()
    val turnMessage: MutableLiveData<String> = _turnMessage

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    sealed class UiEvent {
        data object SoundTap: UiEvent()
    }

    init {
        _roomReady.observeForever { (roomId, actionType) ->
            Log.d(TAG, "RoomId: $roomId, ActionType: $actionType")
            if (roomId != "0") {
                getRoomById(roomId) {
                    if (actionType == ActionTypeEnum.CREATE.name) { // Player 1 (Creador)
                        Log.d(TAG, "Crear partida")
                        listenForRoomUpdates(roomId)
                        createNewGame(roomId)
                    } else { // Player 2 (Se une)
                        Log.d(TAG, "Unirse a la partida")
                        getGameByRoomId(roomId) { gameId ->
                            listenForRoomUpdates(roomId)
                            listenForUpdates(gameId)
                        }
                    }
                }
            }
        }
    }

    private fun joinPlayer2(gameState: GameState) {
        Log.d(TAG, "Actualizando status del Jugador O ${gameState.gameId}")
        val currentGame = gameState

        var playerType = PlayerTypeEnum.X
        if(currentGame.player1 != null && currentGame.player1?.playerType == PlayerTypeEnum.X) {
            playerType = PlayerTypeEnum.O
        }
        val player = Player(playerType, PlayerStatusEnum.ONLINE)
        _playerType.value = playerType.name

        val playerTypeInit = PlayerTypeEnum.entries.toTypedArray().random()
        val updatedGame = currentGame.copy(
            player2 = player,
            currentPlayerType = playerTypeInit,
            updatedAt = System.currentTimeMillis(),
            modificationUser = "$deviceInfo/$androidVersion/updateStatusPlayer2"
        )

        database.child(BOARDS_REFERENCE).child(currentGame.gameId).setValue(updatedGame)
    }

    private fun createNewGame(roomId: String) {
        val gameId = UUID.randomUUID().toString()

        val playerType = PlayerTypeEnum.entries.random()

        val player = Player(playerType, PlayerStatusEnum.ONLINE)

        val newGame = GameState(
            gameId = gameId,
            roomId = roomId,
            gameType = GameTypeEnum.MULTIPLAYER,
            player1 = player
        )

        newGame.modificationUser = "$deviceInfo/$androidVersion/createNewGame"

        _turnMessage.value = "Esperando al otro jugador"
        _winCells.value = emptyList()
        _playerType.value = playerType.name

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
            if (currentGame.currentPlayerType.name == PlayerTypeEnum.X.name)
                PlayerTypeEnum.O
            else
                PlayerTypeEnum.X

        val result = GameIA.checkWinner(newBoard)
        val isFinished = result != null // Si hay ganador o empate, el juego termina

        var winner = ""
        when(result) {
            1 -> winner = PlayerTypeEnum.O.name
            0 -> winner = "Draw"
            -1 -> winner = PlayerTypeEnum.X.name
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
                        var lastGame: GameState? = null
                        for(child in snapshot.children) {
                            val game = child.getValue(GameState::class.java)
                            if(game != null && game.gameStatus != GameStatusEnum.FINISHED) {
                                // Se queda con el de mayor updatedAt
                                if (lastGame == null || game.updatedAt > lastGame.updatedAt) {
                                    lastGame = game
                                }
                            }
                        }

                        if (lastGame != null) {
                            lastGame.let {
                                _gameState.value = it
                                Log.e(TAG, "Game: $it")
                                Log.d(TAG, "Partida con roomId $roomId encontrada")

                                joinPlayer2(it)
                                Log.d(TAG, "Status del Jugador O actualizado ")

                                onComplete(it.gameId)
                            }
                        } else {
                            Log.d(TAG, "No se encontraron juegos en curso en la sala $roomId")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error al obtener la sala: ${error.message}")
                }
            })
    }

    fun getTurnMessage(gameState: GameState): String {
        if(gameState.gameStatus == GameStatusEnum.CREATED) {
            return "Esperando al otro jugador"
        }

        if(gameState.gameStatus == GameStatusEnum.IN_PROGRESS) {
            _turnMessage.value = if(gameState.currentPlayerType.name == _playerType.value) {
                "Es tu turno"
            } else {
                "Es turno del Jugador ${gameState.currentPlayerType.name}"
            }
        }

        if(gameState.gameStatus == GameStatusEnum.FINISHED) {
            _turnMessage.value = when (gameState.winner) {
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

        return ""
    }

    fun closeDialogs() {
        _showDialogWinner.value = false
        _showDialogDraw.value = false
        _showDialogLoser.value = false
    }

    fun onPlayerMove(row: Int, col: Int) {
        viewModelScope.launch(Dispatchers.IO) {// Corre en un hilo de fondo
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
        viewModelScope.launch(Dispatchers.Main) { // Mantiene la UI reactiva
            delay(1500)
            when(winner) {
                _playerType.value -> _showDialogWinner.value = true
                "Draw" -> _showDialogDraw.value = true
                else -> _showDialogLoser.value = true
            }
        }
    }

    fun onPlayerJoined(player: String) {
        Log.e("GameScreen", "El jugador $player ingresó a la pantalla del juego")
    }

    fun onPlayerExit() {
        Log.e("GameScreen", "El jugador $_playerType.value salió de la pantalla del juego")
        _gameState.value?.let {
            val currentGame = it
            if(_playerType.value == currentGame.player1?.playerType?.name) {
                currentGame.player1?.playerStatus = PlayerStatusEnum.OFFLINE
            } else {
                currentGame.player2?.playerStatus = PlayerStatusEnum.OFFLINE
            }

            val updatedGame = currentGame.copy(
                modificationUser = "$deviceInfo/$androidVersion/onPlayerExit",
                updatedAt = System.currentTimeMillis()
            )

            database.child(BOARDS_REFERENCE).child(currentGame.gameId).setValue(updatedGame)
        }
    }
}