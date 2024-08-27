package com.mike.uniadmin.ui.theme

import android.content.Context
import android.icu.util.Calendar
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale


object CommonComponents {
    private val calendar: Calendar = Calendar.getInstance()
    private val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

    @Composable
    fun PasswordTextField(
        modifier: Modifier = Modifier,
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        enabled: Boolean = true,
        isError: Boolean = false,
        context: Context

    ) {
        val currentFont = currentFontFamily() // Get initial font
        val selectedFontFamily by remember { mutableStateOf(currentFont) }
        var passwordVisibility by remember { mutableStateOf(false) }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(fontFamily = selectedFontFamily),
            placeholder = { Text(text = label, fontFamily = selectedFontFamily, fontSize = 14.sp) },
            singleLine = true,
            enabled = enabled,
            isError = isError,
            trailingIcon = {
                IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                    Icon(
                        imageVector = if (passwordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        tint = textColor(),
                        contentDescription = if (passwordVisibility) "Hide password" else "Show password"
                    )
                }
            },
            visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
            colors = appTextFieldColors(),
            shape = RoundedCornerShape(10.dp),
            modifier = modifier
                .width(300.dp)
                .shadow(
                    elevation = 10.dp, shape = RoundedCornerShape(20.dp)
                )
        )
    }


    @Composable
    fun getGreetingMessage(): String {
        val currentTime = LocalTime.now()
        return when (currentTime.hour) {
            in 5..11 -> "Good Morning"
            in 12..17 -> "Good Afternoon"
            in 18..21 -> "Good Evening"
            else -> "Good Night"
        }
    }

    @Composable
    fun SingleLinedTextField(
        modifier: Modifier = Modifier,
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        enabled: Boolean = true,
        isError: Boolean = false,
        singleLine: Boolean,
        context: Context

    ) {
        val currentFont = currentFontFamily() // Get initial font
        val selectedFontFamily by remember { mutableStateOf(currentFont) }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(fontFamily = selectedFontFamily),
            placeholder = { Text(text = label, fontFamily = selectedFontFamily, fontSize = 14.sp) },
            singleLine = singleLine,
            enabled = enabled,
            isError = isError,
            colors = appTextFieldColors(),
            shape = RoundedCornerShape(10.dp),
            modifier = modifier
                .width(300.dp)
                .shadow(
                    elevation = 10.dp, shape = RoundedCornerShape(20.dp)
                )
        )
    }

    @Composable
    fun primary(): Color {
        val color = MaterialTheme.colorScheme.primary
        return color
    }

    @Composable
    fun secondary(): Color {
        val color = MaterialTheme.colorScheme.secondary
        return color
    }

    @Composable
    fun tertiary(): Color {
        return MaterialTheme.colorScheme.tertiary
    }

    @Composable
    fun textColor(): Color {
        return MaterialTheme.colorScheme.surface
    }


    @Composable
    fun extraColor1(): Color {
        return MaterialTheme.colorScheme.onPrimary
    }

    @Composable
    fun extraColor2(): Color {
        return MaterialTheme.colorScheme.onSecondary
    }

    @Composable
    fun descriptionTextStyle(context: Context): TextStyle {
        val color = textColor()
        val currentFont = currentFontFamily() // Get initial font
        val selectedFontFamily by remember { mutableStateOf(currentFont) }
        return TextStyle(
            fontFamily = selectedFontFamily, color = color, fontSize = 15.sp
        )
    }

    fun currentDay(): String {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> "Invalid Day" // This should never happen
        }
    }


    @Composable
    fun titleTextStyle(context: Context): TextStyle {
        val color = textColor()
        val currentFont = currentFontFamily() // Get initial font
        val selectedFontFamily by remember { mutableStateOf(currentFont) }
        return TextStyle(
            fontFamily = selectedFontFamily, color = color, fontSize = 25.sp
        )
    }

    @Composable
    fun ColorProgressIndicator(modifier: Modifier = Modifier) {
        val infiniteTransition = rememberInfiniteTransition(label = "")
        val offsetX by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1900, easing = LinearEasing
                ), repeatMode = RepeatMode.Restart
            ), label = ""
        )

        Box(
            modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            secondary().copy(alpha = 0.3f),
                            secondary().copy(alpha = 0.6f),
                            secondary().copy(alpha = 0.9f),
                            secondary().copy(alpha = 0.6f),
                            secondary().copy(alpha = 0.3f),
                            Color.Transparent
                        ),
                        start = Offset(x = -1000f + offsetX * 2000f, y = 0f),
                        end = Offset(x = offsetX * 2000f, y = 0f)
                    )
                )
        )
    }


    @Composable
    fun appTextFieldColors(): TextFieldColors {
        return TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.primary,
            unfocusedContainerColor = MaterialTheme.colorScheme.primary,
            focusedIndicatorColor = MaterialTheme.colorScheme.tertiary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.secondary,
            focusedLabelColor = textColor(),
            cursorColor = textColor(),
            unfocusedLabelColor = textColor(),
            focusedTextColor = textColor(),
            unfocusedTextColor = textColor()
        )
    }

    // Function to get the current timestamp
    fun getTimeStamp(): String {
        return System.currentTimeMillis().toString()
    }


    fun getFormattedTime(timestamp: String): String {
        val instant = Instant.ofEpochMilli(timestamp.toLong())
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val timeFormat = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT) // Use system format
        return timeFormat.format(dateTime)
    }

    fun getDateFromTimeStamp(timestamp: String): String {
        val instant = Instant.ofEpochMilli(timestamp.toLong())
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yy", Locale.getDefault())
        return dateFormat.format(dateTime)
    }


    fun getRelativeDate(date: String): String {
        val todayTimestamp = getTimeStamp()
        val yesterdayTimestamp =
            todayTimestamp.toLong() - (24 * 60 * 60 * 1000) // Subtract a day in milliseconds

        return when (date) {
            getDateFromTimeStamp(todayTimestamp) -> "Today"
            getDateFromTimeStamp(yesterdayTimestamp.toString()) -> "Yesterday"
            else -> date
        }
    }

    fun getRelativeTime(timestamp: String): String {
        val todayTimestamp = System.currentTimeMillis()
        val yesterdayTimestamp =
            todayTimestamp - (24 * 60 * 60 * 1000) // Subtract a day in milliseconds

        return when {
            timestamp.toLong() >= todayTimestamp - (12 * 60 * 60 * 1000) -> { // Within the last 12 hours
                getFormattedTime(timestamp) // Display time
            }

            isSameDay(timestamp.toLong(), todayTimestamp) -> "Today"
            isSameDay(timestamp.toLong(), yesterdayTimestamp) -> "Yesterday"
            else -> getDateFromTimeStamp(timestamp) // Display date
        }
    }

    // Helper function to check if two timestamps are on the same day
    private fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        val calendar1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
        val calendar2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) && calendar1.get(
            Calendar.DAY_OF_YEAR
        ) == calendar2.get(Calendar.DAY_OF_YEAR)
    }

}




