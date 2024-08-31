package com.mike.uniadmin.backEnd.userchat

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mike.uniadmin.UniAdminPreferences

@Dao
interface UserChatDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserChats(userChats: List<UserChatEntity>)

    @Query("DELETE FROM userChats WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("UPDATE userChats SET deliveryStatus = :newStatus WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, newStatus: DeliveryStatus)

    @Query(
        """
    WITH LastMessages AS (
    SELECT 
        path,
        MAX(timeStamp) AS latestTimestamp
    FROM 
        userChats
    GROUP BY 
        path
)
SELECT 
    uc.id, uc.message, uc.senderID, uc.timeStamp, uc.date, uc.recipientID, uc.path, uc.deliveryStatus,
    sender.id AS sender_id, sender.firstName AS sender_firstName, sender.lastName AS sender_lastName,
    sender.email AS sender_email, sender.phoneNumber AS sender_phoneNumber,
    sender.profileImageLink AS sender_profileImageLink, sender.userType AS sender_userType,
    receiver.id AS receiver_id, receiver.firstName AS receiver_firstName, receiver.lastName AS receiver_lastName,
    receiver.email AS receiver_email, receiver.phoneNumber AS receiver_phoneNumber,
    receiver.profileImageLink AS receiver_profileImageLink, receiver.userType AS receiver_userType,
    IFNULL(senderState.online, 'offline') AS senderState,
    IFNULL(receiverState.online, 'offline') AS receiverState,
    IFNULL(unreadCounts.unreadCount, 0) AS unreadCount
FROM 
    userChats uc
INNER JOIN 
    LastMessages lm ON uc.path = lm.path AND uc.timeStamp = lm.latestTimestamp
INNER JOIN 
    users AS sender ON uc.senderID = sender.id
INNER JOIN 
    users AS receiver ON uc.recipientID = receiver.id
LEFT JOIN 
    userState AS senderState ON sender.id = senderState.userID
LEFT JOIN 
    userState AS receiverState ON receiver.id = receiverState.userID
LEFT JOIN 
    (
        SELECT 
            recipientID, 
            COUNT(*) AS unreadCount
        FROM 
            userChats
        WHERE 
            deliveryStatus != 'READ' 
            AND recipientID = :currentUserId
        GROUP BY 
            recipientID
    ) unreadCounts ON uc.recipientID = unreadCounts.recipientID
WHERE 
    uc.senderID = :currentUserId OR uc.recipientID = :currentUserId
ORDER BY 
    uc.timeStamp DESC


    """
    )
    fun getLatestUserChats(currentUserId: String = UniAdminPreferences.userID.value): LiveData<List<UserChatsWithDetails>>

}
