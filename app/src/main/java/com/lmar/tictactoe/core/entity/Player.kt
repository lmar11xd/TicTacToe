package com.lmar.tictactoe.core.entity

import com.lmar.tictactoe.core.enums.PlayerTypeEnum

class Player {
    var nickName: String = ""
    var userId: String = ""
    var points: Int = 0
    var playerType: PlayerTypeEnum = PlayerTypeEnum.X

    constructor()

    constructor(playerType: PlayerTypeEnum) {
        this.playerType = playerType
    }

    constructor(nickName: String, userId: String, points: Int, playerType: PlayerTypeEnum) {
        this.nickName = nickName
        this.userId = userId
        this.points = points
        this.playerType = playerType
    }
}