package com.lmar.tictactoe.core.entity

class User {
    var id: String = ""
    var names: String = ""
    var email: String = ""
    var imageUrl: String = ""
    var createdAt: Long = System.currentTimeMillis()
    var updatedAt: Long = System.currentTimeMillis()

    constructor()
}