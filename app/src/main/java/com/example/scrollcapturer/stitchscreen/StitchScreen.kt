package com.example.scrollcapturer.stitchscreen

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun StitchScreen (navController: NavController) {
    Text("STITCH ORDER PREVIEW",
        fontSize = 18.sp,
        modifier = Modifier.shadow(2.dp))
}
