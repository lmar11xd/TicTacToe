package com.lmar.tictactoe.core.state

data class UserState (
    var id: String,
    var names: String = "",
    var email: String = "",
    var imageUrl: String = "",
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
) {
    constructor(): this("")
}