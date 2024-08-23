package com.mike.uniadmin

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mike.uniadmin.notification.showNotification

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // Handle the received message
        remoteMessage.notification?.let {
            showNotification(
                context = this,
                title = it.title ?: "Notification",
                message = it.body ?: "Message received"
            )
        }
    }

}
