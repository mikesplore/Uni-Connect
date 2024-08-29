package com.mike.uniadmin.backEnd.groupchat

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mike.uniadmin.backEnd.users.UserEntity

fun generateConversationId(userId1: String, userId2: String): String {

    return if (userId1 < userId2) {
        "$userId1$userId2"
    } else {
        "$userId2$userId1"
    }
}


@Entity(tableName = "groupChats")
data class GroupChatEntity(
    @PrimaryKey val chatId: String,
    var message: String = "",
    var senderID: String = "",
    var date: String = "",
){
    constructor(): this("", "","", "")
}

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey val id: String,
    val admin: String = "",
    var name: String = "",
    var description: String = "",
    var groupImageLink: String = "",
    var members: List<String> = emptyList()
){
    constructor(): this("", "", "", "", "", emptyList())
}

data class GroupChatEntityWithDetails(
    @Embedded val groupChat: GroupChatEntity,
    val senderName: String = "",
    val senderProfileImageLink: String = ""
    )

