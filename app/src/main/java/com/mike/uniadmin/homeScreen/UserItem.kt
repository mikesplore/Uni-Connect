package com.mike.uniadmin.homeScreen

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mike.uniadmin.dataModel.users.UserEntity
import com.mike.uniadmin.dataModel.users.UserViewModel
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserItem(
    user: UserEntity,
    context: Context,
    navController: NavController,
    viewModel: UserViewModel
) {
    var visible by remember { mutableStateOf(false) }
    val userStates by viewModel.userStates.observeAsState(emptyMap())
    val userState = userStates[user.id]
    val signedInUser by viewModel.signedInUser.observeAsState()
    LaunchedEffect(Unit) {
        viewModel.getSignedInUser()
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = if (visible) 16.dp else 0.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier.padding(end = 10.dp)
        ) {
            val size = 50.dp
            Box(modifier = Modifier
                .border(
                    1.dp, CC.textColor(), CircleShape
                )
                .background(CC.extraColor1(), CircleShape)
                .clip(CircleShape)
                .combinedClickable(onClick = {
                    navController.navigate("chat/${user.id}")
                }, onLongClick = {
                    visible = !visible
                })
                .size(size), contentAlignment = Alignment.Center
            ) {
                if (user.profileImageLink.isNotEmpty()) {
                    AsyncImage(
                        model = user.profileImageLink,
                        contentDescription = user.firstName,
                        modifier = Modifier
                            .clip(CircleShape)
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    val name = "${user.firstName[0]}${
                        user.lastName[0]
                    }"
                    Text(
                        name, style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    )
                }
            }

            val onlineStatus by animateColorAsState(
                animationSpec = tween(500, easing = LinearEasing),
                targetValue = if (userState?.online == "online") Color.Green else if (userState?.online == "offline") Color.DarkGray else Color.Red,
                label = ""
            )
            Box(
                modifier = Modifier
                    .border(
                        1.dp, CC.extraColor1(), CircleShape
                    )
                    .size(12.dp)
                    .background(
                        onlineStatus, CircleShape
                    )
                    .align(Alignment.BottomEnd)
                    .offset(x = (-6).dp, y = (-6).dp)
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        val displayName = if(signedInUser?.id == user.id) "You" else user.firstName
        Text(
            text = displayName.let {
                if (it.length > 10) it.substring(0, 10) + "..." else it
            },
            style = CC.descriptionTextStyle(context),
            maxLines = 1
        )
        val date = if (userState?.lastDate?.let { CC.getCurrentDate(it) } == CC.getCurrentDate(
                CC.getTimeStamp())) {
            "Today at ${userState.lastTime.let { CC.getFormattedTime(it) }}"
        } else {
            "${userState?.lastDate?.let { CC.getCurrentDate(it) } ?: ""} at ${userState?.lastTime?.let { CC.getFormattedTime(it) } ?: ""}"
        }

        val state = when (userState?.online) {
            "online" -> "Online"
            "offline" -> date
            else -> "Never Online"
        }
        Spacer(modifier = Modifier.height(10.dp))
        AnimatedVisibility(visible = visible) {
            UserInfo(user = user, state, context)
        }
    }
}



@Composable
fun UserInfo(user: UserEntity, userState: String, context: Context) {
    Column(
        modifier = Modifier.fillMaxWidth(0.9f),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            user.firstName + " " + user.lastName,
            style = CC.titleTextStyle(context).copy(fontSize = 15.sp)
        )
        Text(user.id, style = CC.descriptionTextStyle(context).copy(fontSize = 15.sp))
        Text(userState, style = CC.descriptionTextStyle(context).copy(fontSize = 15.sp))

    }
}