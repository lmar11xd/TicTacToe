package com.lmar.tictactoe.ui.screen.game

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lmar.tictactoe.ui.theme.TicTacToeTheme

@Composable
fun GameScreen(
    navController: NavController,
    viewModel: GameViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val gameState by viewModel.gameState.observeAsState()

    if(gameState == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "¡Bienvenido a Tres en Raya!",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp),
            textAlign = TextAlign.Center
        )

        Text(text = "Turn: ${gameState?.currentPlayer ?: "Cargando..."}", fontSize = 24.sp)

        Box(
            modifier = Modifier
                .size(300.dp)
        ) {
            Column {
                for (row in 0..2) {
                    Row {
                        for (col in 0..2) {
                            val index = row * 3 + col
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .border(1.dp, Color.Black)
                                    .clickable(enabled = gameState?.winner.isNullOrEmpty()) {
                                        gameState?.let {
                                            viewModel.makeMove(index)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = gameState?.board?.getOrNull(index) ?: "",
                                    fontSize = 32.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        gameState?.winner?.let { winner ->
            if (winner.isNotEmpty()) {
                Text(
                    text = if (winner == "Draw") "Empate!" else "Ganador: $winner",
                    fontSize = 24.sp,
                    color = Color.Red,
                    modifier = Modifier.padding(8.dp)
                )

                Button(onClick = { viewModel.createNewGame() }) {
                    Text(text = "Reiniciar Juego")
                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    TicTacToeTheme {
        GameScreen(rememberNavController())
    }
}