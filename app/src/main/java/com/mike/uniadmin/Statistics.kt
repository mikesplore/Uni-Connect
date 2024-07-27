package com.mike.uniadmin


import android.content.Context
import android.graphics.Paint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mike.uniadmin.dataModel.courses.CourseEntity
import com.mike.uniadmin.dataModel.courses.CourseViewModel
import com.mike.uniadmin.dataModel.courses.CourseViewModelFactory
import com.mike.uniadmin.dataModel.groupchat.UniAdmin
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.roundToInt
import com.mike.uniadmin.model.MyDatabase.ExitScreen
import com.mike.uniadmin.model.MyDatabase.getAllScreenTimes
import com.mike.uniadmin.model.ScreenTime
import com.mike.uniadmin.ui.theme.GlobalColors
import com.mike.uniadmin.CommonComponents as CC

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Statistics(navController: NavController, context: Context) {
    val announcementAdmin = context.applicationContext as? UniAdmin
    val courseRepository = remember { announcementAdmin?.courseRepository }
    val courseViewModel: CourseViewModel = viewModel(
        factory = CourseViewModelFactory(
            courseRepository ?: throw IllegalStateException("CourseRepository is null")
        )
    )
    val courses by courseViewModel.courses.observeAsState()

    var animatedValues by remember { mutableStateOf(emptyList<Float>()) }
    val startTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var timeSpent by remember { mutableLongStateOf(0L) }
    val screenID = "SC11"

    LaunchedEffect(courses) {
        if (courses?.isNotEmpty() == true) {
            animatedValues = courses?.map { it.visits?.toFloat() ?: 0f } ?: emptyList()
        }
    }

    LaunchedEffect(Unit) {
        GlobalColors.loadColorScheme(context)
        courseViewModel.fetchCourses()
        while (true) {
            timeSpent = System.currentTimeMillis() - startTime
            delay(1000) // Update every second (adjust as needed)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            ExitScreen(
                context = context,
                screenID = screenID,
                timeSpent = timeSpent
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("homescreen") }) {
                        Icon(
                            Icons.Default.ArrowBackIosNew,
                            contentDescription = null,
                            tint = CC.textColor()
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CC.primary())
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues = it)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth(0.9f)
                ) {
                    Text(
                        "Statistics",
                        style = CC.titleTextStyle(context)
                            .copy(fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
                    )
                }

                Text(
                    "Course Visits",
                    style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.ExtraBold)
                )
                Text(
                    "Number of visits to each course across all users",
                    style = CC.descriptionTextStyle(context)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(600.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CC.primary()
                    )
                ) {
                    courses?.let { BarGraphContent(it, animatedValues, context) }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "Screen Time",
                    style = CC.titleTextStyle(context),
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "Time spent across different screens", style = CC.descriptionTextStyle(context)
                )
                Spacer(modifier = Modifier.height(10.dp))

                ScreenTimeList(context)
            }
        },
        containerColor = CC.primary()
    )
}

