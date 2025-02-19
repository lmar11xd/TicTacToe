package com.lmar.tictactoe.ui.screen.game

import com.lmar.tictactoe.core.enums.GameStatusEnum
import com.lmar.tictactoe.core.enums.PlayerEnum

data class GameState(
    var gameId: String = "-1",
    var board: MutableList<String> = MutableList(9) { "" },
    var winner: String = "",
    var gameStatus: GameStatusEnum = GameStatusEnum.CREATED,
    var currentPlayer: PlayerEnum = PlayerEnum.X,
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
) {
    constructor(gameId: String) : this(
        gameId = gameId,
        currentPlayer = PlayerEnum.entries.toTypedArray().random()
    )
}