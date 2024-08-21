package com.mike.uniadmin.uniChat.groupChat.groupChatComponents

import android.content.Context
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
import com.mike.uniadmin.ui.theme.CommonComponents

@Composable
fun DateHeader(context: Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .background(CommonComponents.secondary(), RoundedCornerShape(10.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp), contentAlignment = Alignment.Center
        ) {
            Text(
                text = CommonComponents.getRelativeDate(
                    CommonComponents.getCurrentDate(
                        CommonComponents.getTimeStamp())),
                style = CommonComponents.descriptionTextStyle(context),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
 fun RowText(context: Context) {
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .background(
                    CommonComponents.secondary(), RoundedCornerShape(10.dp)
                )
                .clip(RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Group chats are moderated by admin.",
                modifier = Modifier.padding(5.dp),
                style = CommonComponents.descriptionTextStyle(context).copy(fontSize = 13.sp),
                textAlign = TextAlign.Center,
                color = CommonComponents.textColor()
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
    AnimatedVisibility(visible = isSearchVisible,
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
                focusedContainerColor = CommonComponents.primary(),
                unfocusedIndicatorColor = CommonComponents.textColor(),
                focusedIndicatorColor = CommonComponents.secondary(),
                unfocusedContainerColor = CommonComponents.primary(),
                focusedTextColor = CommonComponents.textColor(),
                unfocusedTextColor = CommonComponents.textColor(),
                focusedLabelColor = CommonComponents.secondary(),
                unfocusedLabelColor = CommonComponents.textColor()
            ),
            shape = RoundedCornerShape(10.dp)
        )
    }
}