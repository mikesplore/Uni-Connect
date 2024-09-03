package com.mike.uniadmin.uniChat.groupChat.groupChatComponents

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mike.uniadmin.R
import com.mike.uniadmin.backEnd.groupchat.GroupChatEntityWithDetails
import com.mike.uniadmin.helperFunctions.randomColor
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ChatBubble(
    chat: GroupChatEntityWithDetails, isUser: Boolean, navController: NavController
) {
    val alignment = if (isUser) Alignment.TopEnd else Alignment.TopStart
    val bubbleShape = RoundedCornerShape(
        bottomStart = 16.dp,
        bottomEnd = 16.dp,
        topStart = if (isUser) 16.dp else 0.dp,
        topEnd = if (isUser) 0.dp else 16.dp
    )

    val senderBrush = Brush.linearGradient(
        colors = listOf(CC.extraColor1(), CC.extraColor2())
    )
    val receiverBrush = Brush.linearGradient(
        colors = listOf(CC.extraColor2(), CC.extraColor1())
    )
    val backgroundColor = if (isUser) senderBrush else receiverBrush

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = alignment
    ) {
        val maxBubbleWidth = maxWidth * 0.75f
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!isUser) {

                Box(modifier = Modifier
                    .clickable {
                        navController.navigate("chat/${chat.groupChat.senderID}") {
                            popUpTo("chat/${chat.groupChat.senderID}") {
                                inclusive = true
                            }
                        }
                    }
                    .size(35.dp)
                    .clip(CircleShape)
                    .background(randomColor.random(), CircleShape)
                    .padding(1.dp), contentAlignment = Alignment.Center) {
                    if (chat.senderProfileImageLink.isNotBlank()) {
                        AsyncImage(
                            model = chat.senderProfileImageLink,
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .clip(CircleShape)
                                .fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.logo),
                            error = painterResource(id = R.drawable.logo)
                        )
                    } else {
                        Text(
                            text = chat.senderName[0].toString(),
                            style = CC.titleTextStyle().copy(fontSize = 18.sp)
                        )
                    }
                }
            }
            if (isUser) {
                Text(
                    text = CC.getFormattedTime(chat.groupChat.date),
                    style = CC.descriptionTextStyle(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(end = 8.dp)
                )
            }

            Box(
                modifier = Modifier
                    .background(backgroundColor, bubbleShape)
                    .widthIn(max = maxBubbleWidth)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Column {
                    if (!isUser) {
                        Text(
                            text = chat.senderName,
                            style = CC.descriptionTextStyle().copy(fontSize = 13.sp),
                            fontWeight = FontWeight.Bold,
                            color = randomColor.random()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    SelectionContainer {
                        Text(
                            text = chat.groupChat.message,
                            style = CC.descriptionTextStyle().copy(fontSize = 12.sp)
                        )
                    }
                }
            }
            if (!isUser) {
                Text(
                    text = CC.getFormattedTime(chat.groupChat.date),
                    style = CC.descriptionTextStyle(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 8.dp)
                )
            }
        }
    }
}