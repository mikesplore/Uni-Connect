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
    var message: String? = null,
    var senderName: String? = null,
    var senderID: String? = null,
    var time: String? = null,
    var date: String? = null,
    var profileImageLink: String? = null
){
    constructor() : this(
        "",
        null,
        null,
        null,
        null,
        null,
        null
    )
}

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey val id: String,
    val admin: String? = null,
    var name: String? = null,
    var description: String? = null,
    var groupImageLink: String? = null,
    var members: List<String>? = null
) {
    constructor() : this(
        "",
        null,
        null,
        null,
        null,
        null
    )
}


class UniAdmin : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val chatRepository by lazy { ChatRepository(database.chatDao(), database.groupDao()) }
}
