package com.mike.uniadmin.authentication

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mike.uniadmin.UniAdminPreferences
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@Composable
fun UserType(context: Context, navController: NavController) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(CC.primary())
            .padding(16.dp)
    ) {
        val columnWidth = maxWidth
        val boxSize = columnWidth * 0.15f
        val textSize = with(LocalDensity.current) { (columnWidth * 0.07f).toSp() }
        val spacing = 24.dp // Additional spacing for better alignment

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            Text(
                text = "Select User Type",
                style = CC.titleTextStyle(context).copy(fontSize = textSize),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = spacing)
            )

            // User Type Options
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .width(columnWidth)
            ) {
                UserTypeOption(
                    label = "Student",
                    icon = Icons.Default.AccountCircle,
                    onClick = {
                        UniAdminPreferences.saveUserType("local")
                        navController.navigate("courses")
                    },
                    boxSize = boxSize,
                    context = context
                )

                Spacer(modifier = Modifier.width(spacing))

                UserTypeOption(
                    label = "Admin",
                    icon = Icons.Default.AccountCircle,
                    onClick = {
                        UniAdminPreferences.saveUserType("admin")
                        navController.navigate("courses")
                    },
                    boxSize = boxSize,
                    context = context
                )
            }
        }
    }
}

@Composable
fun UserTypeOption(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    boxSize: Dp,
    context: Context
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .background(CC.secondary(), CircleShape)
                .size(boxSize)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = CC.tertiary(),
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Medium)
        )
    }
}

