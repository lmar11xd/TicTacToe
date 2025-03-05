package com.lmar.tictactoe.ui.screen.single_game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lmar.tictactoe.core.enums.GameLevelEnum
import com.lmar.tictactoe.core.enums.GameStatusEnum
import com.lmar.tictactoe.core.enums.PlayerTypeEnum
import com.lmar.tictactoe.feature.sounds.SoundEffectPlayer
import com.lmar.tictactoe.ui.component.Board
import com.lmar.tictactoe.ui.component.CustomAppBar
import com.lmar.tictactoe.ui.component.LevelBadge
import com.lmar.tictactoe.ui.component.PlayersInfo
import com.lmar.tictactoe.ui.component.message_dialog.DialogTypeEnum
import com.lmar.tictactoe.ui.component.message_dialog.MessageDialog
import com.lmar.tictactoe.ui.theme.TicTacToeTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleGameScreen(
    navController: NavController,
    viewModel: SingleGameViewModel = viewModel()
) {
    val context = LocalContext.current

    val soundPlayer = remember { SoundEffectPlayer(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    val gameState by viewModel.gameState.observeAsState()
    val showDialogWinner by viewModel.showDialogWinner.observeAsState()
    val showDialogDraw by viewModel.showDialogDraw.observeAsState()
    val showDialogLoser by viewModel.showDialogLoser.observeAsState()

    val winCells by viewModel.winCells.observeAsState()
    val isBoardDisabled by viewModel.isBoardDisabled.collectAsState()
    val turnMessage by viewModel.turnMessage.observeAsState()

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {// Se ejecuta solo una vez
        coroutineScope.launch {
            viewModel.eventFlow.collectLatest { event ->
                when(event) {
                    SingleGameViewModel.UiEvent.SoundTap -> soundPlayer.playClick()
                }
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

                        LevelBadge(gameState?.level ?: GameLevelEnum.EASY)

                        Text(
                            turnMessage ?: "",
                            color = MaterialTheme.colorScheme.tertiary,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(0.dp)
                        )
                    }

                    val glowingColorX =
                        if (gameState?.currentPlayerType == PlayerTypeEnum.X) {
                            PlayerTypeEnum.X.color
                        } else {
                            MaterialTheme.colorScheme.primary
                        }

                    val glowingColorO =
                        if (gameState?.currentPlayerType == PlayerTypeEnum.O) {
                            PlayerTypeEnum.O.color
                        } else {
                            MaterialTheme.colorScheme.primary
                        }

                    PlayersInfo(
                        "Jugador X",
                        "Computadora",
                        glowingColorX,
                        glowingColorO
                    )

                    Box(modifier = Modifier.height(16.dp))

                    // Tablero de juego con animación y bordes redondeados
                    gameState?.board?.let {
                        Board(
                            it,
                            winCells = winCells ?: emptyList(),
                            onCellClick = if (isBoardDisabled) null else viewModel::onPlayerMove
                        )
                    }

                    if(gameState?.gameStatus == GameStatusEnum.FINISHED) {
                        Button(
                            onClick = { gameState?.level?.let { viewModel.createNewGame(it) } },
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.tertiary),
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            Text(text = "Nuevo Juego", color = Color.White)
                        }
                    }

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
private fun SingleGameScreenPreview() {
    TicTacToeTheme {
        SingleGameScreen(rememberNavController())
    }
}