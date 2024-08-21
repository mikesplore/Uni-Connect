package com.mike.uniadmin.dashboard

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mike.uniadmin.courseResources.CourseName
import com.mike.uniadmin.backEnd.courses.CourseEntity
import com.mike.uniadmin.backEnd.courses.CourseViewModel
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@Composable
fun CourseBox(
    course: CourseEntity,
    context: Context,
    navController: NavController,
    onClicked: (CourseEntity) -> Unit
) {
    BaseCourseBox(
        imageContent = {
            AsyncImage(
                model = course.courseImageLink,
                contentDescription = course.courseName,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp))
                    .fillMaxSize(),
                alignment = Alignment.Center,
                contentScale = ContentScale.Crop
            )
        },
        bodyContent = {
            Text(
                course.courseCode,
                style = CC.descriptionTextStyle(context),
                modifier = Modifier.padding(start = 10.dp)
            )
            Text(
                course.courseName,
                style = CC.titleTextStyle(context)
                    .copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            val visits = when (course.visits) {
                0 -> "Never visited"
                1 -> "Visited once"
                else -> "Visited ${course.visits} times"
            }
            Row(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    visits,
                    style = CC.descriptionTextStyle(context)
                        .copy(color = CC.tertiary())
                )
                IconButton(
                    onClick = {
                        onClicked(course)
                        CourseName.name.value = course.courseName
                        navController.navigate("courseResource/${course.courseCode}")
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = CC.textColor()
                    )
                }
            }
        }
    )
}

@Composable
fun LoadingCourseBox() {
    BaseCourseBox(
        imageContent = {
            CC.ColorProgressIndicator(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .fillMaxSize()
            )
        },
        bodyContent = {
            LoadingPlaceholder(modifier = Modifier
                .height(20.dp)
                .fillMaxWidth(0.5f)) // Adjusted to a fraction of width
            LoadingPlaceholder(modifier = Modifier
                .height(25.dp)
                .padding(horizontal = 10.dp)
                .fillMaxWidth())
            LoadingPlaceholder(modifier = Modifier
                .height(25.dp)
                .fillMaxWidth(0.5f)) // Adjusted to a fraction of width
        }
    )
}

@Composable
fun BaseCourseBox(
    imageContent: @Composable BoxScope.() -> Unit,
    bodyContent: @Composable ColumnScope.() -> Unit
) {
    BoxWithConstraints {
        // Dynamically calculate width and height based on screen size
        val boxWidth = maxWidth * 0.95f  // 40% of available width
        val boxHeight = boxWidth * 1.25f // Maintain an aspect ratio

        Column(
            modifier = Modifier
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(16.dp)
                )
                .width(boxWidth)
                .height(boxHeight),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight(0.4f)
                    .background(CC.extraColor2(), RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp))
                    .fillMaxWidth(),
                content = imageContent
            )
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(CC.extraColor1(), RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp))
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween,
                content = bodyContent
            )
        }
    }
}

@Composable
fun CourseBoxList(courses: List<CourseEntity>, context: Context, navController: NavController, courseViewModel: CourseViewModel){
    BoxWithConstraints {
        val screenWidth = maxWidth
        val itemWidth = screenWidth * 0.4f // Each item takes 40% of the screen width

        // Set a minimum and maximum width for the items
        val adaptiveItemWidth = itemWidth.coerceIn(minimumValue = 200.dp, maximumValue = 250.dp)
        LazyRow(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .fillMaxWidth(),
        ) {
            items(courses) { course -> // Use the sorted list
                Box(
                    modifier = Modifier.width(adaptiveItemWidth) // Apply the adaptive width
                ) {
                CourseBox(course, context, navController, onClicked = {
                    courseViewModel.saveCourse(
                        course.copy(
                            visits = course.visits.plus(
                                1
                            )
                        )
                    )
                })}
            }
        }
    }
}

