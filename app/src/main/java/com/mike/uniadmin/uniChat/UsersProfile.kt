package com.mike.uniadmin.uniChat

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.mike.uniadmin.model.users.UserEntity
import com.mike.uniadmin.getUserViewModel
import com.mike.uniadmin.helperFunctions.randomColor
import java.util.Locale
import com.mike.uniadmin.ui.theme.CommonComponents as CC


@Composable
fun UsersProfile(context: Context, navController: NavController) {
    val userViewModel = getUserViewModel(context)
    var searchText by remember { mutableStateOf("") }
    val users by userViewModel.users.observeAsState(emptyList())

    // Filter the users based on the search input
    val filteredUsers = users.filter { user ->
        user.firstName.contains(searchText, ignoreCase = true) || user.lastName.contains(
            searchText,
            ignoreCase = true
        ) || user.id.contains(searchText, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Redesigned Search Bar
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = {
                Text(
                    text = "Search by name or ID",
                    style = CC.descriptionTextStyle()
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clip(RoundedCornerShape(24.dp)),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon",
                    tint = CC.tertiary()
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = CC.appTextFieldColors()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display the filtered users
        if (filteredUsers.isEmpty()) {
            Text(
                text = "No users found",
                style = CC.descriptionTextStyle().copy(
                    fontSize = 18.sp, color = CC.textColor().copy(alpha = 0.7f)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredUsers) { user ->
                    UserProfileCard(user, context, navController)
                }
            }
        }
    }
}

@Composable
fun UserProfileCard(user: UserEntity, context: Context, navController: NavController) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = randomColor.random().copy(alpha = 0.18f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier.padding(16.dp)
        ) {
            val maxWith = maxWidth
            val imageSize = maxWidth * 0.13f // Dynamically calculate the image size
            val iconSize = imageSize * 0.45f // Icons are half the size of the profile image
            val density = LocalDensity.current
            val textSize = with(density) { (maxWidth * 1.3f).toSp() }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Profile Picture or Initials
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(imageSize)
                        .clip(CircleShape)
                        .background(
                            randomColor
                                .random()
                                .copy(alpha = 0.2f)
                        )
                ) {
                    if (user.profileImageLink.isNotBlank()) {
                        Image(
                            painter = rememberAsyncImagePainter(user.profileImageLink),
                            contentDescription = "Profile Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = user.firstName[0].toString()
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                            style = CC.descriptionTextStyle().copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = textSize * 0.05f
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(maxWith * 0.05f)) // Dynamically calculate the spacer width

                // User Info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${user.firstName} ${user.lastName}",
                        style = CC.titleTextStyle().copy(
                            fontSize = textSize * 0.04f, // Adjusted font size based on available width
                            color = CC.textColor()
                        )
                    )
                    Text(
                        text = "Admission No: ${user.id}",
                        style = CC.descriptionTextStyle().copy(
                            fontSize = textSize * 0.030f,
                            color = CC.textColor().copy(alpha = 0.6f)
                        )
                    )
                }

                // Icons for Message and Call
                Row(
                    horizontalArrangement = Arrangement.spacedBy(maxWith * 0.03f), // Dynamic spacing
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.navigate("chat/${user.id}") }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Message Icon",
                            tint = CC.tertiary(),
                            modifier = Modifier.size(iconSize)
                        )
                    }
                    IconButton(
                        onClick = {
                            if (user.phoneNumber.isBlank()) {
                                return@IconButton
                            }
                            val intent = Intent(Intent.ACTION_DIAL)
                            intent.data = Uri.parse("tel:${user.phoneNumber}")
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call Icon",
                            tint = if (user.phoneNumber.isBlank()) CC.textColor()
                                .copy(alpha = 0.3f) else CC.tertiary(),
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }
            }
        }
    }
}



