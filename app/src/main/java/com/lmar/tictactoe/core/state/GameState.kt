package com.lmar.tictactoe.core.state

import com.lmar.tictactoe.core.entity.Player
import com.lmar.tictactoe.core.enums.GameLevelEnum
import com.lmar.tictactoe.core.enums.GameStatusEnum
import com.lmar.tictactoe.core.enums.GameTypeEnum
import com.lmar.tictactoe.core.enums.PlayerTypeEnum

data class GameState(
    var gameId: String,
    var roomId: String = "",
    var gameType: GameTypeEnum = GameTypeEnum.SINGLE,
    var gameStatus: GameStatusEnum = GameStatusEnum.CREATED,
    var level: GameLevelEnum = GameLevelEnum.EASY,
    var player1: Player? = null,
    var player2: Player? = null,
    var board: MutableList<MutableList<String>> = MutableList(3){ MutableList(3) { "" } },
    var currentPlayerType: PlayerTypeEnum = PlayerTypeEnum.X,
    var winner: String = "",
    var modificationUser: String = "",
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
) {
    constructor() : this("")// Constructor vacío requerido por Firebase

    constructor(gameId: String, player1: Player?, player2: Player?) : this (
        gameId = gameId,
        gameType = GameTypeEnum.SINGLE,
        player1 = player1,
        player2 = player2
    )

}