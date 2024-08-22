package com.mike.uniadmin.backEnd.userchat

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserChatDAO {
    @Query("SELECT * FROM userChats WHERE path = :path")
    suspend fun getUserChats(path: String): List<UserChatEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserChats(userChats: List<UserChatEntity>)

    @Query("DELETE FROM userChats WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("UPDATE userChats SET deliveryStatus = :newStatus WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, newStatus: DeliveryStatus)
}
