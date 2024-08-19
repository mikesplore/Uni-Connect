package com.mike.uniadmin.uniChat.groupChat.groupChatComponents

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mike.uniadmin.R
import com.mike.uniadmin.ui.theme.CommonComponents

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopAppBar(
    navController: NavController,
    name: String,
    link: String,
    context: Context,
    onSearchClick: () -> Unit,
    onShowUsersClick: () -> Unit
) {
    TopAppBar(title = {
        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
        ) {
            // Group Icon on the left
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CommonComponents.secondary(), CircleShape)
                    .size(50.dp), contentAlignment = Alignment.Center
            ) {
                if (link.isNotBlank()) {
                    AsyncImage(
                        model = link,
                        contentDescription = "Group Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.logo),
                        error = painterResource(id = R.drawable.logo)
                    )
                } else {
                    Text(
                        text = name[0].toString(),
                        style = CommonComponents.titleTextStyle(context).copy(fontSize = 18.sp)
                    )
                }

            }

            Spacer(modifier = Modifier.width(8.dp)) // Space between icon and text

            // Group Name and User Info in the center
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = name,
                    style = CommonComponents.titleTextStyle(context).copy(fontSize = 18.sp),
                    maxLines = 1
                )
            }
        }
    },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    Icons.Filled.Search, contentDescription = "Search", tint = CommonComponents.textColor()
                )
            }
            IconButton(onClick = { onShowUsersClick() }) {
                Icon(Icons.Filled.Person, "Participants", tint = CommonComponents.textColor())
            }
        },
        navigationIcon = {
            IconButton(onClick = {
                navController.navigate("uniChat") {
                    popUpTo("GroupChat/${GroupDetails.groupName.value}") {
                        inclusive = true
                    }
                }
            }) {
                Icon(
                    Icons.Default.ArrowBackIosNew,
                    contentDescription = "Back",
                    tint = CommonComponents.textColor()
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = CommonComponents.primary()),
        modifier = Modifier.statusBarsPadding() // Adjust for status bar
    )
}