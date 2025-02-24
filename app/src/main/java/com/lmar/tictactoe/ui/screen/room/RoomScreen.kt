package com.lmar.tictactoe.ui.screen.room

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lmar.tictactoe.ui.component.CustomAppBar
import com.lmar.tictactoe.ui.component.CustomTextField
import com.lmar.tictactoe.ui.component.GlowingCard
import com.lmar.tictactoe.ui.screen.ScreenRoutes
import com.lmar.tictactoe.ui.theme.TicTacToeTheme

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomScreen(
    navController: NavController,
    viewModel: RoomViewModel = viewModel(),
) {
    val snackbarHostState = remember { SnackbarHostState() }

    var code by remember { mutableStateOf("") }
    var validationMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CustomAppBar(
                "Multijugador",
                onBackAction = {
                navController.popBackStack()
            },
                state = rememberTopAppBarState()
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.size(100.dp))

                GlowingCard(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    glowingColor = MaterialTheme.colorScheme.tertiary,
                    glowingRadius = 30.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 30.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Unirse a Partida",
                            fontWeight = FontWeight.Bold,
                            fontFamily = MaterialTheme.typography.displayMedium.fontFamily,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.size(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CustomTextField(
                                value = code,
                                onValueChange = { code = it },
                                keyboardType = KeyboardType.Number,
                                label = "Código de Partida",
                                modifier = Modifier.weight(1f)
                            )

                            Button(
                                onClick = {
                                    if(code.isEmpty()) {
                                        validationMessage = ""
                                    } else {
                                        viewModel.searchRoomByCode(code) { valid, roomId ->
                                            if (valid) {
                                                navController.navigate(
                                                    ScreenRoutes.GameScreen.route + "?roomId=$roomId&playerType=O"
                                                )
                                            } else {
                                                validationMessage = "¡Partida no encontrada!"
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.tertiary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.width(60.dp)
                            ) {
                                Text("Ir")
                            }
                        }

                        Text(
                            validationMessage,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontSize = 9.sp
                        )
                    }
                }

                GlowingCard(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    glowingColor = MaterialTheme.colorScheme.primary,
                    glowingRadius = 30.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 30.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Crear Partida",
                            fontWeight = FontWeight.Bold,
                            fontFamily = MaterialTheme.typography.displayMedium.fontFamily,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.size(10.dp))

                        Button(
                            onClick = {
                                viewModel.createNewRoom { roomId ->
                                    navController.navigate(
                                        ScreenRoutes.GameScreen.route + "?roomId=$roomId&playerType=X"
                                    )
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Text("Jugar")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RoomScreenPreview() {
    TicTacToeTheme {
        RoomScreen(rememberNavController())
    }
}