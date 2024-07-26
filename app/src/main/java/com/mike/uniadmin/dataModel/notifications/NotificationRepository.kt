package com.mike.uniadmin.dataModel.notifications

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.dataModel.userchat.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val viewModelScope = CoroutineScope(Dispatchers.Main)

class NotificationRepository(private val notificationDao: NotificationDao) {

    private val database = FirebaseDatabase.getInstance().reference.child("Notifications")
    private val listeners = mutableMapOf<String, ValueEventListener>() // Store listeners

    fun getNotifications(onComplete: (List<NotificationEntity>) -> Unit) {
        viewModelScope.launch {
            val cachedData = notificationDao.getAllNotifications()
            if (cachedData.isNotEmpty()) {
                onComplete(cachedData)
            } else {
                val listener = database.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val notifications = mutableListOf<NotificationEntity>()
                        for (childSnapshot in snapshot.children) {
                            val notification = childSnapshot.getValue(NotificationEntity::class.java)
                            notification?.let { notifications.add(it) }
                        }
                        viewModelScope.launch {
                            notificationDao.insertNotifications(notifications)
                        }
                        onComplete(notifications)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                    }
                })
                listeners["getNotifications"] = listener // Store listener with a key
            }
        }
    }

    fun writeNotification(notificationEntity: NotificationEntity, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            notificationDao.insertNotification(notificationEntity)
            database.child(notificationEntity.id).setValue(notificationEntity)
                .addOnSuccessListener {
                    onComplete(true)
                }.addOnFailureListener {
                    onComplete(false)
                }
        }
    }

    fun stopListening(key: String) { // Function to stop listening
        val listener = listeners[key]
        listener?.let {
            database.removeEventListener(it)
            listeners.remove(key)
        }
    }
}
