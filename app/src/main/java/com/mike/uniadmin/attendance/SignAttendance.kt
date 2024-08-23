package com.mike.uniadmin.attendance

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@Composable
fun SignAttendance(context: Context){
    Box(modifier = Modifier
        .background(CC.primary())
        .fillMaxSize(),
        contentAlignment = Alignment.Center){
        Text(text = "Sign Attendance", style = CC.titleTextStyle(context))

    }
}