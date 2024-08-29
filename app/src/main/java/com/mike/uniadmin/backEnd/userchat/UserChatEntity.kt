package com.mike.uniadmin.backEnd.userchat

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mike.uniadmin.backEnd.users.UserEntity

@Entity(tableName = "userChats")
data class UserChatEntity(
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



data class UserChatsWithDetails(
    @Embedded val userChat: UserChatEntity,
    @Embedded(prefix = "sender_") val sender: UserEntity, // Maps sender details
    @Embedded(prefix = "receiver_") val receiver: UserEntity, // Maps receiver details
    val senderState: String?, // Sender's state from userState table
    val receiverState: String?, // Receiver's state from userState table
    val unreadCount: Int? = 0 // Optional, in case there are no unread counts
)




enum class DeliveryStatus{
    SENT,
    READ,

}