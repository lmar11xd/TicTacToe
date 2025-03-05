package com.lmar.tictactoe.ui.screen.home

import android.annotation.SuppressLint
import android.graphics.drawable.Icon
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lmar.tictactoe.R
import com.lmar.tictactoe.core.enums.GameLevelEnum
import com.lmar.tictactoe.ui.component.ShadowText
import com.lmar.tictactoe.ui.screen.ScreenRoutes
import com.lmar.tictactoe.ui.theme.TicTacToeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val snackbarHostState = remember { SnackbarHostState() }

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ShadowText(
                text = "Tres en Raya",
                fontFamily = MaterialTheme.typography.displayLarge.fontFamily!!,
                fontSize = 32.sp,
                textColor = MaterialTheme.colorScheme.primary,
                shadowColor = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Image(
                painter = painterResource(R.drawable.tictactoe),
                contentDescription = "Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    showBottomSheet = true
                },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.tertiary),
                modifier = Modifier.width(200.dp)
            ) {
                Text("Un Jugador")
            }

            Spacer(modifier = Modifier.size(4.dp))

            Button(
                onClick = {
                    navController.navigate(ScreenRoutes.RoomScreen.route)
                },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                modifier = Modifier.width(200.dp)
            ) {
                Text("Multijugador")
            }

            Spacer(modifier = Modifier.size(4.dp))
        }

        if(showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false},
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ShadowText(
                        text = "Selecciona Nivel",
                        fontFamily = MaterialTheme.typography.displayLarge.fontFamily!!,
                        fontSize = 24.sp,
                        textColor = MaterialTheme.colorScheme.primary,
                        shadowColor = MaterialTheme.colorScheme.primary
                    )

                    BottomSheetItem(title = "Fácil") {
                        showBottomSheet = false
                        navController.navigate(ScreenRoutes.SingleGameScreen.route + "?level=${GameLevelEnum.EASY.name}")
                    }

                    BottomSheetItem(title = "Normal") {
                        showBottomSheet = false
                        navController.navigate(ScreenRoutes.SingleGameScreen.route + "?level=${GameLevelEnum.MEDIUM.name}")
                    }

                    BottomSheetItem(title = "Difícil") {
                        showBottomSheet = false
                        navController.navigate(ScreenRoutes.SingleGameScreen.route + "?level=${GameLevelEnum.HARD.name}")
                    }
                }
            }
        }
    }
}

@Composable
fun BottomSheetItem(
    title: String,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        if(icon != null) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
        }
        Text(text = title, color = MaterialTheme.colorScheme.tertiary, fontSize = 18.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    TicTacToeTheme {
        HomeScreen(rememberNavController())
    }
}