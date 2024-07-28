package com.mike.uniadmin.chat

import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@Composable
fun SearchGroup(navController: NavController, context: Context){
    Text("Search Screen", style = CC.titleTextStyle(context))
}