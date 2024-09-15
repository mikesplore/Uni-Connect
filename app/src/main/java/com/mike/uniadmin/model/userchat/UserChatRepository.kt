package com.mike.uniadmin.model.userchat

import android.util.Log
import androidx.lifecycle.LiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.model.announcements.uniConnectScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class UserChatRepository(private val userChatDAO: UserChatDAO) {
    private val database = FirebaseDatabase.getInstance().reference

    fun fetchUserChats(path: String, onResult: (List<UserChatEntity>) -> Unit) {
        // Log.d("UserChatRepository","Fetching from path: $path")

        // Set up a listener for real-time updates from Firebase
        database.child(path).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userChats = mutableListOf<UserChatEntity>()
                for (childSnapshot in snapshot.children) {
                    val message = childSnapshot.getValue(UserChatEntity::class.java)
                    message?.let { userChats.add(it) }
                }
                onResult(userChats)


                // Insert into database and update UI with Main dispatcher
                uniConnectScope.launch(Dispatchers.IO) {
                    userChatDAO.insertUserChats(userChats)
                    withContext(Dispatchers.Main) {
                        onResult(userChats)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UserChatRepository", "Error reading userChats: ${error.message}")
            }
        })

    }


    fun getLatestUserChats(): LiveData<List<UserChatsWithDetails>> {
        return userChatDAO.getLatestUserChats()

    }


    fun saveMessage(message: UserChatEntity, path: String, onComplete: (Boolean) -> Unit) {
        // Use IO dispatcher for saving the message
        uniConnectScope.launch(Dispatchers.IO) {
            // Save the message to the local database first
            userChatDAO.insertUserChats(listOf(message))
            // Then save the message to Firebase using the message ID
            database.child(path).child(message.id).setValue(message)
                .addOnCompleteListener {
                    onComplete(true)
                }
                .addOnFailureListener {
                    onComplete(false)
                }
        }
    }

    fun deleteMessage(
        messageId: String,
        path: String,
        onSuccess: () -> Unit,
        onFailure: (Exception?) -> Unit
    ) {
        // Use IO dispatcher for deletion
        uniConnectScope.launch(Dispatchers.IO) {
            // Delete the message from the local database
            userChatDAO.deleteMessage(messageId)
        }
        // Then delete the message from Firebase
        database.child(path).child(messageId).removeValue()
            .addOnSuccessListener {
                fetchUserChats(path) {
                    onSuccess()
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }

    }

    fun updateTypingStatus(path: String, userID: String, isTyping: Boolean) {
        database.child(path).child(userID).child("typingStatus").setValue(isTyping)
    }

    fun listenForTypingStatus(
        path: String,
        userID: String,
        onTypingStatusChanged: (Boolean) -> Unit
    ) {
        database.child(path).child(userID).child("typingStatus")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val isTyping = snapshot.getValue(Boolean::class.java) ?: false
                    onTypingStatusChanged(isTyping)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error if needed
                }
            })
    }


    fun markMessageAsRead(messageId: String, path: String) {
        // Use IO dispatcher for updating the message status
        uniConnectScope.launch(Dispatchers.IO) {
            // Update delivery status to READ in Firebase
            database.child(path).child(messageId).child("deliveryStatus")
                .setValue(DeliveryStatus.READ.name)

            // Update local database
            userChatDAO.updateMessageStatus(messageId, DeliveryStatus.READ)
        }
    }
}

