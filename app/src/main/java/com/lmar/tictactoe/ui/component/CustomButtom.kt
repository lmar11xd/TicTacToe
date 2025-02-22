package com.lmar.tictactoe.ui.component

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color = Color(0xFF6200EE), // Color por defecto (morado)
    contentColor: Color = Color.White // Color del texto
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(12.dp), // Bordes redondeados
        modifier = Modifier.height(50.dp) // Altura fija
    ) {
        Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}