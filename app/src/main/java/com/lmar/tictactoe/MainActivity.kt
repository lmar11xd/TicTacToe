package com.lmar.tictactoe

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.lmar.tictactoe.ui.screen.ScreenRoutes
import com.lmar.tictactoe.ui.screen.game.GameScreen
import com.lmar.tictactoe.ui.screen.home.HomeScreen
import com.lmar.tictactoe.ui.theme.TicTacToeTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //FirebaseApp.initializeApp(this)

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

                        composable(route = ScreenRoutes.GameScreen.route) {
                            GameScreen(navController)
                        }
                    }
                }
            }
        }
    }
}
