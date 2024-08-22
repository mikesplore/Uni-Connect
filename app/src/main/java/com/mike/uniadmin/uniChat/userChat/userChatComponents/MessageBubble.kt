package com.mike.uniadmin.uniChat.userChat.userChatComponents

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mike.uniadmin.backEnd.userchat.DeliveryStatus
import com.mike.uniadmin.backEnd.userchat.UserChatEntity
import com.mike.uniadmin.backEnd.userchat.UserChatViewModel
import com.mike.uniadmin.model.randomColor
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MessageBubble(
    message: UserChatEntity,
    isUser: Boolean,
    context: Context,
    userGroupChatViewModel: UserChatViewModel,
    path: String,
    senderID: String
) {
    val alignment = if (isUser) Alignment.TopEnd else Alignment.TopStart
    val bubbleShape = RoundedCornerShape(
        bottomStart = 16.dp,
        bottomEnd = 16.dp,
        topStart = if (isUser) 16.dp else 0.dp,
        topEnd = if (isUser) 0.dp else 16.dp
    )

    val senderBrush = Brush.linearGradient(
        colors = listOf(CC.extraColor1(), randomColor.random())
    )

    val receiverBrush = Brush.linearGradient(
        colors = listOf(randomColor.random(), CC.extraColor1())
    )


    val backgroundColor = if (isUser) senderBrush else receiverBrush

    var showDeleteDialog by remember { mutableStateOf(false) }


    fun deleteMessage() {
        userGroupChatViewModel.deleteMessage(message.id, path, onSuccess = {
            Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show()
        })
    }

    BoxWithConstraints(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 2.dp)
        .combinedClickable(onClick = {}, onLongClick = {
            if (message.senderID != senderID) return@combinedClickable
            showDeleteDialog = true
        }), contentAlignment = alignment
    ) {
        val maxBubbleWidth = maxWidth * 0.75f

        Row(
            modifier = Modifier.align(alignment), verticalAlignment = Alignment.CenterVertically
        ) {
            // Time on the left for the sender
            if (isUser) {
                Text(
                    text = CC.getFormattedTime(message.timeStamp),
                    style = CC.descriptionTextStyle(context),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }

            // Message bubble
            Box(
                modifier = Modifier
                    .background(backgroundColor, bubbleShape)
                    .widthIn(max = maxBubbleWidth)
                    .padding(8.dp)
            ) {
                SelectionContainer { // Wrap the Text composable with SelectionContainer
                    Text(
                        text = message.message,
                        style = CC.descriptionTextStyle(context).copy(fontSize = 12.sp)
                    )
                }
            }

            // Display the appropriate tick based on delivery status
            if (isUser) {
                DeliveryStatusIcon(deliveryStatus = message.deliveryStatus)
            }

            // Time on the right for the user
            if (!isUser) {
                Text(
                    text = CC.getFormattedTime(message.timeStamp),
                    style = CC.descriptionTextStyle(context),
                    fontSize = 11.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(containerColor = CC.primary(),
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "Delete Message", style = CC.titleTextStyle(context)) },
            text = {
                Text(
                    text = "Are you sure you want to delete this message?",
                    style = CC.descriptionTextStyle(context)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    deleteMessage()
                    showDeleteDialog = false
                }) {
                    Text("Delete", style = CC.descriptionTextStyle(context))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", style = CC.descriptionTextStyle(context))
                }
            })
    }
}

@Composable
fun DeliveryStatusIcon(
    deliveryStatus: DeliveryStatus
) {
    when (deliveryStatus) {
        DeliveryStatus.SENT -> {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Sent",
                tint = Color.Gray, // Single gray tick
                modifier = Modifier.size(16.dp)
            )
        }

        DeliveryStatus.READ -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy((-8).dp) // Slight overlap for double tick
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Delivered",
                    tint = Color.Gray, // First gray tick
                    modifier = Modifier.size(16.dp)
                )
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Delivered",
                    tint = Color.Gray, // Second gray tick
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
