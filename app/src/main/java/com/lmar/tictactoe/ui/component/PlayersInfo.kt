package com.lmar.tictactoe.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lmar.tictactoe.core.enums.PlayerTypeEnum

@Composable
fun PlayersInfo(
    playerNameX: String,
    playerNameO: String,
    colorPlayerX: Color = MaterialTheme.colorScheme.primary,
    colorPlayerO: Color = MaterialTheme.colorScheme.primary,
    isOnlineActive: Boolean = false,
    onlinePlayerX: Boolean = false,
    onlinePlayerO: Boolean = false
) {
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
            GlowingCard(
                modifier = Modifier
                    .width(70.dp)
                    .height(60.dp),
                glowingColor = colorPlayerX,
                containerColor = colorPlayerX,
                cornerRadius = 8.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ShadowText(
                        text = PlayerTypeEnum.X.name,
                        textColor = Color.White,
                        shadowColor = Color.White,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                    )
                    Text(playerNameX, fontSize = 8.sp, color = Color.White, textAlign = TextAlign.Center)
                }
            }

            if(isOnlineActive) {
                Spacer(modifier = Modifier.size(10.dp))

                val colorOnlineX = if (onlinePlayerX) Color.Green else Color.Gray

                GlowingCard(
                    glowingColor = colorOnlineX,
                    glowingRadius = 10.dp,
                    containerColor = colorOnlineX
                ) {
                    Box(modifier = Modifier.size(10.dp))
                }
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
                modifier = Modifier
                    .align(Alignment.Center)
            )
        }

        //Player O
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GlowingCard(
                modifier = Modifier
                    .width(70.dp)
                    .height(60.dp),
                glowingColor = colorPlayerO,
                containerColor = colorPlayerO,
                cornerRadius = 8.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ShadowText(
                        text = PlayerTypeEnum.O.name,
                        textColor = Color.White,
                        shadowColor = Color.White,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                    )

                    Text(playerNameO, fontSize = 8.sp, color = Color.White, textAlign = TextAlign.Center)
                }
            }

            if(isOnlineActive) {
                Spacer(modifier = Modifier.size(10.dp))

                val colorOnlineO = if (onlinePlayerO) Color.Green else Color.Gray

                GlowingCard(
                    glowingColor = colorOnlineO,
                    glowingRadius = 10.dp,
                    containerColor = colorOnlineO.copy(alpha = 0.6f)
                ) {
                    Box(modifier = Modifier.size(10.dp))
                }
            }
        }
    }
}

@Preview
@Composable
private fun PlayersInfoPreview() {
    MaterialTheme {
        PlayersInfo("X", "O", isOnlineActive = true)
    }
}