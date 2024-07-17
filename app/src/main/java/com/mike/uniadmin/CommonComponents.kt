package com.mike.uniadmin

import android.content.Context
import android.icu.util.Calendar
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mike.uniadmin.ui.theme.GlobalColors
import com.mike.uniadmin.ui.theme.currentFontFamily
import java.time.LocalTime


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
        val currentFont = currentFontFamily(context) // Get initial font
        val selectedFontFamily by remember { mutableStateOf(currentFont) }
        var passwordVisibility by remember { mutableStateOf(false) }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(fontFamily = selectedFontFamily),
            label = { Text(text = label, fontFamily = selectedFontFamily) },
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
        val currentFont = currentFontFamily(context) // Get initial font
        val selectedFontFamily by remember { mutableStateOf(currentFont) }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(fontFamily = selectedFontFamily),
            label = { Text(text = label, fontFamily = selectedFontFamily, fontSize = 14.sp) },
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
        val  color by animateColorAsState(
            targetValue = if (GlobalColors.isDarkMode) GlobalColors.primaryColor
            else GlobalColors.primaryColor,
            label = "primaryBackgroundColor", // Provide a descriptive label
            animationSpec = tween(500)
        )
        return color
    }

    @Composable
    fun secondary(): Color {
        val  color by animateColorAsState(
            targetValue = if (GlobalColors.isDarkMode) GlobalColors.secondaryColor
            else GlobalColors.secondaryColor,
            label = "primaryBackgroundColor", // Provide a descriptive label
            animationSpec = tween(500)
        )
        return color
    }

    @Composable
    fun tertiary(): Color {
        val  color by animateColorAsState(
            targetValue = if (GlobalColors.isDarkMode) GlobalColors.tertiaryColor
            else GlobalColors.tertiaryColor,
            label = "primaryBackgroundColor", // Provide a descriptive label
            animationSpec = tween(500)
        )
        return color
    }

    @Composable
    fun textColor(): Color {
        val  color by animateColorAsState(
            targetValue = if (GlobalColors.isDarkMode) GlobalColors.textColor
            else GlobalColors.textColor,
            label = "primaryBackgroundColor", // Provide a descriptive label
            animationSpec = tween(500)
        )
        return color
    }


    @Composable
    fun extraColor1(): Color {
        val  color by animateColorAsState(
            targetValue = if (GlobalColors.isDarkMode) GlobalColors.extraColor1
            else GlobalColors.extraColor1,
            label = "primaryBackgroundColor", // Provide a descriptive label
            animationSpec = tween(500)
        )
        return color
    }

    @Composable
    fun extraColor2(): Color {
        val  color by animateColorAsState(
            targetValue = if (GlobalColors.isDarkMode) GlobalColors.extraColor2
            else GlobalColors.extraColor2,
            label = "primaryBackgroundColor", // Provide a descriptive label
            animationSpec = tween(500)
        )
        return color
    }

    @Composable
    fun descriptionTextStyle(context: Context): TextStyle {
        val color by animateColorAsState(
            targetValue = if (GlobalColors.isDarkMode) GlobalColors.textColor
            else GlobalColors.textColor,
            label = "",
            animationSpec = tween(500)
        )
        val currentFont = currentFontFamily(context) // Get initial font
        val selectedFontFamily by remember { mutableStateOf(currentFont) }
        return TextStyle(
            fontFamily = selectedFontFamily,
            color = color,
            fontSize = 15.sp
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

    fun currentDayID(): Int {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> 0
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            else -> 0
        }
    }


    @Composable
    fun titleTextStyle(context: Context): TextStyle {
        val color by animateColorAsState(
            targetValue = if (GlobalColors.isDarkMode) GlobalColors.textColor
            else GlobalColors.textColor,
            label = "",
            animationSpec = tween(500)
        )
        val currentFont = currentFontFamily(context) // Get initial font
        val selectedFontFamily by remember { mutableStateOf(currentFont) }
        return TextStyle(
            fontFamily = selectedFontFamily,
            color = color,
            fontSize = 25.sp
        )
    }



    @Composable
    fun appTextFieldColors(): TextFieldColors {
        return TextFieldDefaults.colors(
            focusedContainerColor = GlobalColors.primaryColor,
            unfocusedContainerColor = GlobalColors.primaryColor,
            focusedIndicatorColor = GlobalColors.tertiaryColor,
            unfocusedIndicatorColor = GlobalColors.secondaryColor,
            focusedLabelColor = textColor(),
            cursorColor = textColor(),
            unfocusedLabelColor = textColor(),
            focusedTextColor = textColor(),
            unfocusedTextColor = textColor()
        )
    }
}
