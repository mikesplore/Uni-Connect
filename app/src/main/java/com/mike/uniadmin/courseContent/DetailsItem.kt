package com.mike.uniadmin.courseContent

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalPostOffice
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mike.uniadmin.backEnd.coursecontent.coursedetails.CourseDetail
import com.mike.uniadmin.backEnd.coursecontent.coursedetails.CourseDetailViewModel
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@Composable
fun DetailsItem(
    courseID: String, detailsViewModel: CourseDetailViewModel, context: Context
) {
    var expanded by remember { mutableStateOf(false) }
    val details = detailsViewModel.details.observeAsState()
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .background(
                    CC
                        .tertiary()
                        .copy(0.1f), RoundedCornerShape(10.dp)
                )
                .fillMaxWidth(0.9f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { detailsViewModel.getCourseDetailsByCourseID(courseID){} },

                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = background,
                )
            ) {
                Icon(
                    Icons.Default.Refresh, contentDescription = "Refresh", tint = CC.textColor()
                )
            }
            FloatingActionButton(
                onClick = { expanded = !expanded },
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(35.dp),
                containerColor = background,
                contentColor = CC.textColor()
            ) {
                Icon(Icons.Default.Add, "Add timetable")
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Column(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(visible = expanded) {
                AddDetailsItem(
                    courseID,
                    context,
                    expanded,
                    onExpandedChange = { expanded = !expanded },
                    detailsViewModel = detailsViewModel
                )
            }
            //card here
            if (details.value == null) {
                Text("No Details", style = CC.descriptionTextStyle(context))
            } else {
                details.value?.let {
                    DetailsItemCard(it, context)
                }
            }

        }
    }

}


