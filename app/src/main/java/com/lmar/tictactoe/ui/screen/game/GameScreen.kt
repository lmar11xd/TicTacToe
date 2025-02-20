package com.lmar.tictactoe.ui.screen.game

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lmar.tictactoe.core.enums.PlayerTypeEnum
import com.lmar.tictactoe.ui.component.GlowingCard
import com.lmar.tictactoe.ui.component.ShadowText
import com.lmar.tictactoe.ui.theme.TicTacToeTheme

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    navController: NavController,
    viewModel: GameViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val gameState by viewModel.gameState.observeAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                title = { Text("Tres en Raya", color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                },
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(
                    rememberTopAppBarState()
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            if (gameState == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                TopAppBar(
                    title = {  },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )

                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(100.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        //Player 1
                        GlowingCard(
                            modifier = Modifier.width(70.dp).height(60.dp),
                            glowingColor = if (gameState?.currentPlayerType?.name == PlayerTypeEnum.X.name) PlayerTypeEnum.X.color else MaterialTheme.colorScheme.primary,
                            containerColor = if (gameState?.currentPlayerType?.name == PlayerTypeEnum.X.name) PlayerTypeEnum.X.color else MaterialTheme.colorScheme.primary,
                            cornerRadius = 8.dp,
                        ) {
                            Column(
                                modifier = Modifier.align(Alignment.BottomCenter)
                            ) {
                                ShadowText(
                                    text = PlayerTypeEnum.X.name,
                                    textColor = Color.White,
                                    shadowColor = Color.White,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                                Text("Jugador 1", fontSize = 8.sp, color = Color.White)
                            }
                        }

                        //VS
                        GlowingCard(
                            modifier = Modifier.size(70.dp).padding(10.dp),
                            glowingColor = MaterialTheme.colorScheme.tertiary,
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            cornerRadius = Int.MAX_VALUE.dp,
                        ) {
                            ShadowText(
                                text = "VS",
                                fontSize = 18.sp,
                                textColor = Color.White,
                                shadowColor = Color.White,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        //Player 2
                        GlowingCard(
                            modifier = Modifier.width(70.dp).height(60.dp),
                            glowingColor = if (gameState?.currentPlayerType?.name == PlayerTypeEnum.O.name) PlayerTypeEnum.O.color else MaterialTheme.colorScheme.primary,
                            containerColor = if (gameState?.currentPlayerType?.name == PlayerTypeEnum.O.name) PlayerTypeEnum.O.color else MaterialTheme.colorScheme.primary,
                            cornerRadius = 8.dp,
                        ) {
                            Column(
                                modifier = Modifier.align(Alignment.BottomCenter)
                            ) {
                                ShadowText(
                                    text = PlayerTypeEnum.O.name,
                                    textColor = Color.White,
                                    shadowColor = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                )

                                Text("Jugador 2", fontSize = 8.sp, color = Color.White)
                            }
                        }
                    }

                    Box(modifier = Modifier.height(16.dp))

                    // Tablero de juego con animación y bordes redondeados
                    GlowingCard(
                        glowingColor = MaterialTheme.colorScheme.primary,
                        cornerRadius = 12.dp,
                        glowingRadius = 20.dp,
                        containerColor = Color.Transparent
                    ) {
                        Column(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                        ) {
                            for (row in 0..2) {
                                Row {
                                    for (col in 0..2) {
                                        val index = row * 3 + col
                                        Box(modifier = Modifier.padding(2.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(100.dp)
                                                    .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f))
                                                    .clickable(enabled = gameState?.winner.isNullOrEmpty()) {
                                                        viewModel.makeMove(index)
                                                    }
                                                    .padding(16.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                ShadowText(
                                                    text = gameState?.board?.get(index) ?: "",
                                                    fontSize = 48.sp,
                                                    textColor =
                                                    if (gameState?.board?.get(index) == PlayerTypeEnum.X.name)
                                                        PlayerTypeEnum.X.color
                                                    else
                                                        PlayerTypeEnum.O.color,
                                                    shadowColor = Color.White,
                                                    modifier = Modifier.animateContentSize()
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            gameState?.codigo?.let {
                                Text(
                                    text = it,
                                    fontSize = 12.sp,
                                    color = Color.White.copy(0.5f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }

                    gameState?.winner?.let { winner ->
                        if (winner.isNotEmpty()) {
                            Text(
                                text = if (winner == "Draw") "¡Empate!" else "¡Ganador: $winner!",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50),
                                modifier = Modifier.padding(top = 16.dp)
                            )

                            Button(
                                onClick = { viewModel.createNewGame() },
                                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.tertiary),
                                modifier = Modifier.padding(top = 12.dp)
                            ) {
                                Text(text = "Nuevo Juego", color = Color.White)
                            }
                        }
                    }
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