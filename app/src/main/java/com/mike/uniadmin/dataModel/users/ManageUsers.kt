package com.mike.uniadmin.dataModel.users

import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mike.uniadmin.dataModel.groupchat.UniAdmin

import com.mike.uniadmin.ui.theme.CommonComponents as CC

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageUsers(navController: NavController, context: Context) {
    val userAdmin = context.applicationContext as? UniAdmin
    val userRepository = remember { userAdmin?.userRepository }
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(
            userRepository ?: throw IllegalStateException("UserRepository is null")
        )
    )
    val users by userViewModel.users.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("homeScreen")
                    }) {
                        Icon(Icons.Default.ArrowBackIosNew,"back",
                            tint = CC.textColor())
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CC.primary()
                )

            )
        },
        containerColor = CC.primary()
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(it),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(modifier = Modifier
            .height(100.dp)
            .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
            Text("Manage Users", style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold, fontSize = 30.sp))
        }
        LazyColumn {
            items(users) { user ->
                UserCard(
                    user = user,
                    context,
                    userViewModel
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
    }
}

@Composable
fun UserCard(user: UserEntity, context: Context, userViewModel: UserViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(0.9f),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = CC.extraColor1()
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .border(
                        1.dp, CC.textColor(), CircleShape
                    )
                    .clip(CircleShape)
                    .background(CC.secondary(), CircleShape)
                    .size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                if (user.profileImageLink?.isNotEmpty() == true) {
                    AsyncImage(
                        model = user.profileImageLink,
                        contentDescription = "Profile Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        "${user.profileImageLink?.get(0)}",
                        style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold),
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "${user.firstName} ${user.lastName}",
                style = CC.descriptionTextStyle(context),
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = {
                userViewModel.deleteAccount(user.id, onSuccess = { success ->
                    if (success){
                    userViewModel.fetchUsers()}
                    else{
                        Toast.makeText(context,"Failed to delete user", Toast.LENGTH_SHORT).show()
                    }
                })

            }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete User",
                    tint = Color.Red
                )
            }
        }
    }
}

