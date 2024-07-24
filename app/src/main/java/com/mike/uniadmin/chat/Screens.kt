package com.mike.uniadmin.chat

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class UniScreen(
    val selectedIcon: ImageVector, val unselectedIcon: ImageVector, val name: String
) {
    data object Chats : UniScreen(
        Icons.AutoMirrored.Filled.Message, Icons.AutoMirrored.Outlined.Message, "Chats"
    )

    data object Groups : UniScreen(Icons.Filled.Groups, Icons.Outlined.Groups, "Groups")

    data object Status : UniScreen(
        Icons.Filled.Search, Icons.Outlined.Search, "Search"
    )

}