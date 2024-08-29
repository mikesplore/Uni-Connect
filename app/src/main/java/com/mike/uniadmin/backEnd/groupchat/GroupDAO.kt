package com.mike.uniadmin.backEnd.groupchat

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GroupChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChats(chats: List<GroupChatEntity>)

    @Query("DELETE FROM groupChats WHERE chatId = :chatId")
    suspend fun deleteChat(chatId: String)

    @Query(
        "SELECT" +
                "    gc.*," +
                "    a.firstName AS senderName," +
                "    a.profileImageLink AS senderProfileImageLink\n" +
                "FROM groupChats gc\n" +
                "INNER JOIN admins a ON gc.senderID = a.id;"
    )
     fun getChatsWithDetails(): LiveData<List<GroupChatEntityWithDetails>>
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