@Composable
fun BarGraphContent(courses: List<CourseEntity>, animatedValues: List<Float>, context: Context) {
    val maxVisits = animatedValues.maxOrNull()?.roundToInt() ?: 1
    val colors = listOf(CC.extraColor2(), CC.extraColor1())
    val animatedHeights = animatedValues.map { value ->
        animateFloatAsState(
            targetValue = (value / maxVisits) * 1f,
            animationSpec = tween(durationMillis = 3000),
            label = ""
        ).value
    }

    Column(
        modifier = Modifier
            .background(CC.secondary())
            .fillMaxSize()
    ) {
        Box(modifier = Modifier.weight(1f)) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 40.dp, end = 16.dp, bottom = 40.dp, top = 16.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val graphWidth = canvasWidth * 0.7f
                val graphHeight = canvasHeight * 0.8f
                val leftPadding = canvasWidth * 0.1f
                val bottomPadding = canvasHeight * 0.1f

                // Draw Y-axis
                drawLine(
                    color = GlobalColors.textColor,
                    start = Offset(leftPadding, canvasHeight - bottomPadding),
                    end = Offset(leftPadding, bottomPadding),
                    strokeWidth = 2f
                )

                // Draw X-axis
                drawLine(
                    color = GlobalColors.textColor,
                    start = Offset(leftPadding, canvasHeight - bottomPadding),
                    end = Offset(canvasWidth - leftPadding / 2, canvasHeight - bottomPadding),
                    strokeWidth = 2f
                )

                // Draw X-axis title
                drawContext.canvas.nativeCanvas.drawText(
                    "Courses",
                    canvasWidth / 2,
                    canvasHeight - bottomPadding / 4,
                    Paint().apply {
                        color = GlobalColors.textColor.toArgb()
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 14.sp.toPx()
                    }
                )

                // Draw Y-axis title
                drawContext.canvas.nativeCanvas.save()
                drawContext.canvas.nativeCanvas.rotate(-90f)
                drawContext.canvas.nativeCanvas.drawText(
                    "Visits",
                    -canvasHeight / 2,
                    leftPadding / 4,
                    Paint().apply {
                        color = GlobalColors.textColor.toArgb()
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 14.sp.toPx()
                    }
                )
                drawContext.canvas.nativeCanvas.restore()

                // Draw Y-axis labels
                val ySteps = 5
                for (i in 0..ySteps) {
                    val y = canvasHeight - bottomPadding - (i.toFloat() / ySteps) * graphHeight
                    val labelValue = ((i.toFloat() / ySteps) * maxVisits).roundToInt()
                    drawContext.canvas.nativeCanvas.drawText(
                        labelValue.toString(),
                        leftPadding - 20.dp.toPx(),
                        y,
                        Paint().apply {
                            color = GlobalColors.textColor.toArgb()
                            textAlign = android.graphics.Paint.Align.RIGHT
                            textSize = 12.sp.toPx()
                        }
                    )
                }

                val barWidth = graphWidth / (courses.size * 2)
                val spacing = barWidth / 2

                courses.forEachIndexed { index, course ->
                    val animatedHeight = (animatedHeights.getOrNull(index) ?: 0f) * graphHeight // Adjust height
                    var color = colors[index % colors.size]

                    // Debugging info
                    Log.d("BarGraphContent", "Course: ${course.courseName}, Visits: ${course.visits}, Animated Height: $animatedHeight")

                    // Draw the bars
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(
                            x = leftPadding + index * (barWidth * 2 + spacing),
                            y = canvasHeight - bottomPadding - animatedHeight
                        ),
                        size = Size(barWidth * 2, animatedHeight.coerceAtLeast(20.dp.toPx())), // Ensure a minimum height
                        cornerRadius = CornerRadius(10f, 10f)
                    )

                    // Draw visit count inside the bar
                    if (animatedHeight > 20.dp.toPx()) {
                        drawContext.canvas.nativeCanvas.drawText(
                            course.visits.toString(),
                            leftPadding + index * (barWidth * 2 + spacing) + barWidth,
                            canvasHeight - bottomPadding - animatedHeight + 15.dp.toPx(),
                            Paint().apply {
                                color = GlobalColors.textColor
                                textAlign = android.graphics.Paint.Align.CENTER
                                textSize = 12.sp.toPx()
                            }
                        )
                    }

                    // Draw letter labels on X-axis
                    drawContext.canvas.nativeCanvas.drawText(
                        ('A' + index).toString(),
                        leftPadding + index * (barWidth * 2 + spacing) + barWidth,
                        canvasHeight - bottomPadding / 2,
                        Paint().apply {
                            color = GlobalColors.textColor
                            textAlign = android.graphics.Paint.Align.CENTER
                            textSize = 12.sp.toPx()
                        }
                    )
                }
            }
        }

        // Draw legend below the graph
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Legend", style = CC.titleTextStyle(context)
            )
            courses.forEachIndexed { index, course ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(colors[index % colors.size])
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${('A' + index)}: ${course.courseName}",
                        style = CC.descriptionTextStyle(context).copy(fontSize = 10.sp)
                    )
                }
            }
        }
    }
}




@Composable
fun ScreenTimeList(context: Context) {
    val screenTimes = remember { mutableStateListOf<ScreenTime>() }

    // Fetch screen times when the composable enters the composition
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            getAllScreenTimes { fetchedScreenTimes ->
                screenTimes.clear()
                screenTimes.addAll(fetchedScreenTimes)
                // Sort the list in descending order of screen time
                screenTimes.sortByDescending { it.time }
            }
        }
    }

    // Display the screen times with animations
    Column(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (screenTime in screenTimes) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                ScreenTimeItem(screenTime, context)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ScreenTimeItem(screenTime: ScreenTime, context: Context) {
    Column(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = CC.textColor(),
                shape = RoundedCornerShape(10.dp) // Use RectangleShape for a square
            )
            .background(
                CC
                    .extraColor2()
                    .copy(0.5f), RoundedCornerShape(10.dp)
            )
            .fillMaxWidth(0.9f)
            .padding(16.dp)
    ) {
        Text(
            text = screenTime.screenName,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp), // Add padding between the two Text composables
            style = CC.titleTextStyle(context)
        )
        Text(
            text = convertToHoursMinutesSeconds(screenTime.time),
            modifier = Modifier.fillMaxWidth(),
            style = CC.descriptionTextStyle(context)
        )
    }
}

fun convertToHoursMinutesSeconds(timeInMillis: Long): String {
    val totalSeconds = timeInMillis / 1000

    // Calculate hours, minutes, and seconds
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    // Build the formatted string based on non-zero values
    val timeComponents = mutableListOf<String>()
    if (hours > 0) {
        timeComponents.add(String.format(Locale.getDefault(), "%02d hours", hours))
    }
    if (minutes > 0) {
        timeComponents.add(String.format(Locale.getDefault(), "%02d minutes", minutes))
    }
    if (seconds > 0 || timeComponents.isEmpty()) {
        timeComponents.add(String.format(Locale.getDefault(), "%02d seconds", seconds))
    }

    // Join the components with a comma
    return timeComponents.joinToString(", ")
}












