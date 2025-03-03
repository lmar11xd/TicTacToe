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
    //var board: MutableList<String> = MutableList(9) { "" },
    var board: MutableList<MutableList<String>> = MutableList(3){ MutableList(3) { "" } },
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
        gameType = GameTypeEnum.SINGLE
    )

}