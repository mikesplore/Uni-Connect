package com.mike.uniadmin.uniChat.mainChatScreen

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mike.uniadmin.dataModel.userchat.MessageEntity
import com.mike.uniadmin.dataModel.users.UserEntity
import com.mike.uniadmin.dataModel.users.UserStateEntity
import com.mike.uniadmin.dataModel.users.UserViewModel
import com.mike.uniadmin.ui.theme.CommonComponents

@Composable
fun UserMessageCard(
    userEntity: UserEntity,
    latestMessage: MessageEntity?,
    userState: UserStateEntity?,
    context: Context,
    userViewModel: UserViewModel,
    navController: NavController,
    messageCounter: Int,
) {
    val currentUser by userViewModel.user.observeAsState()

    Card(modifier = Modifier
        .fillMaxWidth()
        .height(85.dp)
        .clickable { navController.navigate("chat/${userEntity.id}") }
        .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
        )) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { },
                modifier = Modifier
                    .border(1.dp, CommonComponents.secondary(), CircleShape)
                    .clip(CircleShape)
                    .size(50.dp)
            ) {
                ProfileImage(currentUser = userEntity, context, navController)
            }

            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row {
                    Text(
                        text = if (userEntity.id == currentUser?.id) "You" else userEntity.firstName,
                        style = CommonComponents.descriptionTextStyle(context)
                            .copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    if (messageCounter.toString() != "0") {
                        Box(
                            modifier = Modifier.background(
                                CommonComponents.secondary(),
                                CircleShape
                            )
                        ) {
                            Text(
                                messageCounter.toString(),
                                style = CommonComponents.descriptionTextStyle(context)
                                    .copy(fontSize = 10.sp),
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }
                }

                latestMessage?.message?.let {
                    val senderName = if (latestMessage.recipientID == userEntity.id) "You: " else ""

                    Text(
                        text = "$senderName ${createAnnotatedMessage(createAnnotatedText(it).toString())}",
                        style = CommonComponents.descriptionTextStyle(context).copy(
                                fontSize = 14.sp,
                                color = CommonComponents.extraColor2().copy(alpha = 0.8f)
                            ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

            }

            Column(
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                if (userState == null) {
                    Text(
                        text = "Never Online",
                        style = CommonComponents.descriptionTextStyle(context)
                            .copy(fontSize = 10.sp),
                        color = Color.Gray // Or any color you prefer for "Never Online"
                    )
                } else {
                    userState.let { status ->
                        val textColor = if (status.online == "online") Color.Green else Color.Red
                        Text(
                            text = when {
                                status.online == "online" -> "Online"
                                else -> "${
                                    CommonComponents.getRelativeDate(
                                        CommonComponents.getCurrentDate(
                                            status.lastDate
                                        )
                                    )
                                } at ${
                                    CommonComponents.getFormattedTime(
                                        status.lastTime
                                    )
                                }"
                            },
                            style = CommonComponents.descriptionTextStyle(context)
                                .copy(fontSize = 10.sp),
                            color = textColor
                        )
                    }
                }


                latestMessage?.timeStamp?.let {
                    Text(
                        text = CommonComponents.getRelativeTime(it),
                        style = CommonComponents.descriptionTextStyle(context)
                            .copy(fontSize = 12.sp),
                        color = CommonComponents.textColor().copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileImage(currentUser: UserEntity?, context: Context, navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        if (currentUser?.profileImageLink?.isNotBlank() == true) {
            AsyncImage(
                model = currentUser.profileImageLink,
                contentDescription = "Profile Image",
                modifier = Modifier
                    .clip(CircleShape)
                    .fillMaxSize()
                    .clickable { showDialog = true }, // Make the image clickable
                contentScale = ContentScale.Crop
            )
        } else {
            Box(modifier = Modifier
                .clickable { showDialog = true }
                .background(CommonComponents.extraColor1())
                .fillMaxSize(),
                contentAlignment = Alignment.Center) {
                if (currentUser != null) {
                    Text(
                        (currentUser.firstName[0].toString()) + currentUser.lastName[0],
                        style = CommonComponents.descriptionTextStyle(context)
                            .copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }

    if (showDialog) {
        BasicAlertDialog(modifier = Modifier, onDismissRequest = { showDialog = false }, content = {
            AlertDialogComponent(currentUser, context, navController) {
                showDialog = it
            }
        })
    }
}

@Composable
fun AlertDialogComponent(
    currentUser: UserEntity?,
    context: Context,
    navController: NavController,
    onShowDialogChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(10.dp)
            .width(200.dp)
            .height(250.dp)
            .background(Color.Transparent)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (currentUser?.profileImageLink?.isNotBlank() == true) {
                AsyncImage(
                    model = currentUser.profileImageLink,
                    contentDescription = "Profile Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .background(CommonComponents.extraColor1())
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile Image",
                        tint = CommonComponents.textColor()
                    )
                }
            }
            Row(
                modifier = Modifier
                    .height(30.dp)
                    .background(
                        CommonComponents
                            .primary()
                            .copy(0.5f)
                    )
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                verticalAlignment = Alignment.CenterVertically
            ) {
                currentUser?.let { user ->
                    Text(
                        "${user.firstName} ${user.lastName}",
                        style = CommonComponents.descriptionTextStyle(context)
                            .copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CommonComponents.primary())
                    .align(Alignment.BottomCenter),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {

                IconButton(onClick = {
                    navController.navigate("chat/${currentUser?.id}")
                    onShowDialogChange(false)
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Message,
                        contentDescription = "Chat",
                        tint = CommonComponents.textColor() // Use a custom color from your theme
                    )
                }
                IconButton(onClick = {
                    val phoneNumber = currentUser?.phoneNumber ?: "" // Handle null phone number
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                    context.startActivity(intent)
                    onShowDialogChange(false)
                }) {
                    Icon(
                        imageVector = Icons.Filled.Call,
                        contentDescription = "Call",
                        tint = CommonComponents.textColor() // Use a custom color from your theme
                    )
                }
            }
        }
    }
}


// Function to create AnnotatedString
fun createAnnotatedText(message: String): AnnotatedString {
    return AnnotatedString.Builder().apply {
        append(message)
    }.toAnnotatedString()
}


@Composable
fun createAnnotatedMessage(message: String): AnnotatedString {
    val emojiRegex = Regex("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]")

    return buildAnnotatedString {
        var startIndex = 0
        emojiRegex.findAll(message).forEach { matchResult ->
            val emoji = matchResult.value
            val emojiIndex = matchResult.range.first

            append(message.substring(startIndex, emojiIndex))
            addStyle(
                SpanStyle(color = CommonComponents.textColor().copy(alpha = 0.5f)), startIndex, emojiIndex
            ) // Apply color to non-emoji text

            append(emoji)

            startIndex = matchResult.range.last + 1
        }

        append(message.substring(startIndex))
        addStyle(
            SpanStyle(color = CommonComponents.secondary().copy(alpha = 0.5f)), startIndex, message.length
        ) // Apply color to non-emoji text
    }
}