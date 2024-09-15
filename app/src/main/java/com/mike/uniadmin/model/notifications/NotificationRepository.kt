package com.mike.uniadmin.model.notifications

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.model.announcements.uniConnectScope
import kotlinx.coroutines.launch


class NotificationRepository(private val notificationDao: NotificationDao) {
    private val database = FirebaseDatabase.getInstance().reference.child("Notifications")
    private val valueListeners = mutableMapOf<String, ValueEventListener>()
    private val childListeners = mutableMapOf<String, ChildEventListener>()

    init {
        // Add a listener to keep the local database updated
        addRealtimeListener()
    }

    fun getNotifications(onComplete: (List<NotificationEntity>) -> Unit) {
        uniConnectScope.launch {
            val cachedData = notificationDao.getAllNotifications()
            if (cachedData.isNotEmpty()) {
                onComplete(cachedData)
            } else {
                // Use ValueEventListener for initial data load
                val listener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val notifications = mutableListOf<NotificationEntity>()
                        for (childSnapshot in snapshot.children) {
                            val notification = childSnapshot.getValue(NotificationEntity::class.java)
                            notification?.let { notifications.add(it) }
                        }
                        uniConnectScope.launch {
                            notificationDao.insertNotifications(notifications)
                            onComplete(notifications)
                        }
                        stopListening("initialLoad") // Stop the listener after initial load
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                    }
                }
                database.addListenerForSingleValueEvent(listener)
                valueListeners["initialLoad"] = listener // Store listener with a key
            }
        }
    }

    private fun addRealtimeListener() {
        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val notification = snapshot.getValue(NotificationEntity::class.java)
                notification?.let {
                    uniConnectScope.launch {
                        notificationDao.insertNotification(it)
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val notification = snapshot.getValue(NotificationEntity::class.java)
                notification?.let {
                    uniConnectScope.launch {
                        notificationDao.insertNotification(it)
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val notificationId = snapshot.key
                uniConnectScope.launch {
                    notificationId?.let { notificationDao.deleteNotification(it) }
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle moves if needed
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        }
        database.addChildEventListener(childEventListener)
        childListeners["realtimeUpdates"] = childEventListener // Store listener with a key
    }

    fun writeNotification(notificationEntity: NotificationEntity, onComplete: (Boolean) -> Unit) {
        uniConnectScope.launch {
            notificationDao.insertNotification(notificationEntity)
            database.child(notificationEntity.id).setValue(notificationEntity)
                .addOnSuccessListener {
                    onComplete(true)
                }.addOnFailureListener {
                    onComplete(false)
                }
        }
    }

    fun stopListening(key: String) {
        valueListeners[key]?.let {
            database.removeEventListener(it)
            valueListeners.remove(key)
        }
        childListeners[key]?.let {
            database.removeEventListener(it)
            childListeners.remove(key)
        }
    }
}
