package com.mike.uniadmin.dataModel.userchat

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.model.MyDatabase

class MessageRepository {
    private val database = FirebaseDatabase.getInstance().reference

    fun fetchMessages(path: String, onResult: (List<Message>) -> Unit) {
        database.child(path).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()
                for (childSnapshot in snapshot.children) {
                    val message = childSnapshot.getValue(Message::class.java)
                    message?.let { messages.add(it) }
                }
                onResult(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the read error (e.g., log the error)
                println("Error reading messages: ${error.message}")
            }
        })
    }

    fun saveMessage(message: Message, path: String, onComplete: (Boolean) -> Unit) {
        MyDatabase.database.child(path).push().setValue(message)
            .addOnCompleteListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun deleteMessage(messageId: String, onSuccess: () -> Unit, onFailure: (Exception?) -> Unit) {
        database.child(messageId).removeValue() // Use the consistent database reference
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}