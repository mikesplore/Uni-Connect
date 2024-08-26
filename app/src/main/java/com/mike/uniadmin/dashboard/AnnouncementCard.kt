package com.mike.uniadmin.dashboard

import android.content.Context
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mike.uniadmin.backEnd.announcements.AnnouncementEntity
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@Composable
fun AnnouncementCard(
    announcement: AnnouncementEntity,
    context: Context
) {
    BaseAnnouncementCard {
        // Title Row
        AnnouncementTitleRow(
            imageLink = announcement.imageLink,
            authorName = announcement.authorName,
            title = announcement.title,
            context = context
        )

        Spacer(modifier = Modifier.height(16.dp))
        // Description
        AnnouncementDescription(
            description = announcement.description,
            context = context
        )

        Spacer(modifier = Modifier.height(16.dp))
        // Bottom Row
        AnnouncementFooter(
            date = announcement.date,
            authorName = announcement.authorName,
            context = context
        )
    }
}


@Composable
fun LoadingAnnouncementCard(context: Context) {
    BaseAnnouncementCard {
        // Title Row
        AnnouncementTitleRow(
            isLoading = true,
            context = context
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
            LoadingPlaceholder(modifier = Modifier.width(100.dp).height(20.dp))
            LoadingPlaceholder(modifier = Modifier.width(100.dp).height(20.dp))
        }
    }
}

@Composable
fun BaseAnnouncementCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .heightIn(min = 200.dp)
            .fillMaxWidth()
            .padding(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = CC.extraColor1()
        ),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .background(CC.extraColor1())
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

@Composable
fun AnnouncementTitleRow(
    imageLink: String = "",
    authorName: String = "",
    title: String = "",
    context: Context,
    isLoading: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        ProfileImageBox(imageLink, authorName, isLoading, context)

        Spacer(modifier = Modifier.width(8.dp))

        if (isLoading) {
            LoadingPlaceholder(modifier = Modifier.width(150.dp).height(30.dp))
        } else {
            Text(
                text = title,
                style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(1f)
            )
        }
    }
}

@Composable
fun ProfileImageBox(imageLink: String, authorName: String, isLoading: Boolean, context: Context) {
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
                style = CC.descriptionTextStyle(context)
                    .copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun AnnouncementFooter(date: String, authorName: String, context: Context) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = CC.getRelativeDate(CC.getDateFromTimeStamp(date)),
            style = CC.descriptionTextStyle(context)
                .copy(color = CC.textColor().copy(alpha = 0.7f))
        )
        Text(
            text = authorName,
            style = CC.descriptionTextStyle(context)
                .copy(color = CC.textColor().copy(alpha = 0.7f))
        )
    }
}


@Composable
fun AnnouncementDescription(description: String, context: Context) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = description,
            style = CC.descriptionTextStyle(context)
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
