package com.mike.uniadmin.dashboard

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
fun ModuleTimetableCard(timetable: ModuleTimetable, context: Context) {
    val today = CC.currentDay()
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CC.secondary()),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp))
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start,
            ) {
                // Module Name
                Text(
                    text = timetable.moduleName,
                    style = CC.titleTextStyle(context).copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Day and Time
                Row(
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
                            text = "Today",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = CC.textColor().copy(alpha = 0.7f)
                            )
                        )
                    } else {
                        Text(
                            text = "${timetable.day}, ${timetable.startTime} - ${timetable.endTime}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = CC.textColor().copy(alpha = 0.7f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Venue and Lecturer
                Row(
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
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = CC.textColor().copy(alpha = 0.7f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
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
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = CC.textColor().copy(alpha = 0.7f)
                        )
                    )
                }
            }
        }
    }
}
