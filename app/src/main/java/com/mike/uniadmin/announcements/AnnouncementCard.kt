package com.mike.uniadmin.announcements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mike.uniadmin.UniConnectPreferences
import com.mike.uniadmin.model.announcements.AnnouncementViewModel
import com.mike.uniadmin.model.announcements.AnnouncementsWithAuthor
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@Composable
fun AnnouncementCard(
    announcement: AnnouncementsWithAuthor,
    onEdit: () -> Unit,
    onDelete: (String) -> Unit,
    isEditing: Boolean,
    onEditComplete: () -> Unit,
    announcementViewModel: AnnouncementViewModel
) {
    // State to track whether the card is expanded or not
    var expanded by remember { mutableStateOf(false) }
    val iconRotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "")
    val cardElevation by animateDpAsState(targetValue = if (expanded) 8.dp else 2.dp, label = "")
    val descriptionAlpha by animateFloatAsState(
        targetValue = if (expanded) 1f else 0.8f,
        label = ""
    )
    val userTypes = UniConnectPreferences.userType.value

    Card(
        elevation = CardDefaults.elevatedCardElevation(cardElevation),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { expanded = !expanded }
            .animateContentSize() // Animate size changes
    ) {
        Column(
            modifier = Modifier
                .background(CC.secondary(), shape = RoundedCornerShape(8.dp))
                .padding(16.dp)
                .imePadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Profile image or author's initial
                Box(
                    modifier = Modifier
                        .border(1.dp, CC.textColor(), CircleShape)
                        .clip(CircleShape)
                        .background(CC.secondary(), CircleShape)
                        .size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (announcement.profileImageLink.isNotEmpty()) {
                        // Load image asynchronously if the link is available
                        AsyncImage(
                            model = announcement.profileImageLink,
                            contentDescription = "Profile Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Display author's initial if no image is available
                        Text(
                            "${announcement.authorName[0]}",
                            style = CC.descriptionTextStyle().copy(fontWeight = FontWeight.Bold),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Announcement title
                Text(
                    text = announcement.title,
                    style = CC.descriptionTextStyle(),
                    fontWeight = FontWeight.Bold,
                    color = CC.textColor(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Button to toggle expansion of the card
                IconButton(
                    onClick = { expanded = !expanded },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = CC.primary().copy(0.3f),
                        contentColor = CC.textColor()
                    )
                ) {
                    Icon(
                        Icons.Default.ArrowDownward,
                        "Expand",
                        modifier = Modifier.rotate(iconRotation)
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Announcement description
                    Text(
                        text = announcement.description,
                        style = CC.descriptionTextStyle().copy(fontSize = 14.sp),
                        color = CC.textColor().copy(alpha = descriptionAlpha),
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Author name and date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = announcement.authorName,
                            style = CC.descriptionTextStyle().copy(fontSize = 12.sp),
                            color = CC.textColor().copy(alpha = 0.6f),
                        )

                        Text(
                            text = CC.getRelativeDate(CC.getDateFromTimeStamp(announcement.date)),
                            style = CC.descriptionTextStyle().copy(fontSize = 12.sp),
                            color = CC.textColor().copy(alpha = 0.6f),
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Edit and Delete buttons
                    if (userTypes == "admin" && announcement.authorID == UniConnectPreferences.userID.value) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { onEdit() },
                                colors = ButtonDefaults.buttonColors(containerColor = CC.primary())
                            ) {
                                Text("Edit", style = CC.descriptionTextStyle())
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { onDelete(announcement.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = CC.tertiary())
                            ) {
                                Text("Delete", style = CC.descriptionTextStyle())
                            }
                        }
                    }
                }
            }

            // Inline editing form when the card is in editing mode
            if (isEditing) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    EditAnnouncement(
                        announcement = announcement, onComplete = {
                            onEditComplete()
                        }, announcementViewModel = announcementViewModel
                    )
                }
            }
        }
    }
}