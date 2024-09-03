package com.mike.uniadmin.uniChat.userChat.userChatComponents

import android.os.CountDownTimer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@Composable
fun ChatInput(
    modifier: Modifier = Modifier,
    onMessageChange: (String) -> Unit,
    sendMessage: (String) -> Unit,
    isTypingChange: (Boolean) -> Unit
) {
    var input by remember { mutableStateOf(TextFieldValue("")) }
    var typingTimer: CountDownTimer? by remember { mutableStateOf(null) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Text Field
        BasicTextField(
            value = input,
            onValueChange = { message ->
                input = message

                // Cancel any existing timer
                typingTimer?.cancel()

                // Set typing status to true
                isTypingChange(true)

                // Start a new timer to reset typing status after inactivity
                typingTimer = object : CountDownTimer(3000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() {
                        // Set typing status to false after 3 seconds of inactivity
                        isTypingChange(false)
                    }
                }.start()
            },
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
                .background(CC.secondary(), RoundedCornerShape(24.dp))
                .heightIn(min = 40.dp),
            textStyle = CC.descriptionTextStyle(),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (input.text.isEmpty()) {
                        Text(
                            text = "Message",
                            style = CC.descriptionTextStyle().copy(fontSize = 12.sp)
                        )
                    }
                    innerTextField()
                }
            },
            cursorBrush = SolidColor(CC.textColor())
        )

        // Send Button
        IconButton(
            onClick = {
                if (input.text.isNotBlank()) {
                    onMessageChange(input.text)
                    sendMessage(input.text)
                    input = TextFieldValue("") // Clear input field
                    isTypingChange(false) // Set typing status to false after sending message
                }
            },
            modifier = Modifier
                .clip(CircleShape)
                .background(CC.secondary())
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = CC.extraColor2()
            )
        }
    }
}