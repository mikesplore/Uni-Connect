package com.mike.uniadmin.dataModel.userchat

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey var id: String,
    var message: String = "",
    var senderID: String = "",
    var timeStamp: String = "",
    var date: String = "",
    var recipientID: String = "",
    var path: String = "",
    var deliveryStatus: DeliveryStatus = DeliveryStatus.SENT,

){
    constructor(): this("", "", "", "", "", "", "", DeliveryStatus.SENT)
}

enum class DeliveryStatus{
        SENT,
        READ,

}