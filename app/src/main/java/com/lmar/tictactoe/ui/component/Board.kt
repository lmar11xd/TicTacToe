package com.lmar.tictactoe.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lmar.tictactoe.core.enums.PlayerTypeEnum

@Composable
fun Board(
    board: MutableList<MutableList<String>>,
    roomCode: String = "",
    onCellClick: (Int, Int) -> Unit
) {
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
                        Cell(board[row][col]) { onCellClick(row, col) }
                    }
                }
            }

            Text(
                text = roomCode,
                fontSize = 12.sp,
                color = Color.White.copy(0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun Cell(value: String, onClick: () -> Unit) {
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
                .clickable(enabled = value.isEmpty(), onClick = onClick)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            ShadowText(
                text = value,
                fontSize = 48.sp,
                textColor =
                if (value == PlayerTypeEnum.X.name)
                    PlayerTypeEnum.X.color
                else
                    PlayerTypeEnum.O.color,
                shadowColor = Color.White,
                modifier = Modifier.animateContentSize()
            )
        }
    }
}

fun makeMove(row: Int, col: Int) {}

@Preview(showBackground = true)
@Composable
fun BoardPreview() {
    val board: MutableList<MutableList<String>> = mutableListOf(
        MutableList(3) {"X"},
        MutableList(3) {""},
        MutableList(3) {"O"}
    )

    MaterialTheme {
        Board(
            board,
            roomCode = "123456",
            onCellClick = { i, j ->
                makeMove(i,j)
            }
        )
    }
}