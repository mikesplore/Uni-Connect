package com.mike.uniadmin.uniChat

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mike.uniadmin.getMessageViewModel

@Composable
fun UsersProfile(context: Context, navController: NavController) {
    val messageViewModel = getMessageViewModel(context)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Magenta),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Settings Screen", style = TextStyle(fontSize = 24.sp))
    }
}
