package com.mike.uniadmin.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mike.uniadmin.model.announcements.AnnouncementsWithAuthor
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@Composable
fun AnnouncementCard(
    announcement: AnnouncementsWithAuthor,
) {
    BaseAnnouncementCard {
        // Title Row
        AnnouncementTitleRow(
            imageLink = announcement.profileImageLink,
            authorName = announcement.authorName,
            title = announcement.title,
        )

        Spacer(modifier = Modifier.height(16.dp))
        // Description
        AnnouncementDescription(
            description = announcement.description,
        )

        Spacer(modifier = Modifier.height(16.dp))
        // Bottom Row
        AnnouncementFooter(
            date = announcement.date,
            authorName = announcement.authorName,
        )
    }
}


@Composable
fun LoadingAnnouncementCard() {
    BaseAnnouncementCard {
        // Title Row
        AnnouncementTitleRow(
            isLoading = true,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Loading description
        LoadingPlaceholder(modifier = Modifier.height(100.dp))

        Spacer(modifier = Modifier.height(16.dp))

        // Loading footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LoadingPlaceholder(modifier = Modifier
                .width(100.dp)
                .height(20.dp))
            LoadingPlaceholder(modifier = Modifier
                .width(100.dp)
                .height(20.dp))
        }
    }
}

@Composable
fun BaseAnnouncementCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .heightIn(min = 100.dp)
            .shadow(
                elevation = 4.dp, shape = RoundedCornerShape(16.dp),
            )
            .background(CC.surfaceContainer())
            .fillMaxWidth(0.9f)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

@Composable
fun AnnouncementTitleRow(
    imageLink: String = "",
    authorName: String = "",
    title: String = "",
    isLoading: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        ProfileImageBox(imageLink, authorName, isLoading)

        Spacer(modifier = Modifier.width(8.dp))

        if (isLoading) {
            LoadingPlaceholder(modifier = Modifier
                .width(150.dp)
                .height(30.dp))
        } else {
            Text(
                text = title,
                style = CC.titleTextStyle().copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(1f)
            )
        }
    }
}

@Composable
fun ProfileImageBox(imageLink: String, authorName: String, isLoading: Boolean) {
    Box(
        modifier = Modifier
            .border(1.dp, CC.textColor(), CircleShape)
            .clip(CircleShape)
            .background(CC.secondary(), CircleShape)
            .size(40.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CC.ColorProgressIndicator(modifier = Modifier.fillMaxSize())
        } else if (imageLink.isNotEmpty()) {
            AsyncImage(
                model = imageLink,
                contentDescription = "Profile Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = authorName.firstOrNull()?.toString() ?: "",
                style = CC.descriptionTextStyle()
                    .copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun AnnouncementFooter(date: String, authorName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = CC.getRelativeDate(CC.getDateFromTimeStamp(date)),
            style = CC.descriptionTextStyle()
                .copy(color = CC.textColor().copy(alpha = 0.7f))
        )
        Text(
            text = authorName,
            style = CC.descriptionTextStyle()
                .copy(color = CC.textColor().copy(alpha = 0.7f))
        )
    }
}


@Composable
fun AnnouncementDescription(description: String) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = description,
            style = CC.descriptionTextStyle()
                .copy(color = CC.textColor().copy(alpha = 0.7f))
        )
    }
}

@Composable
fun LoadingPlaceholder(modifier: Modifier) {
    CC.ColorProgressIndicator(
        modifier = modifier.clip(RoundedCornerShape(10.dp))
    )
}
