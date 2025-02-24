package com.lmar.tictactoe.ui.screen.game

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lmar.tictactoe.core.enums.PlayerStatusEnum
import com.lmar.tictactoe.core.enums.PlayerTypeEnum
import com.lmar.tictactoe.core.enums.RoomStatusEnum
import com.lmar.tictactoe.feature.sounds.SoundEffectPlayer
import com.lmar.tictactoe.ui.component.CustomAppBar
import com.lmar.tictactoe.ui.component.GlowingCard
import com.lmar.tictactoe.ui.component.ShadowText
import com.lmar.tictactoe.ui.component.message_dialog.DialogTypeEnum
import com.lmar.tictactoe.ui.component.message_dialog.MessageDialog
import com.lmar.tictactoe.ui.theme.TicTacToeTheme
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    navController: NavController,
    viewModel: GameViewModel = viewModel()
) {
    val context = LocalContext.current
    val soundPlayer = remember { SoundEffectPlayer(context) }

    val snackbarHostState = remember { SnackbarHostState() }
    val gameState by viewModel.gameState.observeAsState()
    val roomState by viewModel.roomState.observeAsState()
    val playerType by viewModel.playerType.observeAsState()

    val showDialogWinner by viewModel.showDialogWinner.observeAsState()
    val showDialogDraw by viewModel.showDialogDraw.observeAsState()
    val showDialogLoser by viewModel.showDialogLoser.observeAsState()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when(event) {
                GameViewModel.UiEvent.SoundTap -> soundPlayer.playClick()
            }
        }
    }

    Scaffold(
        topBar = {
            CustomAppBar(
                "Tres en Raya",
                onBackAction = {
                    navController.popBackStack()
                },
                state = rememberTopAppBarState()
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(paddingValues),
        ) {
            if (gameState == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            "Eres el jugador $playerType",
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.End).padding(0.dp)
                        )

                        Text(
                            viewModel.getTurnMessage(),
                            color = MaterialTheme.colorScheme.tertiary,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.End).padding(0.dp)
                        )
                    }

                    Spacer(modifier = Modifier.size(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        //Player 1
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val glowingColor =
                                if (roomState?.roomStatus?.name != RoomStatusEnum.OPENED.name
                                    && gameState?.currentPlayerType?.name == PlayerTypeEnum.X.name
                                ) {
                                    PlayerTypeEnum.X.color
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }

                            GlowingCard(
                                modifier = Modifier
                                    .width(70.dp)
                                    .height(60.dp),
                                glowingColor = glowingColor,
                                containerColor = glowingColor,
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
                                    Text("Jugador X", fontSize = 8.sp, color = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.size(10.dp))

                            val colorOnline =
                                if (gameState?.player1?.playerStatus!!.name == PlayerStatusEnum.ONLINE.name)
                                    Color.Green
                                else
                                    Color.Gray

                            GlowingCard(
                                glowingColor = colorOnline,
                                glowingRadius = 10.dp,
                                containerColor = colorOnline
                            ) {
                                Box(modifier = Modifier.size(10.dp))
                            }
                        }

                        //VS
                        GlowingCard(
                            modifier = Modifier
                                .size(70.dp)
                                .padding(10.dp),
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

                        //Player O
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val glowingColor =
                                if (roomState?.roomStatus?.name != RoomStatusEnum.OPENED.name
                                    && gameState?.currentPlayerType?.name == PlayerTypeEnum.O.name
                                ) {
                                    PlayerTypeEnum.O.color
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }

                            GlowingCard(
                                modifier = Modifier
                                    .width(70.dp)
                                    .height(60.dp),
                                glowingColor = glowingColor,
                                containerColor = glowingColor,
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

                                    Text("Jugador O", fontSize = 8.sp, color = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.size(10.dp))

                            val colorOnline =
                                if (gameState?.player2?.playerStatus!!.name == PlayerStatusEnum.ONLINE.name)
                                    Color.Green
                                else
                                    Color.Gray

                            GlowingCard(
                                glowingColor = colorOnline,
                                glowingRadius = 10.dp,
                                containerColor = colorOnline.copy(alpha = 0.6f)
                            ) {
                                Box(modifier = Modifier.size(10.dp))
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
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(start = 20.dp, end = 20.dp, top = 20.dp)
                        ) {
                            for (row in 0..2) {
                                Row {
                                    for (col in 0..2) {
                                        val index = row * 3 + col
                                        Box(modifier = Modifier.padding(2.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(100.dp)
                                                    .border(
                                                        1.dp,
                                                        Color.White.copy(alpha = 0.5f),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        MaterialTheme.colorScheme.tertiary.copy(
                                                            alpha = 0.2f
                                                        )
                                                    )
                                                    .clickable(
                                                        enabled = roomState?.roomStatus == RoomStatusEnum.COMPLETED
                                                                //&& gameState?.currentPlayerType?.name == playerType
                                                                && gameState?.winner.isNullOrEmpty()
                                                    ) {
                                                        viewModel.onMoveMade(index)
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

                            Text(
                                text = roomState?.roomCode ?: "",
                                fontSize = 12.sp,
                                color = Color.White.copy(0.5f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }

                    /*gameState?.winner?.let { winner ->
                        if (winner.isNotEmpty()) {
                            Button(
                                onClick = { viewModel.onNewGame() },
                                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.tertiary),
                                modifier = Modifier.padding(top = 12.dp)
                            ) {
                                Text(text = "Nuevo Juego", color = Color.White)
                            }
                        }
                    }*/

                    Spacer(modifier = Modifier.size(50.dp))

                    //Alertas
                    if(showDialogWinner == true) {
                        soundPlayer.playWin()
                        MessageDialog(DialogTypeEnum.SUCCESS,"Ganador","¡Felicidades has ganado!") {
                            viewModel.closeDialogs()
                        }
                    }

                    if(showDialogDraw == true) {
                        soundPlayer.playDraw()
                        MessageDialog(DialogTypeEnum.INFO, "Empate", "El juego ha terminado en empate") {
                            viewModel.closeDialogs()
                        }
                    }

                    if(showDialogLoser == true) {
                        soundPlayer.playLose()
                        MessageDialog(DialogTypeEnum.ERROR, "Perdiste", "¡Has perdido!, inténtalo otra vez") {
                            viewModel.closeDialogs()
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GameScreenPreview() {
    TicTacToeTheme {
        GameScreen(rememberNavController())
    }
}