package com.mike.uniadmin.homeScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mike.uniadmin.backEnd.groupchat.GroupChatViewModel
import com.mike.uniadmin.backEnd.users.UserViewModel
import com.mike.uniadmin.helperFunctions.Screen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BottomBar(
    screens: List<Screen>,
    pagerState: PagerState,
    coroutineScope: CoroutineScope,
    drawerState: DrawerState,
    userViewModel: UserViewModel,
    chatViewModel: GroupChatViewModel,
    scope: CoroutineScope,
    showBottomSheet: (Boolean) -> Unit
) {
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
                    CC
                        .primary()
                        .copy()
                ),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            screens.forEachIndexed { index, screen ->
                val isSelected = pagerState.currentPage == index

                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) CC.extraColor1() else CC.extraColor2()
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
                            style = CC.descriptionTextStyle()
                                .copy(fontSize = 13.sp),
                            color = CC.textColor()
                        )
                    }
                }, colors = NavigationBarItemDefaults.colors(
                    indicatorColor = CC.extraColor2(),
                    unselectedIconColor = CC.textColor(),
                    selectedIconColor = CC.primary()
                ), onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }, icon = {
                    Column(modifier = Modifier.combinedClickable(onLongClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    }, onDoubleClick = {
                        userViewModel.fetchUsers()
                        chatViewModel.fetchGroups()
                        showBottomSheet(true)
                    }) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
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

}