package com.mike.uniadmin.uniChat.groupChat.groupChatComponents

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mike.uniadmin.backEnd.groupchat.ChatEntity
import com.mike.uniadmin.backEnd.groupchat.ChatViewModel
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@Composable
fun MessageInputRow(
    message: String, onMessageChange: (String) -> Unit, onSendClick: () -> Unit, context: Context
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .imePadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = message,
            onValueChange = onMessageChange,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp) // Add padding to the right
                .background(CC.secondary(), RoundedCornerShape(24.dp)) // Use surface color
                .heightIn(min = 40.dp), // Minimum height
            textStyle = CC.descriptionTextStyle(context),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.padding(16.dp), // Increased padding
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (message.isEmpty()) {
                        Text(
                            text = "Message",
                            style = CC.descriptionTextStyle(context).copy(fontSize = 12.sp)
                        )
                    }
                    innerTextField()
                }
            },
            cursorBrush = SolidColor(CC.textColor())
        )
        IconButton(
            onClick = {
                if (message.isNotBlank()) { // Check for non-blank messages
                    onMessageChange(message)
                    onSendClick()
                }
            }, modifier = Modifier
                .clip(CircleShape) // Circular button
                .background(CC.secondary()) // Button background color
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = CC.extraColor2() // Icon color on secondary background
            )
        }
    }
}

fun sendMessage(
    chat: ChatEntity, viewModel: ChatViewModel, path: String
) {
    viewModel.saveChat(chat, path) {}
}