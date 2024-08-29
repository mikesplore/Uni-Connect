package com.mike.uniadmin.uniChat.mainChatScreen

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.sizeIn
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
import com.mike.uniadmin.UniAdminPreferences
import com.mike.uniadmin.backEnd.userchat.UserChatsWithDetails
import com.mike.uniadmin.backEnd.users.UserEntity
import com.mike.uniadmin.helperFunctions.randomColor
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@Composable
fun UserMessageCard(
    chat: UserChatsWithDetails,
    context: Context,
    navController: NavController,

    ) {
    val messageCounter = chat.unreadCount
    val currentUserId = UniAdminPreferences.userID.value
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(85.dp)
            .clickable { navController.navigate("chat/${chat.userChat.recipientID}") }
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Use the appropriate profile image based on sender or receiver
            val profileImageUser =
                if (chat.userChat.recipientID == currentUserId) chat.sender else chat.receiver
            ProfileImage(currentUser = profileImageUser, context, navController)

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // Display sender or receiver name based on current user
                val userName =
                    if (chat.userChat.recipientID == currentUserId) "You" else chat.receiver.firstName
                Text(
                    text = userName,
                    style = CC.descriptionTextStyle(context)
                        .copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Display latest message with sender name if not sent by current user
                chat.userChat.message.let { message ->
                    val senderName =
                        if (chat.userChat.senderID != currentUserId) "${chat.sender.firstName}: " else ""
                    Text(
                        text = "$senderName${createAnnotatedMessage(createAnnotatedText(message).toString())}",
                        style = CC.descriptionTextStyle(context).copy(
                            fontSize = 14.sp,
                            color = CC.tertiary().copy(alpha = 0.8f)
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
                // Display message counter if not zero
                if (messageCounter != 0) {
                    MessageCounterBadge(count = messageCounter, context = context)
                }

                // Display timestamp
                chat.userChat.timeStamp.let {
                    Text(
                        text = CC.getRelativeTime(it),
                        style = CC.descriptionTextStyle(context).copy(fontSize = 12.sp),
                        color = CC.textColor().copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

// Extracted composable for message counter badge
@Composable
fun MessageCounterBadge(count: Int, context: Context) {
    Box(
        modifier = Modifier
            .sizeIn(minWidth = 20.dp, minHeight = 20.dp)
            .clip(CircleShape)
            .background(CC.extraColor1(), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            count.toString(),
            style = CC.descriptionTextStyle(context).copy(fontSize = 10.sp),
            modifier = Modifier.padding(2.dp)
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileImage(currentUser: UserEntity?, context: Context, navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .size(40.dp), contentAlignment = Alignment.Center
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
                .background(randomColor.random(), CircleShape)
                .fillMaxSize(),
                contentAlignment = Alignment.Center) {
                if (currentUser != null) {
                    Text(
                        (currentUser.firstName[0].toString()) + currentUser.lastName[0],
                        style = CC.descriptionTextStyle(context)
                            .copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }

    if (showDialog) {
        BasicAlertDialog(modifier = Modifier, onDismissRequest = { showDialog = false }, content = {
            if (currentUser != null) {
                AlertDialogComponent(currentUser, context, navController) {
                    showDialog = it
                }
            }
        })
    }
}

@Composable
fun AlertDialogComponent(
    user: UserEntity,
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
            if (user.profileImageLink.isNotBlank()) {
                AsyncImage(
                    model = user.profileImageLink,
                    contentDescription = "Profile Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .background(CC.extraColor1())
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile Image",
                        tint = CC.textColor()
                    )
                }
            }
            Row(
                modifier = Modifier
                    .height(30.dp)
                    .background(
                        CC
                            .primary()
                            .copy(0.5f)
                    )
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    user.firstName + " " + user.lastName,
                    style = CC.descriptionTextStyle(context)
                        .copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 10.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CC.primary())
                    .align(Alignment.BottomCenter),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {

                IconButton(onClick = {
                    navController.navigate("chat/${user.id}")
                    onShowDialogChange(false)
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Message,
                        contentDescription = "Chat",
                        tint = CC.textColor()
                    )
                }
                IconButton(onClick = {
                    val phoneNumber = user.phoneNumber
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                    context.startActivity(intent)
                    onShowDialogChange(false)
                }) {
                    Icon(
                        imageVector = Icons.Filled.Call,
                        contentDescription = "Call",
                        tint = CC.textColor()
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
                SpanStyle(color = CC.textColor().copy(alpha = 0.5f)), startIndex, emojiIndex
            ) // Apply color to non-emoji text

            append(emoji)

            startIndex = matchResult.range.last + 1
        }

        append(message.substring(startIndex))
        addStyle(
            SpanStyle(color = CC.secondary().copy(alpha = 0.5f)), startIndex, message.length
        ) // Apply color to non-emoji text
    }
}
