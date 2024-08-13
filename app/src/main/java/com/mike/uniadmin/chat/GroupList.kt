package com.mike.uniadmin.chat

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mike.uniadmin.dataModel.groupchat.ChatViewModel
import com.mike.uniadmin.dataModel.groupchat.GroupEntity
import com.mike.uniadmin.dataModel.groupchat.UniAdmin
import com.mike.uniadmin.dataModel.users.UserEntity
import com.mike.uniadmin.dataModel.users.UserViewModel
import com.mike.uniadmin.dataModel.users.UserViewModelFactory
import com.mike.uniadmin.model.MyDatabase
import com.mike.uniadmin.ui.theme.CommonComponents as CC

object GroupDetails {
    var groupName: MutableState<String?> = mutableStateOf("")
    var groupImageLink: MutableState<String?> = mutableStateOf("")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniGroups(context: Context, navController: NavController) {
    val application = context.applicationContext as UniAdmin
    val chatRepository = remember { application.chatRepository }
    val chatViewModel: ChatViewModel = viewModel(
        factory = ChatViewModel.ChatViewModelFactory(chatRepository)
    )
    val userAdmin = context.applicationContext as? UniAdmin
    val userRepository = remember { userAdmin?.userRepository }
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(
            userRepository ?: throw IllegalStateException("UserRepository is null")
        )
    )

    val groups by chatViewModel.groups.observeAsState(emptyList())
    val users by userViewModel.users.observeAsState(emptyList())
    val signedInUser by userViewModel.signedInUser.observeAsState(initial = null)
    val fetchedUserDetails by userViewModel.user.observeAsState()
    var showAddGroup by remember { mutableStateOf(false) }

    LaunchedEffect(signedInUser) {
        userViewModel.getSignedInUser()
        signedInUser?.email?.let { email ->
            userViewModel.findUserByEmail(email) {}
            chatViewModel.fetchGroups()
        }
    }



