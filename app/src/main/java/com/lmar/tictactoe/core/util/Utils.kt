package com.lmar.tictactoe.core.util

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

fun generarCodigoUnico(): String {
    val timestamp = System.currentTimeMillis().toString().takeLast(3) // Últimos 3 dígitos del tiempo
    val random = Random.nextInt(100, 999).toString() // Número aleatorio de 3 dígitos
    return timestamp + random
}

fun Color.darken(factor: Float = 0.8f): Color {
    return Color(
        red = this.red * factor,
        green = this.green * factor,
        blue = this.blue * factor,
        alpha = this.alpha
    )
}