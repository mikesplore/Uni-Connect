package com.mike.uniadmin.backEnd.groupchat

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GroupChatDao {
    @Query("SELECT * FROM groupChats WHERE id LIKE '%' || :path || '%'")
    suspend fun getChats(path: String): List<GroupChatEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChats(chats: List<GroupChatEntity>)

    @Query("DELETE FROM groupChats WHERE id = :chatId")
    suspend fun deleteChat(chatId: String)
}

@Dao
interface GroupDao {
    @Query("SELECT * FROM groups")
    suspend fun getGroups(): List<GroupEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groups: List<GroupEntity>)

    @Query("DELETE FROM groups WHERE id = :groupId")
    suspend fun deleteGroup(groupId: String)
}
