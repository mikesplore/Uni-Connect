package com.mike.uniadmin.uniChat.userChat.userChatComponents

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.mike.uniadmin.backEnd.users.UserEntity
import com.mike.uniadmin.helperFunctions.randomColor
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarComponent(
    navController: NavController,
    name: String,
    context: Context,
    user: UserEntity,
    userState: String,
    isSearchVisible: Boolean = false,
    onValueChange: (Boolean) -> Unit,
    isTyping: Boolean
) {
    BoxWithConstraints {
        val screenWidth = maxWidth

        // Adjustments based on screen width
        val iconSize = when {
            screenWidth < 360.dp -> 20.dp
            screenWidth < 480.dp -> 25.dp
            else -> 30.dp
        }

        val textFontSize = when {
            screenWidth < 360.dp -> 14.sp
            screenWidth < 480.dp -> 16.sp
            else -> 18.sp
        }

        val spacing = when {
            screenWidth < 360.dp -> 4.dp
            screenWidth < 480.dp -> 6.dp
            else -> 8.dp
        }

        val subtitleFontSize = when {
            screenWidth < 360.dp -> 8.sp
            screenWidth < 480.dp -> 9.sp
            else -> 10.sp
        }

        val profileSize = when {
            screenWidth < 360.dp -> 40.dp
            screenWidth < 480.dp -> 45.dp
            else -> 50.dp
        }

        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = spacing)
                ) {
                    IconButton(onClick = {
                        navController.navigate("uniChat") {
                            popUpTo("chat/${user.id}") {
                                inclusive = true
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            tint = CC.textColor(),
                            modifier = Modifier.size(iconSize) // Dynamic icon size
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(randomColor.random(), CircleShape)
                            .size(profileSize) // Dynamic icon size
                    ) {
                        if (user.profileImageLink.isNotBlank()) {
                            Image(
                                painter = rememberAsyncImagePainter(user.profileImageLink),
                                contentDescription = "Profile Image",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text("${user.firstName[0]}${user.lastName[0]}",
                                style = CC.descriptionTextStyle().copy(fontSize = textFontSize, fontWeight = FontWeight.Bold))
                        }
                    }

                    Spacer(modifier = Modifier.width(spacing)) // Dynamic spacing

                    Column(
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Text(
                            name,
                            style = CC.titleTextStyle().copy(fontSize = textFontSize)
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        if (isTyping) {
                            Text(
                                "Typing...",
                                style = CC.descriptionTextStyle().copy(fontSize = subtitleFontSize)
                            )
                        } else {
                            Text(
                                userState,
                                style = CC.descriptionTextStyle().copy(fontSize = subtitleFontSize)
                            )
                        }
                    }
                }
            },
            actions = {
                IconButton(onClick = { onValueChange(!isSearchVisible) }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = CC.textColor(),
                        modifier = Modifier.size(iconSize) // Dynamic icon size
                    )
                }
                val intent = Intent(Intent.ACTION_DIAL)
                IconButton(onClick = {
                    if (user.phoneNumber.isNotEmpty()) {
                        intent.data = Uri.parse("tel:${user.phoneNumber}")
                        context.startActivity(intent)
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = CC.textColor(),
                        modifier = Modifier.size(iconSize) // Dynamic icon size
                    )
                }
            },
            modifier = Modifier
                .height(70.dp)
                .padding(horizontal = spacing), // Dynamic padding
            colors = TopAppBarDefaults.topAppBarColors(containerColor = CC.primary())
        )
    }
}


