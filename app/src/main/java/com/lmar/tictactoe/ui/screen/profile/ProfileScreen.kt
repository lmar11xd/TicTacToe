package com.lmar.tictactoe.ui.screen.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.lmar.tictactoe.R
import com.lmar.tictactoe.ui.component.CustomAppBar
import com.lmar.tictactoe.ui.component.GlowingCard
import com.lmar.tictactoe.ui.component.HeadingTextComponent
import com.lmar.tictactoe.ui.component.ImageCircle
import com.lmar.tictactoe.ui.component.NormalTextComponent
import com.lmar.tictactoe.ui.screen.AuthState
import com.lmar.tictactoe.ui.screen.AuthViewModel
import com.lmar.tictactoe.ui.screen.ScreenRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val authState = authViewModel.authState.observeAsState()
    val user = profileViewModel.user.observeAsState()

    LaunchedEffect(authState.value) {
        when(authState.value) {
            AuthState.Unauthenticated -> navController.navigate(ScreenRoutes.HomeScreen)
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            CustomAppBar(
                "Perfil",
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 16.dp),
            ) {
                Spacer(modifier = Modifier.size(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (authState.value == AuthState.Authenticated) {
                        //Información del Usuario
                        GlowingCard(
                            modifier = Modifier
                                .size(150.dp)
                                .padding(5.dp),
                            glowingColor = MaterialTheme.colorScheme.tertiary,
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            cornerRadius = Int.MAX_VALUE.dp
                        ) {
                            ImageCircle(
                                "https://images.freeimages.com/365/images/istock/previews/1009/100996291-male-avatar-profile-picture-vector.jpg",
                                modifier = Modifier.size(150.dp)
                            )
                        }

                        Spacer(modifier = Modifier.size(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically, // Alinea los elementos verticalmente
                            horizontalArrangement = Arrangement.SpaceBetween // Distribuye los elementos en la fila
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                HeadingTextComponent(
                                    value = user.value?.names ?: "",
                                    textColor = MaterialTheme.colorScheme.primary,
                                    fontSize = 16.sp
                                )

                                NormalTextComponent(
                                    value = user.value?.email ?: "",
                                    fontSize = 14.sp
                                )
                            }

                            IconButton(
                                onClick = { navController.navigate(ScreenRoutes.EditUserScreen.route) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    } else if (authState.value == AuthState.Unauthenticated) {
                        //Default
                        GlowingCard(
                            modifier = Modifier
                                .size(150.dp)
                                .padding(5.dp),
                            glowingColor = MaterialTheme.colorScheme.tertiary,
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            cornerRadius = Int.MAX_VALUE.dp
                        ) {
                            Image(
                                painter = painterResource(R.drawable.default_avatar),
                                contentDescription = "Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(CircleShape)
                                    .border(5.dp, MaterialTheme.colorScheme.tertiary, CircleShape),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.size(18.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {

                    Spacer(modifier = Modifier.size(4.dp))

                    if (authState.value == AuthState.Authenticated) {
                        OutlinedButton(
                            onClick = { authViewModel.signout() },
                            modifier = Modifier.width(200.dp)
                        ) {
                            Text("Cerrar Sesión")
                        }
                    } else if (authState.value == AuthState.Unauthenticated) {
                        Button(
                            onClick = {
                                navController.navigate(ScreenRoutes.LoginScreen.route)
                            },
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.tertiary),
                            modifier = Modifier.width(200.dp)
                        ) {
                            Text("Iniciar Sesión")
                        }

                        Spacer(modifier = Modifier.size(4.dp))

                        Button(
                            onClick = {
                                navController.navigate(ScreenRoutes.SignUpScreen.route)
                            },
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.width(200.dp)
                        ) {
                            Text("Registrarse")
                        }
                    }
                }

                Spacer(modifier = Modifier.size(4.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    ProfileScreen(rememberNavController())
}