    val userGroups = groups.filter { it.members.contains(fetchedUserDetails?.id) }
    Scaffold(topBar = {
        IconButton(
            onClick = {showAddGroup = !showAddGroup}) {
            Icon(
                imageVector = if (showAddGroup) Icons.Default.Close else Icons.Default.Add,
                contentDescription = "Add Group",
                tint = CC.textColor()
            )
        }
    }) {
        Column(
            modifier = Modifier
                .background(CC.primary())
                .fillMaxSize()
                .padding(it)
        ) {

            AnimatedVisibility(
                visible = showAddGroup,
                enter = expandVertically(animationSpec = tween(durationMillis = 300)),
                exit = shrinkVertically(animationSpec = tween(durationMillis = 300))
            ) {
                fetchedUserDetails?.let { it1 ->
                    AddGroupSection(
                        it1,
                        context,
                        chatViewModel,
                        users,
                        onComplete = { showAddGroup = false })
                }
            }
            if (userGroups.isEmpty()) {
                Text(
                    text = "No groups available",
                    style = CC.descriptionTextStyle(context).copy(fontSize = 18.sp),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.animateContentSize()
                ) {
                    items(userGroups) { group ->
                        if (group.name.isNotEmpty() && group.description.isNotEmpty()) {
                            fetchedUserDetails?.let { it1 ->
                                GroupItem(
                                    group,
                                    context,
                                    navController,
                                    chatViewModel,
                                    userViewModel,
                                    it1
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddGroupSection(
    signedInUser: UserEntity,
    context: Context,
    chatViewModel: ChatViewModel,
    users: List<UserEntity>,
    onComplete: (Boolean) -> Unit = {}
) {
    var groupName by remember { mutableStateOf("") }
    var groupDescription by remember { mutableStateOf("") }
    var selectedMembers by remember { mutableStateOf(setOf(signedInUser.id)) } // Include the admin by default
    var imageLink by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .background(CC.primary())
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        // Title for the group creation section
        Text(
            text = "Create New Group",
            style = CC.titleTextStyle(context).copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = CC.textColor()
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Group Name Input
        CC.SingleLinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(CC.secondary(), shape = RoundedCornerShape(10.dp))
                .padding(8.dp),
            value = groupName,
            onValueChange = { groupName = it },
            label = "Group Name",
            enabled = true,
            singleLine = true,
            context = context
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Group Description Input
        CC.SingleLinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(CC.secondary(), shape = RoundedCornerShape(10.dp))
                .padding(8.dp),
            value = groupDescription,
            onValueChange = { groupDescription = it },
            label = "Description",
            enabled = true,
            singleLine = true,
            context = context
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Group Image Link Input
        CC.SingleLinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(CC.secondary(), shape = RoundedCornerShape(10.dp))
                .padding(8.dp),
            value = imageLink,
            onValueChange = { imageLink = it },
            label = "Image Link",
            enabled = true,
            singleLine = true,
            context = context
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Members Selection Title
        Text(
            text = "Choose Your Members",
            style = CC.descriptionTextStyle(context).copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = CC.textColor()
            ),
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Users LazyRow for member selection
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(CC.secondary(), shape = RoundedCornerShape(10.dp))
                .padding(8.dp)
        ) {
            items(users) { user ->
                UserSelectionItem(
                    context = context,
                    user = user,
                    isSelected = selectedMembers.contains(user.id),
                    onClick = {
                        selectedMembers = if (selectedMembers.contains(user.id)) {
                            selectedMembers - user.id
                        } else {
                            selectedMembers + user.id
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Create Group Button
        Button(
            onClick = {
                if (groupName.isBlank() || groupDescription.isBlank()) {
                    Toast.makeText(
                        context,
                        "Please enter group name and description",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }
                isLoading = true
                MyDatabase.generateGroupId { id ->
                    val newGroup = GroupEntity(
                        id = id,
                        name = groupName,
                        description = groupDescription,
                        groupImageLink = imageLink,
                        admin = signedInUser.id,
                        members = selectedMembers.toList() // The admin is already included
                    )
                    chatViewModel.saveGroup(newGroup, onSuccess = {
                        isLoading = false
                        groupDescription = ""
                        onComplete(true)
                        groupName = ""
                        imageLink = ""
                        selectedMembers = setOf(signedInUser.id) // Reset with admin included
                        if (it) {
                            chatViewModel.fetchGroups()
                        }
                    })
                }
            },
            modifier = Modifier
                .align(Alignment.End)
                .clip(RoundedCornerShape(10.dp))
                .background(CC.extraColor1(), shape = RoundedCornerShape(10.dp))
                .padding(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CC.extraColor1(),
                contentColor = CC.textColor()
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = CC.textColor(), modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Create", style = CC.descriptionTextStyle(context))
            }
        }
    }
}



@Composable
fun UserSelectionItem(
    context: Context,
    user: UserEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(100.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize()
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier
                    .background(CC.extraColor1(), CircleShape)
                    .border(1.dp, CC.primary(), CircleShape)
                    .clip(CircleShape)
                    .size(50.dp),
                    contentAlignment = Alignment.Center){
                if (user.profileImageLink.isNotBlank()) {
                    AsyncImage(
                        model = user.profileImageLink,
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .clip(CircleShape)
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("${user.firstName[0]}${user.lastName[0]}")
                }}
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.firstName,
                    style = CC.descriptionTextStyle(context),
                    maxLines = 1
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White
                    )
                }
            }
        }
    }
}




@Composable
fun EditGroupSection(
    group: GroupEntity,
    context: Context,
    chatViewModel: ChatViewModel,
    users: List<UserEntity>,
    onDismiss: () -> Unit
) {
    var groupName by remember { mutableStateOf(group.name) }
    var groupDescription by remember { mutableStateOf(group.description) }
    var selectedMembers by remember { mutableStateOf(group.members.toSet()) }
    var expanded by remember { mutableStateOf(false) }
    var imageLink by remember { mutableStateOf(group.groupImageLink) }

    Column(modifier = Modifier.fillMaxWidth()) {
        groupName.let {
            CC.SingleLinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = it,
                onValueChange = { newValue ->
                    groupName = newValue },
                label = "Group Name",
                enabled = true,
                singleLine = true,
                context = context
            )
        }
        groupDescription.let {
            CC.SingleLinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = it,
                onValueChange = {
                    newValue ->   groupDescription = newValue },
                label = "Description",
                enabled = true,
                singleLine = true,
                context = context
            )
        }
        imageLink.let {
            CC.SingleLinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = it,
                onValueChange = {
                        newValue ->   imageLink = newValue },
                label = "Image link",
                enabled = true,
                singleLine = true,
                context = context
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { expanded = !expanded },
            colors = ButtonDefaults.buttonColors(
                containerColor = CC.extraColor2(), contentColor = CC.textColor()
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.width(200.dp)
        ) {
            Text(
                "Select Members",
                modifier = Modifier.padding(8.dp),
                style = CC.descriptionTextStyle(context)
            )
        }

        AnimatedVisibility(visible = expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                modifier = Modifier.background(CC.secondary(), RoundedCornerShape(10.dp))
            ) {
                items(users) { user ->
                    UserSelectionItem(
                        context,
                        user = user,
                        isSelected = selectedMembers.contains(user.id),
                        onClick = {
                            selectedMembers = if (selectedMembers.contains(user.id)) {
                                selectedMembers.minus(user.id)
                            } else {
                                selectedMembers.plus(user.id)
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val updatedGroup = group.copy(
                    name = groupName,
                    description = groupDescription,
                    groupImageLink = imageLink,
                    members = selectedMembers.toList()
                )
                chatViewModel.saveGroup(updatedGroup, onSuccess = {
                    if (it) {
                        chatViewModel.fetchGroups()
                    }
                })
                onDismiss()
            },
            modifier = Modifier.align(Alignment.End),
            colors = ButtonDefaults.buttonColors(
                containerColor = CC.extraColor1(), contentColor = CC.textColor()
            )
        ) {
            Text("Save Changes", style = CC.descriptionTextStyle(context))
        }
    }
}


@Composable
fun GroupItem(
    group: GroupEntity,
    context: Context,
    navController: NavController,
    chatViewModel: ChatViewModel,
    userViewModel: UserViewModel,
    user: UserEntity
) {
    var showEditGroup by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp)
            .clickable {
                GroupDetails.groupName.value = group.name
                GroupDetails.groupImageLink.value = group.groupImageLink
                navController.navigate("GroupChat/${group.id}")
            }, colors = CardDefaults.cardColors(containerColor = CC.primary())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CC.secondary(), CircleShape)
                    .size(50.dp)
            ) {
                if (group.groupImageLink.isNotBlank()) {
                    AsyncImage(
                        model = group.groupImageLink,
                        contentDescription = "Group Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Groups,
                        "Group Image",
                        tint = CC.textColor(),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                if (group.admin == user.id) {
                    IconButton(onClick = { showEditGroup = true }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Group",
                            tint = CC.textColor()
                        )
                    }
                }

            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = group.name, style = CC.titleTextStyle(context), maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = group.description, style = CC.descriptionTextStyle(context).copy(color = CC.textColor().copy(0.5f)),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    if (showEditGroup) {
        AlertDialog(containerColor = CC.primary(),
            onDismissRequest = { showEditGroup = false },
            title = { Text("Edit Group", style = CC.titleTextStyle(context)) },
            text = {
                userViewModel.users.value?.let {
                    EditGroupSection(group = group,
                        context = context,
                        chatViewModel = chatViewModel,
                        users = it,
                        onDismiss = { showEditGroup = false })
                }
            },
            confirmButton = {
                Button(
                    onClick = { showEditGroup = false }, colors = ButtonDefaults.buttonColors(
                        containerColor = CC.tertiary(), contentColor = CC.textColor()
                    )
                ) {
                    Text("Close", style = CC.descriptionTextStyle(context))
                }
            })
    }
}

