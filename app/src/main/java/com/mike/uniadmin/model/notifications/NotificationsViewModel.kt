package com.mike.uniadmin.model.notifications

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class NotificationViewModel(private val repository: NotificationRepository): ViewModel() {
    private val _notifications: MutableLiveData<List<NotificationEntity>> = MutableLiveData()
    val notifications: LiveData<List<NotificationEntity>> = _notifications

    init {
        fetchNotifications()
    }

     fun fetchNotifications() {
        repository.getNotifications { notifications ->
            _notifications.postValue(notifications)
        }
    }

    fun writeNotification(notificationEntity: NotificationEntity) {
        repository.writeNotification(notificationEntity, onComplete = {
            if (it) {
            Log.d("Success", "Notification written successfully")}
            else{
                Log.d("Failure", "Notification failed to write")
            }
        })
    }

    class NotificationViewModelFactory(private val repository: NotificationRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
                return NotificationViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class for Notifications")
        }
    }
}