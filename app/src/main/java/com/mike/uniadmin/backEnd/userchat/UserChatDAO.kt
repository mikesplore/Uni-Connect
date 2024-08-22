package com.mike.uniadmin.backEnd.userchat

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserChatDAO {
    @Query("SELECT * FROM messages WHERE path = :path")
    suspend fun getMessages(path: String): List<UserChatEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<UserChatEntity>)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("UPDATE messages SET deliveryStatus = :newStatus WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, newStatus: DeliveryStatus)
}
