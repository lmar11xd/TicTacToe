package com.lmar.tictactoe.core.state

import com.lmar.tictactoe.core.entity.Player
import com.lmar.tictactoe.core.enums.GameStatusEnum
import com.lmar.tictactoe.core.enums.PlayerTypeEnum

data class GameState(
    var gameId: String = "-1",
    var codigo: String = "",
    var board: MutableList<String> = MutableList(9) { "" },
    var winner: String = "",
    var player1: Player = Player(PlayerTypeEnum.X),
    var player2: Player = Player(PlayerTypeEnum.O),
    var gameStatus: GameStatusEnum = GameStatusEnum.CREATED,
    var currentPlayerType: PlayerTypeEnum = PlayerTypeEnum.X,
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
) {
    constructor(gameId: String, codigo: String) : this(
        gameId = gameId,
        codigo = codigo,
        currentPlayerType = PlayerTypeEnum.entries.toTypedArray().random()
    )
}