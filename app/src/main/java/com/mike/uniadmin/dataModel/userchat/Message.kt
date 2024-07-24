package com.mike.uniadmin.dataModel.userchat

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey var id: String,
    var message: String? = null,
    var senderName: String? = null,
    var senderID: String? = null,
    var time: String? = null,
    var date: String? = null,
    var recipientID: String? = null,
    var profileImageLink: String? = null

)
{
    constructor() : this(
        "",
        null,
        null,
        null,
        null,
        null,
        null,
        null
    )
}