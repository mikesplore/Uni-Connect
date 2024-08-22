package com.mike.uniadmin.uniChat.groupChat.groupChatComponents

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextOverflow
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
    targetGroupID: String,
    name: String,
    link: String,
    context: Context,
    onSearchClick: () -> Unit,
    onShowUsersClick: () -> Unit
) {
    BoxWithConstraints {
        // Determine screen width
        val screenWidth = maxWidth

        // Calculate adaptive sizes
        val iconSize = when {
            screenWidth < 360.dp -> 20.dp
            screenWidth < 480.dp -> 25.dp
            else -> 30.dp
        }

        val fontSize = when {
            screenWidth < 360.dp -> 14.sp
            screenWidth < 480.dp -> 16.sp
            else -> 18.sp
        }

        val spacing = when {
            screenWidth < 360.dp -> 4.dp
            screenWidth < 480.dp -> 6.dp
            else -> 8.dp
        }

        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Group Icon on the left
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(CommonComponents.secondary(), CircleShape)
                            .size(iconSize), // Dynamically adjust icon size
                        contentAlignment = Alignment.Center
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
                                style = CommonComponents.titleTextStyle(context).copy(fontSize = fontSize)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(spacing)) // Adaptive space between icon and text

                    // Group Name and User Info in the center
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = name,
                            style = CommonComponents.titleTextStyle(context).copy(fontSize = fontSize),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            },
            actions = {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        Icons.Filled.Search, contentDescription = "Search", tint = CommonComponents.textColor(),
                        modifier = Modifier.size(iconSize) // Dynamically adjust icon size
                    )
                }
                IconButton(onClick = onShowUsersClick) {
                    Icon(
                        Icons.Filled.Person, contentDescription = "Participants", tint = CommonComponents.textColor(),
                        modifier = Modifier.size(iconSize) // Dynamically adjust icon size
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = {
                    navController.navigate("uniChat") {
                        popUpTo("GroupChat/$targetGroupID") {
                            inclusive = true
                        }
                    }
                }) {
                    Icon(
                        Icons.Default.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = CommonComponents.textColor(),
                        modifier = Modifier.size(iconSize) // Dynamically adjust icon size
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = CommonComponents.primary()),
            modifier = Modifier
                .statusBarsPadding() // Adjust for status bar
                .padding(horizontal = spacing) // Adaptive padding
        )
    }
}
