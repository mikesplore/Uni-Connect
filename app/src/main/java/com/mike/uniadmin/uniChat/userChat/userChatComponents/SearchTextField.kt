package com.mike.uniadmin.uniChat.userChat.userChatComponents

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.mike.uniadmin.ui.theme.CommonComponents

@Composable
fun SearchTextField(
    searchQuery: String,
    onValueChange: (String) -> Unit,
) {
    TextField(value = searchQuery,
        onValueChange = { onValueChange(it) },
        placeholder = { Text("Search Chats") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = CommonComponents.primary(),
            unfocusedIndicatorColor = CommonComponents.textColor(),
            focusedIndicatorColor = CommonComponents.textColor(),
            unfocusedContainerColor = CommonComponents.primary(),
            focusedTextColor = CommonComponents.textColor(),
            unfocusedTextColor = CommonComponents.textColor(),
            focusedLabelColor = CommonComponents.textColor(),
            unfocusedLabelColor = CommonComponents.textColor(),
            cursorColor = CommonComponents.textColor()
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        shape = RoundedCornerShape(10.dp)
    )
}