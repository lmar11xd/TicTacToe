package com.lmar.tictactoe.ui.component.message_dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.lmar.tictactoe.ui.theme.ErrorColor
import com.lmar.tictactoe.ui.theme.InfoColor
import com.lmar.tictactoe.ui.theme.Shapes
import com.lmar.tictactoe.ui.theme.SuccessColor

@Composable
private fun SuccessDialog(
    title: String,
    description: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .width(300.dp)
                .height(400.dp)
        ) {
            Column(
                modifier = Modifier
                    .size(300.dp)
            ) {
                Spacer(modifier = Modifier.height(36.dp))
                Box(
                    modifier = Modifier
                        .width(300.dp)
                        .height(164.dp)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(10.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = title.uppercase(),
                            style = TextStyle(
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = description,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            /*Button(
                                onClick = onDismiss,
                                shape = Shapes.large,
                                colors = ButtonDefaults.buttonColors(ErrorColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .clip(RoundedCornerShape(5.dp))
                            ) {
                                Text(
                                    text = "Cancel",
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))*/
                            Button(
                                onClick = onDismiss,
                                shape = Shapes.large,
                                colors = ButtonDefaults.buttonColors(SuccessColor),
                                modifier = Modifier
                                    //.fillMaxWidth()
                                    //.weight(1f)
                                    .clip(RoundedCornerShape(5.dp))
                            ) {
                                Text(
                                    text = "Ok",
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
            SuccessHeader(
                modifier = Modifier
                    .size(72.dp)
                    .align(Alignment.TopCenter)
                    .border(
                        border = BorderStroke(width = 5.dp, color = Color.White),
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun ErrorDialog(
    title: String,
    description: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .width(300.dp)
                .height(400.dp)
        ) {
            Column(
                modifier = Modifier
                    .size(300.dp)
            ) {
                Spacer(modifier = Modifier.height(36.dp))
                Box(
                    modifier = Modifier
                        .width(300.dp)
                        .height(164.dp)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(10.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = title.uppercase(),
                            style = TextStyle(
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = description,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            /*Button(
                                onClick = onDismiss,
                                shape = Shapes.large,
                                colors = ButtonDefaults.buttonColors(ErrorColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .clip(RoundedCornerShape(5.dp))
                            ) {
                                Text(
                                    text = "Cancel",
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))*/
                            Button(
                                onClick = onDismiss,
                                shape = Shapes.large,
                                colors = ButtonDefaults.buttonColors(ErrorColor),
                                modifier = Modifier
                                    //.fillMaxWidth()
                                    //.weight(1f)
                                    .clip(RoundedCornerShape(5.dp))
                            ) {
                                Text(
                                    text = "Ok",
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
            ErrorHeader(
                modifier = Modifier
                    .size(72.dp)
                    .align(Alignment.TopCenter)
                    .border(
                        border = BorderStroke(width = 5.dp, color = Color.White),
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun InfoDialog(
    title: String,
    description: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .width(300.dp)
                .height(400.dp)
        ) {
            Column(
                modifier = Modifier
                    .size(300.dp)
            ) {
                Spacer(modifier = Modifier.height(36.dp))
                Box(
                    modifier = Modifier
                        .width(300.dp)
                        .height(164.dp)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(10.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = title.uppercase(),
                            style = TextStyle(
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = description,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            )
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onDismiss,
                            shape = Shapes.large,
                            colors = ButtonDefaults.buttonColors(InfoColor),
                            modifier = Modifier
                                //.fillMaxWidth()
                                .align(Alignment.End)
                                .clip(RoundedCornerShape(5.dp))
                        ) {
                            Text(
                                text = "Ok",
                                color = Color.White
                            )
                        }
                    }
                }
            }
            InfoHeader(
                modifier = Modifier
                    .size(72.dp)
                    .align(Alignment.TopCenter)
                    .border(
                        border = BorderStroke(width = 5.dp, color = Color.White),
                        shape = CircleShape
                    )
            )
        }
    }
}


@Composable
fun MessageDialog(
    type: DialogTypeEnum = DialogTypeEnum.INFO,
    title: String,
    description: String,
    onDismiss: () -> Unit
) {
    MaterialTheme {
        when(type) {
            DialogTypeEnum.SUCCESS -> {
                SuccessDialog(title, description, onDismiss)
            }
            DialogTypeEnum.ERROR -> {
                ErrorDialog(title, description, onDismiss)
            }
            DialogTypeEnum.INFO -> {
                InfoDialog(title, description, onDismiss)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SuccessDialogPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        MessageDialog(DialogTypeEnum.SUCCESS,"Success", "Hola Mundo Dialog") {}
    }
}
