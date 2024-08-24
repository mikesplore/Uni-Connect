package com.mike.uniadmin.uniChat.userChat.userChatComponents

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@Composable
fun RowDate(date: String, context: Context) {
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
                text = CC.getRelativeDate(CC.getDateFromTimeStamp(date)),
                modifier = Modifier.padding(5.dp),
                style = CC.descriptionTextStyle(context),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}