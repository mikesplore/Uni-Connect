package com.mike.uniadmin.dataModel.groupchat
import androidx.room.Entity
import androidx.room.PrimaryKey
import android.app.Application

fun generateConversationId(userId1: String, userId2: String): String {
    return if (userId1 < userId2) {
        "$userId1$userId2"
    } else {
        "$userId2$userId1"
    }
}



@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    var message: String = "",
    var senderName: String = "",
    var senderID: String = "",
    var time: String = "",
    var date: String = "",
    var profileImageLink: String = ""
)

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey val id: String,
    val admin: String = "",
    var name: String = "",
    var description: String = "",
    var groupImageLink: String = "",
    var members: List<String> = emptyList()
)


class UniAdmin : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val chatRepository by lazy { ChatRepository(database.chatDao(), database.groupDao()) }
}

