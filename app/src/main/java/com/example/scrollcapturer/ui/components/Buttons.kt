package com.example.scrollcapturer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun StyledButton(text: String, onClick: () -> Unit, imageVector: ImageVector) {
    Button(
        shape = RoundedCornerShape(2.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        contentPadding = PaddingValues(4.dp),
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
//                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 2.dp)
            )
//            Image(
//                painter = painterResource(id = imageVector),
//                contentDescription = null,
//                modifier = Modifier.padding(bottom = 2.dp)
//            )
            Text(
                text,
                maxLines = 1
            )
        }
    }
}