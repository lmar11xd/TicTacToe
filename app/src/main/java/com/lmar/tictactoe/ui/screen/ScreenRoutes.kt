package com.lmar.tictactoe.ui.screen

sealed class ScreenRoutes(val route: String) {
    object HomeScreen: ScreenRoutes("home_screen")
    object GameScreen: ScreenRoutes("game_screen")
}