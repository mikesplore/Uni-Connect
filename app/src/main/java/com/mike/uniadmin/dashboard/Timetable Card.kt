package com.mike.uniadmin.dashboard

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mike.uniadmin.backEnd.moduleContent.moduleTimetable.ModuleTimetable
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ModuleTimetableCard(timetable: ModuleTimetable) {
    val today = CC.currentDay()
    BoxWithConstraints(
        modifier = Modifier
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(8.dp),
                )
                .background(CC.surfaceContainer())
                .fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start,
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Module Name
            Text(
                text = timetable.moduleName,
                style = CC.titleTextStyle().copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Day and Time
            Row(
                modifier = Modifier.padding(start = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = "Day",
                    tint = CC.tertiary(),
                    modifier = Modifier.size(18.dp)
                )
                if (timetable.day == today) {
                    Text(
                        text = "Today, ${timetable.startTime} - ${timetable.endTime}",
                        style = CC.descriptionTextStyle(),
                        color = CC.textColor().copy(alpha = 0.7f)
                    )

                } else {
                    Text(
                        text = "${timetable.day}, ${timetable.startTime} - ${timetable.endTime}",
                        style = CC.descriptionTextStyle(),
                        color = CC.textColor().copy(alpha = 0.7f)
                    )

                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Venue and Lecturer
            Row(
                modifier = Modifier.padding(start = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Venue",
                    tint = CC.tertiary(),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = timetable.venue,
                    style = CC.descriptionTextStyle(),
                    color = CC.textColor().copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.padding(start = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Lecturer",
                    tint = CC.tertiary(),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = timetable.lecturer,
                    style = CC.descriptionTextStyle(),
                    color = CC.textColor().copy(alpha = 0.7f)
                )
            }
        }
    }

}
