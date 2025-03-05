package com.lmar.tictactoe.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lmar.tictactoe.core.enums.GameLevelEnum
import com.lmar.tictactoe.ui.theme.DangerColor
import com.lmar.tictactoe.ui.theme.ErrorColor
import com.lmar.tictactoe.ui.theme.SuccessColor

@Composable
fun LevelBadge(level: GameLevelEnum) {
    val levelName = when (level) {
        GameLevelEnum.EASY -> "Fácil"
        GameLevelEnum.MEDIUM -> "Normal"
        GameLevelEnum.HARD -> "Difícil"
    }

    val backgroundColor = when (level) {
        GameLevelEnum.EASY -> SuccessColor
        GameLevelEnum.MEDIUM -> DangerColor
        GameLevelEnum.HARD -> ErrorColor
    }

    Box(
        modifier = Modifier
            .background(backgroundColor, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = levelName.uppercase(),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBadges() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        LevelBadge(GameLevelEnum.EASY)
        LevelBadge(GameLevelEnum.MEDIUM)
        LevelBadge(GameLevelEnum.HARD)
    }
}