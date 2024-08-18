package com.mike.uniadmin.dashboard

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mike.uniadmin.dataModel.courses.CourseEntity
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@Composable
fun CourseItem(course: CourseEntity, context: Context, navController: NavController) {
    Column(
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(modifier = Modifier
            .background(CC.tertiary(), CircleShape)
            .clip(CircleShape)
            .clickable {
                navController.navigate("courseContent/${course.courseCode}")
            }
            .size(70.dp),
            elevation = CardDefaults.elevatedCardElevation(5.dp)) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
            if (course.courseImageLink.isNotEmpty()) {
                AsyncImage(
                    model = course.courseImageLink,
                    contentDescription = course.courseName,
                    modifier = Modifier
                        .clip(CircleShape)
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.School, "Icon", tint = CC.textColor()
                )
            }}
        }
        Spacer(modifier = Modifier.height(5.dp))
        (if (course.courseName.length > 10) {
            course.courseName.substring(0, 10) + "..."
        } else {
            course.courseName
        }).let {
            Text(
                text = it, style = CC.descriptionTextStyle(context), maxLines = 1
            )
        }
    }
}

@Composable
fun LoadingCourseItem() {
    Column(
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .border(
                    1.dp, CC.textColor(), CircleShape
                )
                .background(CC.primary(), CircleShape)
                .clip(CircleShape)
                .size(70.dp),
            contentAlignment = Alignment.Center
        ) {
            CC.ColorProgressIndicator(modifier = Modifier.fillMaxSize())

        }
        Spacer(modifier = Modifier.height(5.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .width(90.dp)
                .height(15.dp)
        ) {
            CC.ColorProgressIndicator(modifier = Modifier.fillMaxSize())
        }

    }
}
