package com.mike.uniadmin.backEnd.userchat

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.notification.showNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val viewModelScope = CoroutineScope(Dispatchers.Main)

class UserChatRepository(private val userChatDAO: UserChatDAO) {
    private val database = FirebaseDatabase.getInstance().reference

    fun fetchUserChats(path: String, onResult: (List<UserChatEntity>) -> Unit) {
        viewModelScope.launch {
            // Fetch userChats from the local database first
            val cachedChats = userChatDAO.getUserChats(path)
            Log.d("Cached UserChats","The userChats are not fetched")
            if (cachedChats.isNotEmpty()) {
                onResult(cachedChats)
            }

            // Set up a listener for real-time updates from Firebase
            database.child(path).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userChats = mutableListOf<UserChatEntity>()
                    for (childSnapshot in snapshot.children) {
                        val message = childSnapshot.getValue(UserChatEntity::class.java)
                        message?.let { userChats.add(it) }
                    }
                    viewModelScope.launch(Dispatchers.IO) {
                        userChatDAO.insertUserChats(userChats)
                    }

                    // Call onResult on the main thread
                    viewModelScope.launch(Dispatchers.Main) {
                        onResult(userChats)
                    }
                }


                override fun onCancelled(error: DatabaseError) {
                    // Handle the read error (e.g., log the error)
                    println("Error reading userChats: ${error.message}")
                }
            })
        }
    }

    fun saveMessage(message: UserChatEntity, path: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            // Save the message to the local database first
            userChatDAO.insertUserChats(listOf(message))
            Log.d("Message Saved","The message is saved in path $path")

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

    fun deleteMessage(messageId: String, path: String, onSuccess: () -> Unit, onFailure: (Exception?) -> Unit) {
        viewModelScope.launch {
            // Delete the message from the local database
            userChatDAO.deleteMessage(messageId)
            // Then delete the message from Firebase
            //the user may get delayed feedback
            database.child(path).child(messageId).removeValue()
                .addOnSuccessListener {
                    fetchUserChats(path){
                        onSuccess()
                    }
                }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }
        }
    }


    fun updateTypingStatus(path: String, userID: String, isTyping: Boolean) {
        database.child(path).child(userID).child("typingStatus").setValue(isTyping)
    }

    fun listenForTypingStatus(path: String, userID: String, onTypingStatusChanged: (Boolean) -> Unit) {
        database.child(path).child(userID).child("typingStatus").addValueEventListener(object : ValueEventListener {
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
        viewModelScope.launch {
            // Update delivery status to DELIVERED in Firebase
            database.child(path).child(messageId).child("deliveryStatus")
                .setValue(DeliveryStatus.READ.name)

            // Update local database
            userChatDAO.updateMessageStatus(messageId, DeliveryStatus.READ)
        }
    }

}
