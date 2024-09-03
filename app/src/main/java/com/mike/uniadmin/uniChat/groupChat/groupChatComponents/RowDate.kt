package com.mike.uniadmin.uniChat.groupChat.groupChatComponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@Composable
fun DateHeader(date: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .background(CC.secondary(), RoundedCornerShape(10.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp), contentAlignment = Alignment.Center
        ) {
            Text(
                text = CC.getRelativeDate(date),
                style = CC.descriptionTextStyle(),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RowText() {
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .background(
                    CC.secondary(), RoundedCornerShape(10.dp)
                )
                .clip(RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Group chats are moderated by admin.",
                modifier = Modifier.padding(5.dp),
                style = CC.descriptionTextStyle().copy(fontSize = 13.sp),
                textAlign = TextAlign.Center,
                color = CC.textColor()
            )
        }
    }
}

@Composable
fun SearchBar(
    isSearchVisible: Boolean,
    searchQuery: TextFieldValue,
    onSearchQueryChange: (TextFieldValue) -> Unit
) {
    AnimatedVisibility(
        visible = isSearchVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { -it })
    ) {
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("Search Chats") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CC.primary(),
                unfocusedIndicatorColor = CC.textColor(),
                focusedIndicatorColor = CC.secondary(),
                unfocusedContainerColor = CC.primary(),
                focusedTextColor = CC.textColor(),
                unfocusedTextColor = CC.textColor(),
                focusedLabelColor = CC.secondary(),
                unfocusedLabelColor = CC.textColor()
            ),
            shape = RoundedCornerShape(10.dp)
        )
    }
}