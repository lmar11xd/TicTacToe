package com.lmar.tictactoe.ui.screen

sealed class ScreenRoutes(val route: String) {
    data object HomeScreen: ScreenRoutes("home_screen")
    data object SingleGameScreen: ScreenRoutes("singlegame_screen")
    data object GameScreen: ScreenRoutes("game_screen")
    data object RoomScreen: ScreenRoutes("room_screen")
}