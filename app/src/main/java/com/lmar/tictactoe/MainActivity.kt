package com.lmar.tictactoe

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.lmar.tictactoe.core.enums.ActionTypeEnum
import com.lmar.tictactoe.core.enums.GameLevelEnum
import com.lmar.tictactoe.ui.screen.ScreenRoutes
import com.lmar.tictactoe.ui.screen.game.GameScreen
import com.lmar.tictactoe.ui.screen.home.HomeScreen
import com.lmar.tictactoe.ui.screen.room.RoomScreen
import com.lmar.tictactoe.ui.screen.single_game.SingleGameScreen
import com.lmar.tictactoe.ui.theme.TicTacToeTheme

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

        enableEdgeToEdge()
        setContent {
            TicTacToeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = ScreenRoutes.HomeScreen.route
                    ) {
                        composable(route = ScreenRoutes.HomeScreen.route) {
                            HomeScreen(navController)
                        }

                        composable(
                            route = ScreenRoutes.SingleGameScreen.route + "?level={level}",
                            arguments = listOf(
                                navArgument("level") {
                                    type = NavType.StringType
                                    defaultValue = GameLevelEnum.EASY.name // Valor por defecto
                                }
                            )
                        ) {
                            SingleGameScreen(navController)
                        }

                        composable(
                            route = ScreenRoutes.GameScreen.route + "?actionType={actionType}&roomId={roomId}",
                            arguments = listOf(
                                navArgument("actionType") {
                                    type = NavType.StringType
                                    defaultValue = ActionTypeEnum.CREATE.name // Valor por defecto
                                },
                                navArgument("roomId") {
                                    type = NavType.StringType
                                    defaultValue = "0"
                                }
                            )
                        ){
                            GameScreen(navController = navController)
                        }

                        composable(route = ScreenRoutes.RoomScreen.route) {
                            RoomScreen(navController)
                        }
                    }
                }
            }
        }
    }
}
