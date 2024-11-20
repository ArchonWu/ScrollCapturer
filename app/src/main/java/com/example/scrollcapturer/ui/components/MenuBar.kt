package com.example.scrollcapturer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MenuBar(buttons: List<@Composable () -> Unit>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.secondary)
            .padding(0.dp)
    ) {
        for (button in buttons) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)             // each box has the same weight, so equal width for each button
                    .wrapContentHeight(),
//                    .border(1.dp, color = Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                button()
            }
        }
    }
}