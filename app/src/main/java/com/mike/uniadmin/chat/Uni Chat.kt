package com.mike.uniadmin.chat

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerDefaults
import com.google.accompanist.pager.PagerState
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.ui.theme.CommonComponents
import com.mike.uniadmin.dataModel.groupchat.ChatViewModel
import com.mike.uniadmin.dataModel.groupchat.UniAdmin
import com.mike.uniadmin.dataModel.users.UserEntity
import com.mike.uniadmin.dataModel.users.UserViewModel
import com.mike.uniadmin.dataModel.users.UserViewModelFactory
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@OptIn(
    ExperimentalPagerApi::class,
    ExperimentalSnapperApi::class,
    ExperimentalMaterial3Api::class,
)
@Composable
fun UniChat(
    navController: NavController,
    context: Context,
    pagerState: PagerState,
    screens: List<UniScreen>,
    coroutineScope: CoroutineScope,
) {
    val currentPerson = FirebaseAuth.getInstance().currentUser
    val uniAdmin = context.applicationContext as? UniAdmin
    val chatRepository =
        uniAdmin?.chatRepository ?: throw IllegalStateException("ChatRepository not initialized")
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

    val user by userViewModel.user.observeAsState()
    val signedInUser = remember { mutableStateOf<UserEntity?>(null) }
    var expanded by remember { mutableStateOf(false) }


    LaunchedEffect(currentPerson?.email) {
        
        currentPerson?.email?.let { email ->
            userViewModel.findUserByEmail(email) {}
        }
    }

    LaunchedEffect(user) {
        user?.let {
            signedInUser.value = it
            userViewModel.checkAllUserStatuses()
            userViewModel.fetchUsers()
            chatViewModel.fetchGroups()
            userViewModel.checkUserStateByID(it.id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Uni Chat", style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.ExtraBold, fontSize = 30.sp))
                },
                actions = {
                    DropdownMenu(
                        modifier = Modifier
                            .background(CC.secondary())
                            .width(150.dp),
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        MyDropDownMenuItem(
                            icon = Icons.Default.GroupAdd,
                            text = "New group",
                            context = context,
                            expanded = expanded,
                            onClick = {
                                navController.navigate("addgroup")
                                expanded = it }
                        )
                        MyDropDownMenuItem(
                            icon = Icons.Default.GroupAdd,
                            text = "New group",
                            context = context,
                            expanded = expanded,
                            onClick = { expanded = it }
                        )
                    }
                    IconButton(onClick = {expanded = !expanded}) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = CC.textColor()
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CC.primary(),
                    titleContentColor = CC.textColor()
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .height(85.dp)
                    .background(Color.Transparent),
                containerColor = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            CommonComponents
                                .primary()
                                .copy()
                        ),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    screens.forEachIndexed { index, screen ->
                        val isSelected = pagerState.currentPage == index

                        val iconColor by animateColorAsState(
                            targetValue = if (isSelected) CommonComponents.textColor() else CommonComponents.textColor()
                                .copy(0.7f), label = "", animationSpec = tween(500)
                        )

                        // Use NavigationBarItem
                        NavigationBarItem(selected = isSelected, label = {
                            AnimatedVisibility(visible = isSelected,
                                enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
                                    animationSpec = tween(500)
                                ) { initialState -> initialState },
                                exit = fadeOut(animationSpec = tween(500)) + slideOutVertically(
                                    animationSpec = tween(500)
                                ) { initialState -> initialState }) {
                                Text(
                                    text = screen.name,
                                    style = CommonComponents.descriptionTextStyle(context)
                                        .copy(fontSize = 13.sp),
                                    color = CommonComponents.textColor()
                                )
                            }
                        }, colors = NavigationBarItemDefaults.colors(
                            indicatorColor = CommonComponents.extraColor2(),
                            unselectedIconColor = CommonComponents.textColor(),
                            selectedIconColor = CommonComponents.textColor()
                        ), onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }, icon = {
                            Column (
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.name,
                                    tint = iconColor,
                                    modifier = Modifier.size(25.dp)
                                )
                            }
                        })
                    }
                }
            }
        }, containerColor = CommonComponents.primary()
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            count = screens.size,
            modifier = Modifier.padding(innerPadding),
            flingBehavior = PagerDefaults.flingBehavior(state = pagerState)
        ) { page ->
            when (screens[page]) {
                UniScreen.Chats -> ParticipantsScreen(navController, context)
                UniScreen.Groups -> UniGroups(context, navController)
                UniScreen.Status -> SearchGroup(navController, context)
            }
        }
    }
}


@Composable
fun MyDropDownMenuItem(icon: ImageVector, text: String, context: Context, expanded: Boolean, onClick: (Boolean) -> Unit){
    DropdownMenuItem(
        text = {
            Row(modifier = Modifier
                .padding(start = 5.dp)
                .fillMaxWidth()) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = CC.textColor()
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("New group", style = CC.descriptionTextStyle(context))

            }
        },
        onClick = {
            onClick(!expanded)
        }
    )
}