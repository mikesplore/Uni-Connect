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
    SELECT 
    uc.*,
    sender.id AS sender_id,
    sender.firstName AS sender_firstName,
    sender.lastName AS sender_lastName,
    sender.email AS sender_email,
    sender.phoneNumber AS sender_phoneNumber,
    sender.profileImageLink AS sender_profileImageLink,
    sender.userType AS sender_userType,
    
    IFNULL (senderState.online, 'offline') AS senderState,
    receiver.id AS receiver_id,
    receiver.firstName AS receiver_firstName,
    receiver.lastName AS receiver_lastName,
    receiver.email AS receiver_email,
    receiver.phoneNumber AS receiver_phoneNumber,
    receiver.profileImageLink AS receiver_profileImageLink,
    receiver.userType AS receiver_userType,
    IFNULL (receiverState.online, 'offline') AS receiverState,
    unreadCounts.unreadCount
FROM 
    userChats uc
INNER JOIN 
    admins AS sender ON uc.senderID = sender.id
LEFT JOIN 
    admins AS receiver ON uc.recipientID = receiver.id
LEFT JOIN 
    userState AS senderState ON sender.id = senderState.userID
LEFT JOIN 
    userState AS receiverState ON receiver.id = receiverState.userID
LEFT JOIN 
    (
        -- Subquery to find the latest message for each sender-recipient pair
        SELECT 
            senderID, 
            recipientID, 
            MAX(timeStamp) AS latestTimestamp
        FROM 
            userChats
        GROUP BY 
            senderID, recipientID
    ) latestChats ON uc.senderID = latestChats.senderID 
                  AND uc.recipientID = latestChats.recipientID 
                  AND uc.timeStamp = latestChats.latestTimestamp
LEFT JOIN 
    (
        -- Subquery to count unread messages for the current user
        SELECT 
            senderID, 
            COUNT(*) AS unreadCount
        FROM 
            userChats
        WHERE 
            deliveryStatus != 'READ' 
            AND recipientID = :currentUserId
        GROUP BY 
            senderID
    ) unreadCounts ON uc.senderID = unreadCounts.senderID
WHERE 
    latestChats.latestTimestamp IS NOT NULL  -- Ensure only the latest messages are included
ORDER BY 
    uc.timeStamp DESC;


    """
    )
    fun getLatestUserChats(currentUserId: String = UniAdminPreferences.userID.value): LiveData<List<UserChatsWithDetails>>




}
