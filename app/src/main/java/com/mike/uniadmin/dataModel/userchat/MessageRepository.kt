package com.mike.uniadmin.dataModel.userchat

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.dataModel.groupchat.ChatEntity
import com.mike.uniadmin.model.MyDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val viewModelScope = CoroutineScope(Dispatchers.Main)

class MessageRepository(private val messageDao: MessageDao) {
    private val database = FirebaseDatabase.getInstance().reference

    fun fetchMessages(path: String, onResult: (List<MessageEntity>) -> Unit) {
        viewModelScope.launch {
            val cachedChats = messageDao.getMessages(path)
            if (cachedChats.isNotEmpty()) {
                onResult(cachedChats)
            } else {
                    database.child(path).addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val messages = mutableListOf<MessageEntity>()
                            for (childSnapshot in snapshot.children) {
                                val message = childSnapshot.getValue(MessageEntity::class.java)
                                message?.let { messages.add(it) }
                            }
                            com.mike.uniadmin.dataModel.groupchat.viewModelScope.launch {
                                messageDao.insertMessages(messages)
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
    }

    fun saveMessage(message: MessageEntity, path: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            messageDao.insertMessages(listOf(message))
            database.child(path).push().setValue(message)
                .addOnCompleteListener {
                    onComplete(true)
                }
                .addOnFailureListener {
                    onComplete(false)
                }
        }
    }

    fun deleteMessage(messageId: String, onSuccess: () -> Unit, onFailure: (Exception?) -> Unit) {
        viewModelScope.launch {
        messageDao.deleteMessage(messageId)
        database.child(messageId).removeValue() // Use the consistent database reference
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }}
}