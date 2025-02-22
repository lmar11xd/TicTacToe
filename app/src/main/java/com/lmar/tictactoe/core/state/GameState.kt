package com.lmar.tictactoe.core.state

import com.lmar.tictactoe.core.entity.Player
import com.lmar.tictactoe.core.enums.GameStatusEnum
import com.lmar.tictactoe.core.enums.GameTypeEnum
import com.lmar.tictactoe.core.enums.PlayerStatusEnum
import com.lmar.tictactoe.core.enums.PlayerTypeEnum

data class GameState(
    var gameId: String,
    var roomId: String = "",
    var gameType: GameTypeEnum = GameTypeEnum.SINGLE,
    var gameStatus: GameStatusEnum = GameStatusEnum.CREATED,
    var board: MutableList<String> = MutableList(9) { "" },
    var player1: Player = Player(PlayerTypeEnum.X, PlayerStatusEnum.ONLINE),
    var player2: Player = Player(PlayerTypeEnum.O),
    var currentPlayerType: PlayerTypeEnum = PlayerTypeEnum.X,
    var winner: String = "",
    var modificationUser: String = "",
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
) {
    constructor() : this("")// Constructor vacío requerido por Firebase

    constructor(gameId: String) : this (
        gameId = gameId,
        gameType = GameTypeEnum.SINGLE,
        currentPlayerType = PlayerTypeEnum.entries.toTypedArray().random()
    )

    constructor(gameId: String, roomId: String, gameType: GameTypeEnum) : this (
        gameId = gameId,
        roomId = roomId,
        gameType = gameType,
        currentPlayerType = PlayerTypeEnum.entries.toTypedArray().random()
    )
}