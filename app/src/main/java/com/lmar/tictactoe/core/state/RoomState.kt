package com.lmar.tictactoe.core.state

import com.lmar.tictactoe.core.enums.RoomStatusEnum
import com.lmar.tictactoe.core.util.generateUniqueCode

data class RoomState(
    var roomId: String,
    var roomCode: String,
    var roomStatus: RoomStatusEnum = RoomStatusEnum.OPENED,
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
) {
    constructor() : this("")

    constructor(roomId: String) : this (
        roomId = roomId,
        roomCode = generateUniqueCode(),
        roomStatus = RoomStatusEnum.OPENED
    )
}