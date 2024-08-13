package com.mike.uniadmin.dataModel.userchat

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
}
