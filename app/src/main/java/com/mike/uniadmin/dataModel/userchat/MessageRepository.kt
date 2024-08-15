package com.mike.uniadmin.dataModel.userchat

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val viewModelScope = CoroutineScope(Dispatchers.Main)

class MessageRepository(private val messageDao: MessageDao) {
    private val database = FirebaseDatabase.getInstance().reference

    fun fetchMessages(path: String, onResult: (List<MessageEntity>) -> Unit) {
        viewModelScope.launch {
            // Fetch messages from the local database first
            val cachedChats = messageDao.getMessages(path)
            Log.d("Cached Messages","The messages are not fetched")
            if (cachedChats.isNotEmpty()) {
                onResult(cachedChats)
            }

            // Set up a listener for real-time updates from Firebase
            database.child(path).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = mutableListOf<MessageEntity>()
                    for (childSnapshot in snapshot.children) {
                        val message = childSnapshot.getValue(MessageEntity::class.java)
                        message?.let { messages.add(it) }
                    }
                    viewModelScope.launch {
                        // Update local database with the new data from Firebase
                        messageDao.insertMessages(messages)
                        // Update the UI with the latest messages from Firebase
                        onResult(messages)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle the read error (e.g., log the error)
                    println("Error reading messages: ${error.message}")
                }
            })
        }
    }

    fun saveMessage(message: MessageEntity, path: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            // Save the message to the local database first
            messageDao.insertMessages(listOf(message))
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
            messageDao.deleteMessage(messageId)
            // Then delete the message from Firebase
            //the user may get delayed feedback
            database.child(path).child(messageId).removeValue()
                .addOnSuccessListener {
                    fetchMessages(path){
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

    fun markMessageAsDelivered(messageId: String, path: String) {
        viewModelScope.launch {
            // Update delivery status to DELIVERED in Firebase
            database.child(path).child(messageId).child("deliveryStatus")
                .setValue(DeliveryStatus.DELIVERED.name)

            // Update local database
            messageDao.updateMessageStatus(messageId, DeliveryStatus.DELIVERED)
        }
    }

    fun listenForMessageStatusUpdates(path: String, onStatusUpdate: (MessageEntity) -> Unit) {
        database.child(path).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    val message = childSnapshot.getValue(MessageEntity::class.java)
                    message?.let {
                        // Update the local database with the latest status
                        viewModelScope.launch {
                        messageDao.updateMessageStatus(it.id, it.deliveryStatus)
                        onStatusUpdate(it)
                    }}
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
            }
        })
    }


}
