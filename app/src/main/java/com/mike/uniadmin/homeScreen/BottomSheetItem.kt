package com.mike.uniadmin.homeScreen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mike.uniadmin.MainActivity
import com.mike.uniadmin.UniConnectPreferences
import com.mike.uniadmin.model.groupchat.GroupChatViewModel
import com.mike.uniadmin.model.users.UserViewModel
import com.mike.uniadmin.settings.Biometrics
import com.mike.uniadmin.settings.switchColors
import com.mike.uniadmin.uniChat.groupChat.groupChatComponents.GroupItem
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@Composable
fun ModalDrawerItem(
    navController: NavController,
    userViewModel: UserViewModel,
    chatViewModel: GroupChatViewModel,
    activity: MainActivity
) {
    val signedInUser by userViewModel.user.observeAsState()
    val users by userViewModel.users.observeAsState(emptyList())
    val groups by chatViewModel.groups.observeAsState(emptyList())
    val email = UniConnectPreferences.userEmail.value

    LaunchedEffect(Unit) {
        userViewModel.findUserByEmail(email) {}
        userViewModel.fetchUsers()
        chatViewModel.fetchGroups()
    }
    BoxWithConstraints {
        val columnWidth = maxWidth
        val rowHeight = columnWidth * 0.25f
        val iconSize = columnWidth * 0.15f

        val density = LocalDensity.current
        val textSize = with(density) { (columnWidth * 0.07f).toSp() }

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
                        .height(rowHeight)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .border(1.dp, CC.textColor(), CircleShape)
                            .clip(CircleShape)
                            .background(CC.secondary(), CircleShape)
                            .size(iconSize),
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
                                text = "${signedInUser?.firstName?.get(0)}${
                                    signedInUser?.lastName?.get(
                                        0
                                    )
                                }",
                                style = CC.titleTextStyle()
                                    .copy(fontWeight = FontWeight.Bold, fontSize = textSize)
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
                            style = CC.titleTextStyle()
                                .copy(fontWeight = FontWeight.Bold, fontSize = textSize),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        signedInUser?.email?.let {
                            Text(it, style = CC.descriptionTextStyle())
                        }
                        signedInUser?.id?.let {
                            Text(it, style = CC.descriptionTextStyle())
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Chat",
                style = CC.titleTextStyle()
                    .copy(fontWeight = FontWeight.Bold, fontSize = textSize * 0.8f)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Select a user to open chat",
                style = CC.descriptionTextStyle().copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(20.dp))
            LazyRow(
                modifier = Modifier.animateContentSize()
            ) {
                items(users, key = { it.id }) { user ->
                    UserItem(user, navController, userViewModel)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = CC.textColor())
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "Group Discussions",
                style = CC.titleTextStyle()
                    .copy(fontWeight = FontWeight.Bold, fontSize = textSize * 0.8f)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Select a group to open",
                style = CC.descriptionTextStyle().copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(20.dp))

            if (groups.isEmpty()) {
                Text(
                    text = "No groups available",
                    style = CC.descriptionTextStyle().copy(fontSize = 18.sp),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyRow(
                    modifier = Modifier.animateContentSize()
                ) {
                    items(groups, key = { it.id }) { group ->
                        if (group.name.isNotEmpty() && group.description.isNotEmpty() && group.members.contains(signedInUser?.id.toString())) {
                            signedInUser?.let {
                                GroupItem(
                                    group,
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
            HorizontalDivider(color = CC.textColor())
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "Quick Settings",
                style = CC.titleTextStyle()
                    .copy(fontWeight = FontWeight.Bold, fontSize = textSize * 0.8f)
            )
            Spacer(modifier = Modifier.height(10.dp))
            QuickSettings(activity)

        }
    }
}

@Composable
fun QuickSettings(activity: MainActivity) {
    BoxWithConstraints {
        val columnWidth = maxWidth
        val iconSize = columnWidth * 0.10f
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
                        .size(iconSize)
                ) {
                    Icon(
                        if (UniConnectPreferences.darkMode.value) Icons.Default.Nightlight else Icons.Default.LightMode,
                        "theme",
                        tint = CC.textColor()
                    )
                }

                Text("Dark theme ", style = CC.descriptionTextStyle().copy(fontSize = 18.sp))
                Switch(
                    onCheckedChange = {
                        UniConnectPreferences.darkMode.value = it
                        UniConnectPreferences.saveDarkModePreference(it)
                    }, checked = UniConnectPreferences.darkMode.value,
                    colors = switchColors(),
                    modifier = Modifier.size(iconSize)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Biometrics(activity)

        }
    }
}