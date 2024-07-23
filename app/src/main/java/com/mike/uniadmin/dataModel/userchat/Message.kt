package com.mike.uniadmin.dataModel.userchat

data class Message(
    var id: String = "",
    var message: String = "",
    var senderName: String = "",
    var senderID: String = "",
    var time: String = "",
    var date: String = "",
    var recipientID: String = "",
    var profileImageLink: String = ""

)