package com.mike.uniadmin.youtubeVideos

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.model.User

@Composable
fun Videos(navController: NavController, context: Context){
    val user = FirebaseAuth.getInstance().currentUser
    val profileImageUrl = user?.photoUrl.toString()
    val currentUser by remember { mutableStateOf(User()) }



}