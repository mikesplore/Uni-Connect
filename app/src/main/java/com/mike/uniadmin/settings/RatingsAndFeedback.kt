package com.mike.uniadmin.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.mike.uniadmin.backEnd.users.UserEntity
import com.mike.uniadmin.helperFunctions.Feedback
import com.mike.uniadmin.helperFunctions.MyDatabase
import com.mike.uniadmin.ui.theme.CommonComponents

@Composable
fun StarRating(
    currentRating: Int, onRatingChanged: (Int) -> Unit, modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..5) {
            val color = when {
                i <= currentRating -> when (i) {
                    in 1..2 -> Color.Red
                    3 -> CommonComponents.extraColor2()
                    else -> Color.Green
                }

                else -> CommonComponents.secondary()
            }
            val animatedScale by animateFloatAsState(
                targetValue = if (i <= currentRating) 1.2f else 1.0f,
                animationSpec = tween(durationMillis = 300),
                label = ""
            )
            Star(filled = i <= currentRating,
                color = color,
                scale = animatedScale,
                onClick = { onRatingChanged(i) })
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

@Composable
fun Star(
    filled: Boolean, color: Color, scale: Float, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    val path = Path().apply {
        moveTo(50f, 0f)
        lineTo(61f, 35f)
        lineTo(98f, 35f)
        lineTo(68f, 57f)
        lineTo(79f, 91f)
        lineTo(50f, 70f)
        lineTo(21f, 91f)
        lineTo(32f, 57f)
        lineTo(2f, 35f)
        lineTo(39f, 35f)
        close()
    }

    Canvas(
        modifier = modifier
            .size((40 * scale).dp)
            .clickable(onClick = onClick)
    ) {
        drawPath(
            path = path,
            color = if (filled) color else com.mike.uniadmin.ui.theme.BrightBlue,
            style = if (filled) Stroke(width = 8f) else Stroke(
                width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round
            )
        )
    }
}

@Composable
fun RatingAndFeedbackScreen(user: UserEntity, context: Context) {
    var currentRating by remember { mutableIntStateOf(0) }
    var feedbackText by remember { mutableStateOf("") }
    var averageRatings by remember { mutableStateOf("") }
    var showFeedbackForm by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        MyDatabase.fetchAverageRating { averageRating ->
            averageRatings = averageRating
        }
    }

    Column(
        modifier = Modifier

            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (averageRatings.isEmpty()) "No ratings yet" else "Average Rating: $averageRatings",
            style = CommonComponents.descriptionTextStyle(),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        StarRating(
            currentRating = currentRating,
            onRatingChanged = { rating ->
                currentRating = rating
                showFeedbackForm = true
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedVisibility(visible = showFeedbackForm) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(value = feedbackText,
                    onValueChange = { feedbackText = it },
                    label = {
                        Text(
                            "Enter your feedback (optional)",
                            style = CommonComponents.descriptionTextStyle()
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    textStyle = CommonComponents.descriptionTextStyle(),
                    maxLines = 5,
                    colors = CommonComponents.appTextFieldColors()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        loading = true
                        MyDatabase.generateFeedbackID { feedbackId ->
                            val feedback = Feedback(
                                id = feedbackId,
                                rating = currentRating,
                                sender = user.firstName + " " + user.lastName,
                                message = feedbackText,
                                admissionNumber = user.id
                            )
                            MyDatabase.writeFeedback(feedback, onSuccess = {
                                loading = false
                                Toast.makeText(
                                    context, "Thanks for your feedback", Toast.LENGTH_SHORT
                                ).show()
                                feedbackText = ""
                                MyDatabase.fetchAverageRating { averageRating ->
                                    averageRatings = averageRating
                                }
                                showFeedbackForm = false
                            }, onFailure = {
                                loading = false
                                Toast.makeText(
                                    context,
                                    "Failed to send feedback: ${it?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            })
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CommonComponents.extraColor1(), contentColor = CommonComponents.secondary()
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                color = CommonComponents.primary(),
                                trackColor = CommonComponents.tertiary(),
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text("Submit Feedback", style = CommonComponents.descriptionTextStyle())
                        }
                    }
                }
            }
        }
    }
}