package com.mike.uniadmin.homeScreen

import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.backEnd.groupchat.GroupChatViewModel
import com.mike.uniadmin.backEnd.users.SignedInUser
import com.mike.uniadmin.backEnd.users.UserEntity
import com.mike.uniadmin.backEnd.users.UserStateEntity
import com.mike.uniadmin.backEnd.users.UserViewModel
import com.mike.uniadmin.model.MyDatabase
import com.mike.uniadmin.model.Update
import com.mike.uniadmin.ui.theme.CommonComponents as CC
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ModalNavigationDrawerItem(
    drawerState: DrawerState,
    scope: CoroutineScope,
    context: Context,
    navController: NavController,
    userViewModel: UserViewModel,
    chatViewModel: GroupChatViewModel,
    signedInUserLoading: Boolean?,
    signedInUser: SignedInUser?,
    fetchedUserDetails: UserEntity?,
    showBottomSheet:(Boolean) -> Unit,
    userStatus: UserStateEntity?,
    update: Update

){
    var showSignOutDialog by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .background(
                CC.extraColor1(), RoundedCornerShape(0.dp, 0.dp, 10.dp, 10.dp)
            )
            .fillMaxHeight()
            .fillMaxWidth(0.5f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (signedInUserLoading == true) {
                CircularProgressIndicator(color = CC.textColor())
            } else if (signedInUser != null) {
                fetchedUserDetails?.let { SideProfile(it, context) }
            } else{
                Icon(Icons.Default.AccountCircle, "", tint = CC.textColor())
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp)
        ) {
            SideBarItem(
                icon = Icons.Default.AccountCircle,
                text = "Profile",
                context,
                onClicked = {
                    scope.launch {
                        drawerState.close()
                    }
                    navController.navigate("profile")
                })
            SideBarItem(icon = Icons.AutoMirrored.Filled.Message,
                text = "Uni Chat",
                context,
                onClicked = {
                    scope.launch {
                        drawerState.close()
                    }
                    userViewModel.fetchUsers()
                    chatViewModel.fetchGroups()
                    navController.navigate("uniChat")
                })
            SideBarItem(icon = Icons.Default.Notifications,
                text = "Notifications",
                context,
                onClicked = {
                    scope.launch {
                        drawerState.close()
                    }
                    userViewModel.fetchUsers()
                    chatViewModel.fetchGroups()
                    navController.navigate("notifications")
                })
            SideBarItem(icon = Icons.Default.Settings, text = "Settings", context, onClicked = {
                scope.launch {
                    drawerState.close()
                }
                navController.navigate("settings")
            })
            SideBarItem(icon = Icons.Default.Share, text = "Share App", context, onClicked = {
                scope.launch {
                    drawerState.close()
                }
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "${fetchedUserDetails?.firstName} invites you to join Uni Admin! Get organized and ace your studies.\n Download now: ${update.updateLink}"
                    ) // Customize the text
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, null))
                scope.launch { drawerState.close() }
            })
            SideBarItem(
                icon = Icons.Default.ArrowDownward,
                text = "More",
                context,
                onClicked = {
                    scope.launch {
                        drawerState.close()
                    }
                    userViewModel.fetchUsers()
                    chatViewModel.fetchGroups()
                    showBottomSheet(true)
                })
        }
        Row(
            modifier = Modifier
                .background(CC.extraColor1())
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            TextButton(onClick = {
                scope.launch {
                    drawerState.close()
                }
                showSignOutDialog = true

            }) {
                Text(
                    "Sign Out",
                    style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold)
                )
            }
            if (showSignOutDialog) {
                SignOut(
                    userStatus = userStatus,
                    onVisibleChange = {visible -> showSignOutDialog = visible},
                    context = context,
                    navController = navController,
                    userViewModel = userViewModel

                )}
        }
    }
}

@Composable
fun SideBarItem(icon: ImageVector, text: String, context: Context, onClicked: () -> Unit) {
    Spacer(modifier = Modifier.height(10.dp))
    TextButton(onClick = onClicked) {
        Row(
            modifier = Modifier
                .height(30.dp)
                .fillMaxWidth(0.9f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                icon, "", tint = CC.textColor()
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text, style = CC.descriptionTextStyle(context))

        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignOut(
    userStatus: UserStateEntity?,
    onVisibleChange: (Boolean) -> Unit,
    context: Context,
    navController: NavController,
    userViewModel: UserViewModel
) {
    BasicAlertDialog(onDismissRequest = { onVisibleChange(false) }) {
        Column(
            modifier = Modifier
                .background(CC.primary(), RoundedCornerShape(10.dp))
                .width(300.dp)
                .height(200.dp)
                .padding(16.dp) // Added padding for better spacing
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Are you sure you want to sign out?",
                    style = CC.titleTextStyle(context),
                    textAlign = TextAlign.Center // Center-align the text
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Signing out will clear the app database.",
                style = CC.descriptionTextStyle(context),
                modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = {
                    userStatus?.copy(
                        online = "offline",
                        lastDate = CC.getTimeStamp(),
                        lastTime = CC.getTimeStamp()
                    )?.let { updatedUserStatus ->
                        MyDatabase.writeUserActivity(updatedUserStatus, onSuccess = { success ->
                            if (success) {
                                userViewModel.deleteAllTables()
                                userViewModel.deleteSignedInUser()
                                FirebaseAuth.getInstance().signOut()
                                navController.navigate("login") {
                                    popUpTo("homeScreen") { inclusive = true }
                                }
                                Toast.makeText(
                                    context,
                                    "Signed Out Successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onVisibleChange(false)
                            } else {
                                Toast.makeText(
                                    context,
                                    "Error signing out. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                    }
                }) {
                    Text("Sign Out", style = CC.descriptionTextStyle(context))
                }

                TextButton(onClick = { onVisibleChange(false) }) {
                    Text("Cancel", style = CC.descriptionTextStyle(context))
                }
            }
        }
    }
}

@Composable
fun SideProfile(user: UserEntity, context: Context) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                CC
                    .extraColor2()
                    .copy(0.5f)
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(15.dp))
        Box(
            modifier = Modifier
                .border(
                    1.dp, CC.textColor(), CircleShape
                )
                .clip(CircleShape)
                .background(CC.extraColor1(), CircleShape)
                .size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            if (user.profileImageLink.isNotEmpty()) {
                AsyncImage(
                    model = user.profileImageLink,
                    contentDescription = "Profile Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    "${user.firstName[0]}${user.lastName[0]}",
                    style = CC.titleTextStyle(context)
                        .copy(fontWeight = FontWeight.Bold, fontSize = 40.sp),
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            user.firstName + " " + user.lastName,
            style = CC.titleTextStyle(context)
                .copy(fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, fontSize = 18.sp),
            maxLines = 2
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(user.id, style = CC.descriptionTextStyle(context))
    }
}