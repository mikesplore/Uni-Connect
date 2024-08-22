package com.mike.uniadmin.uniChat

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.mike.uniadmin.backEnd.users.UserEntity
import com.mike.uniadmin.getUserViewModel
import com.mike.uniadmin.model.randomColor
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
        // Search Bar
        OutlinedTextField(value = searchText,
            onValueChange = { searchText = it },
            label = { Text(text = "Search by name or ID", style = CC.descriptionTextStyle(context)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search, contentDescription = "Search Icon"
                )
            },
            singleLine = true,
            colors = CC.appTextFieldColors()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display the filtered users
        if (filteredUsers.isEmpty()) {
            Text(text = "No users found", style = CC.descriptionTextStyle(context))
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredUsers) { user ->
                UserProfileCard(user, context, navController)
            }
        }
    }
}

@Composable
fun UserProfileCard(user: UserEntity, context: Context, navController: NavController) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = randomColor.random().copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)
        ) {
            // Profile Picture or Initials
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = CC.extraColor2(),
                        shape = CircleShape
                    )
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(randomColor.random())
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
                        text = user.firstName[0].toString(), style = TextStyle(
                            color = CC.textColor(), fontWeight = FontWeight.Bold, fontSize = 24.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // User Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${user.firstName} ${user.lastName}", style = CC.titleTextStyle(context).copy(
                        fontSize = 15.sp, color = CC.textColor()
                    )
                )
                Text(
                    text = "ID: ${user.id}", style = CC.descriptionTextStyle(context).copy(
                        fontSize = 12.sp, color = CC.textColor().copy(alpha = 0.5f)
                    )

                )
            }

            // Icons for Message and Call
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = {
                    // Navigate to message screen or trigger messaging action
                    navController.navigate("chat/${user.id}")
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Message,
                        contentDescription = "Message Icon",
                        tint = CC.extraColor2()
                    )
                }
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:${user.phoneNumber}")
                    context.startActivity(intent)
                }) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call Icon",
                        tint = CC.extraColor2()
                    )
                }
            }
        }
    }
}