@Composable
fun DetailsItemCard(courseDetails: CourseDetail, context: Context) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CC.extraColor1()),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .border(
                width = 1.dp,
                color = CC.secondary().copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Course Title
            Text(
                text = courseDetails.courseName,
                style = CC.titleTextStyle(context).copy(
                    textAlign = TextAlign.Center,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = CC.extraColor2()
                ),
                modifier = Modifier
                    .wrapContentSize(align = Alignment.Center)
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),

            )

            // Course Information
            CourseInfoRow(
                icon = Icons.Default.Info,
                label = "Course Code:",
                value = courseDetails.courseCode,
                context = context
            )
            CourseInfoRow(
                icon = Icons.Default.Person,
                label = "Lecturer:",
                value = courseDetails.lecturer,
                context = context
            )
            CourseInfoRow(
                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                label = "Visits:",
                value = courseDetails.numberOfVisits,
                context = context
            )
            CourseInfoRow(
                icon = Icons.Default.School,
                label = "Department:",
                value = courseDetails.courseDepartment,
                context = context
            )

            HorizontalDivider(
                color = Color.Gray.copy(alpha = 0.3f),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Overview Section
            SectionTitle("Overview", context)
            Text(
                text = courseDetails.overview,
                style = CC.descriptionTextStyle(context).copy(
                    fontSize = 16.sp,
                    color = CC.textColor().copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Learning Outcomes Section
            SectionTitle("Learning Outcomes", context)
            courseDetails.learningOutcomes.forEach { outcome ->
                Text(
                    text = "- $outcome",
                    style = CC.descriptionTextStyle(context).copy(
                        fontSize = 16.sp,
                        color = CC.textColor().copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                )
            }

            // Schedule Section
            SectionTitle("Schedule", context)
            Text(
                text = courseDetails.schedule,
                style = CC.descriptionTextStyle(context).copy(
                    fontSize = 16.sp,
                    color = CC.textColor().copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Required Materials Section
            SectionTitle("Required Materials", context)
            Text(
                text = courseDetails.requiredMaterials,
                style = CC.descriptionTextStyle(context).copy(
                    fontSize = 16.sp,
                    color = CC.textColor().copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
fun CourseInfoRow(icon: ImageVector, label: String, value: String, context: Context) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = CC.textColor(),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = "$label $value",
            style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Medium)
        )
    }
}

@Composable
fun SectionTitle(title: String, context: Context) {
    Text(
        text = title,
        style = CC.descriptionTextStyle(context).copy(
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = CC.extraColor2()
        ),
        modifier = Modifier.padding(vertical = 4.dp)
    )
}


@Composable
fun AddDetailsItem(
    courseID: String,
    context: Context,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    detailsViewModel: CourseDetailViewModel
) {
    var courseName by remember { mutableStateOf("") }
    var courseCode by remember { mutableStateOf("") }
    var lecturer by remember { mutableStateOf("") }
    var numberOfVisits by remember { mutableStateOf("") }
    var courseDepartment by remember { mutableStateOf("") }
    var courseOutcome by remember { mutableStateOf("") }
    var overview by remember { mutableStateOf("") }
    var learningOutcomes by remember { mutableStateOf("") }
    var schedule by remember { mutableStateOf("") }
    var requiredMaterials by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val courseInfo by detailsViewModel.courseDetails.observeAsState()

    LaunchedEffect(courseID) {
        detailsViewModel.getCourseDetailsByCourseID(courseID) { result ->
            if (result) {
                courseName = courseInfo?.courseName.orEmpty()
                courseCode = courseInfo?.courseCode.orEmpty()
                numberOfVisits = courseInfo?.visits.toString()
            }
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = CC.extraColor1()
        ),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(
                width = 1.dp,
                color = Color.Gray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Course Details",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CC.textColor()
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            AddTextField(
                label = "Lecturer",
                value = lecturer,
                onValueChange = { lecturer = it },
                context = context,

            )

            AddTextField(
                label = "Course Department",
                value = courseDepartment,
                onValueChange = { courseDepartment = it },
                context = context,

            )

            AddTextField(
                label = "Course Outcome",
                value = courseOutcome,
                onValueChange = { courseOutcome = it },
                context = context,
                singleLine = false,
                maxLines = 10,

            )

            AddTextField(
                label = "Overview",
                value = overview,
                onValueChange = { overview = it },
                context = context,
                singleLine = false,
                maxLines = 10,

            )

            AddTextField(
                label = "Learning Outcomes",
                value = learningOutcomes,
                onValueChange = { learningOutcomes = it },
                context = context,
                singleLine = false,
                maxLines = 10,

            )

            AddTextField(
                label = "Schedule",
                value = schedule,
                onValueChange = { schedule = it },
                context = context,
                singleLine = false,
                maxLines = 10,

            )

            AddTextField(
                label = "Required Materials",
                value = requiredMaterials,
                onValueChange = { requiredMaterials = it },
                context = context,
                singleLine = false,
                maxLines = 10,

            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        if (courseName.isEmpty() || courseCode.isEmpty() || lecturer.isEmpty() || numberOfVisits.isEmpty() || courseDepartment.isEmpty() || overview.isEmpty()) {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        loading = true
                        val newDetails = CourseDetail(
                            courseName = courseName,
                            courseCode = courseCode,
                            numberOfVisits = numberOfVisits,
                            detailID = "2024$courseID",
                            lecturer = lecturer,
                            courseDepartment = courseDepartment,
                            overview = overview,
                            learningOutcomes = learningOutcomes.split(",").map { it.trim() },
                            schedule = schedule,
                            requiredMaterials = requiredMaterials
                        )
                        detailsViewModel.saveCourseDetail(
                            courseID = courseID,
                            detail = newDetails,
                            onResult = { success ->
                                loading = false
                                if (success) {
                                    onExpandedChange(!expanded)
                                } else {
                                    Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
                                }
                            })
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CC.extraColor2())
                ) {
                    if (loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text(text = if (expanded) "Save" else "Edit", color = Color.White)
                    }
                }
            }
        }
    }
}
