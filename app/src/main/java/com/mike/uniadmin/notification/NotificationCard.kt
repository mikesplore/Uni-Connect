package com.mike.uniadmin.notification

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mike.uniadmin.R
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import com.mike.uniadmin.CommonComponents as CC


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NotificationCard(
    title: String, message: String, visibleState: MutableState<Boolean>, context: Context
) {
    val swipeableState = rememberSwipeableState(initialValue = 0)
    val sizePx = with(LocalDensity.current) { 300.dp.toPx() }
    val anchors = mapOf(0f to 0, sizePx to 1) // Maps anchor points (offsets) to states

    if (visibleState.value) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .offset { IntOffset(swipeableState.offset.value.roundToInt(), 0) }
            .background(CC.primary(), shape = RoundedCornerShape(8.dp))
            .padding(8.dp)
            .swipeable(
                state = swipeableState,
                anchors = anchors,
                thresholds = { _, _ -> FractionalThreshold(0.3f) },
                orientation = Orientation.Horizontal
            ), contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.padding(4.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.student),
                    contentDescription = "image",
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        style = CC.descriptionTextStyle(context),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    var isExpanded by remember { mutableStateOf(false) } // State to track expansion

                    Text(
                        text = if (isExpanded || message.length <= 20) message else "${
                            message.take(
                                17
                            )
                        }... Read more",
                        style = CC.descriptionTextStyle(context),
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .clickable(enabled = message.length > 20) { isExpanded = !isExpanded },
                        maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                        overflow = if (isExpanded) TextOverflow.Clip else TextOverflow.Ellipsis
                    )

                }
            }
        }

        // Hide the notification after 3 seconds
        LaunchedEffect(swipeableState.currentValue) {
            if (swipeableState.currentValue == 0) {
                delay(3000) // Delay for 3 seconds
                visibleState.value = false
            } else if (swipeableState.currentValue == 1) {
                visibleState.value = false
            }
        }
    }
}

@Preview(showBackground = false)
@Composable
fun NotificationCardPreview() {
    NotificationCard(title = "Title of the notification",
        message = "This is the announcement that will show up on the screen",
        visibleState = remember { mutableStateOf(true) }, context = LocalContext.current)
}


