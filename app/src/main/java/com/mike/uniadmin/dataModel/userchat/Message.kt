package com.mike.uniadmin.dataModel.userchat

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey var id: String,
    var message: String = "",
    var senderName: String = "",
    var senderID: String = "",
    var timeStamp: String = "",
    var date: String = "",
    var recipientID: String = "",
    var profileImageLink: String = ""

){
    constructor(): this("", "", "", "", "", "", "", "")
}
