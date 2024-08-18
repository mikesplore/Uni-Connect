package com.mike.uniadmin.homeScreen

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mike.uniadmin.DeviceTheme
import com.mike.uniadmin.MainActivity
import com.mike.uniadmin.dataModel.groupchat.ChatViewModel
import com.mike.uniadmin.dataModel.users.UserViewModel
import com.mike.uniadmin.ui.theme.CommonComponents as CC
import com.mike.uniadmin.uniChat.groupChat.groupChatComponents.GroupItem

@Composable
fun ModalDrawerItem(
    context: Context,
    navController: NavController,
    userViewModel: UserViewModel,
    chatViewModel: ChatViewModel,
    activity: MainActivity
) {
    val signedInUser by userViewModel.user.observeAsState()
    val currentUser by userViewModel.signedInUser.observeAsState()
    val users by userViewModel.users.observeAsState(emptyList())
    val groups by chatViewModel.groups.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        userViewModel.getSignedInUser()
        currentUser?.email?.let { email ->
            userViewModel.findUserByEmail(email) {}
        }
        userViewModel.fetchUsers()
        chatViewModel.fetchGroups()
    }
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(10.dp)
            .background(CC.primary())
            .fillMaxSize()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CC.extraColor1())
        ) {
            Row(
                modifier = Modifier
                    .height(100.dp)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .border(1.dp, CC.textColor(), CircleShape)
                        .clip(CircleShape)
                        .background(CC.secondary(), CircleShape)
                        .size(70.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (signedInUser?.profileImageLink?.isNotEmpty() == true) {
                        AsyncImage(
                            model = signedInUser?.profileImageLink,
                            contentDescription = "Profile Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = "${signedInUser?.firstName?.get(0)}${signedInUser?.lastName?.get(0)}",
                            style = CC.titleTextStyle(context)
                                .copy(fontWeight = FontWeight.Bold, fontSize = 27.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${signedInUser?.firstName} ${signedInUser?.lastName}",
                        style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    signedInUser?.email?.let {
                        Text(it, style = CC.descriptionTextStyle(context))
                    }
                    signedInUser?.id?.let {
                        Text(it, style = CC.descriptionTextStyle(context))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Chat", style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            "Select a user to open chat",
            style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(20.dp))
        LazyRow(
            modifier = Modifier.animateContentSize()
        ) {
            items(users, key = { it.id }) { user ->
                UserItem(user, context, navController, userViewModel)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "Group Discussions",
            style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            "Select a group to open",
            style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(20.dp))

        if (groups.isEmpty()) {
            Text(
                text = "No groups available",
                style = CC.descriptionTextStyle(context).copy(fontSize = 18.sp),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            LazyRow(
                modifier = Modifier.animateContentSize()
            ) {
                items(groups, key = { it.id }) { group ->
                    if (group.name.isNotEmpty() && group.description.isNotEmpty()) {
                        signedInUser?.let {
                            GroupItem(
                                group,
                                context,
                                navController,
                                chatViewModel,
                                userViewModel,
                                it
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "Quick Settings", style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(10.dp))
        QuickSettings(context, activity)

    }
}

@Composable
fun QuickSettings(context: Context, activity: MainActivity) {
    var isBiometricsEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .background(CC.primary())
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {},
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CC.secondary())
                    .size(50.dp)
            ) {
                Icon(
                    if (DeviceTheme.darkMode.value) Icons.Default.Nightlight else Icons.Default.LightMode,
                    "theme",
                    tint = CC.textColor()
                )
            }

            Text("Dark theme ", style = CC.descriptionTextStyle(context))
            Switch(
                onCheckedChange = {
                    DeviceTheme.darkMode.value = it
                    DeviceTheme.saveDarkModePreference(it)
                }, checked = DeviceTheme.darkMode.value, colors = SwitchDefaults.colors(
                    checkedThumbColor = CC.extraColor1(),
                    uncheckedThumbColor = CC.extraColor2(),
                    checkedTrackColor = CC.extraColor2(),
                    uncheckedTrackColor = CC.extraColor1(),
                    checkedIconColor = CC.textColor(),
                    uncheckedIconColor = CC.textColor()
                )
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {},
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CC.secondary())
                    .size(50.dp)
            ) {
                Icon(
                    Icons.Default.Fingerprint, "theme", tint = CC.textColor()
                )
            }

            Text("Biometrics", style = CC.descriptionTextStyle(context))
            Switch(
                onCheckedChange = { checked -> // Add checked parameter
                    if (checked) {
                        activity.promptManager.showBiometricPrompt(title = "User Authentication",
                            description = "Please Authenticate",
                            onResult = { success ->
                                isBiometricsEnabled = success // Update state based on success
                                if (success) {
                                    Toast.makeText(
                                        context,
                                        "Authenticated Successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Authentication Failed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })
                    } else {
                        isBiometricsEnabled = false // Update state if switch is turned off manually
                    }
                }, checked = isBiometricsEnabled, colors = SwitchDefaults.colors(
                    checkedThumbColor = CC.extraColor1(),
                    uncheckedThumbColor = CC.extraColor2(),
                    checkedTrackColor = CC.extraColor2(),
                    uncheckedTrackColor = CC.extraColor1(),
                    checkedIconColor = CC.textColor(),
                    uncheckedIconColor = CC.textColor()
                )
            )
        }
    }
}