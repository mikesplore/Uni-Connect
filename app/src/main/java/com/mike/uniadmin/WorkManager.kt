package com.mike.uniadmin

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.mike.uniadmin.dataModel.groupchat.generateConversationId
import com.mike.uniadmin.dataModel.userchat.MessageRepository
import com.mike.uniadmin.dataModel.users.UserDao
import com.mike.uniadmin.notification.showNotification
import java.util.concurrent.TimeUnit

// Iterate through the list of user IDs and generate the conversation IDs
suspend fun generatePathsForFetchingMessages(userDao: UserDao, myUserId: String): List<String> {
    Log.d("UniAdminWorkManager", "Generating paths for userId: $myUserId")
    val userIds = userDao.getAllUserIds() // Retrieve all user IDs from the database
    Log.d("UniAdminWorkManager", "Fetched user IDs from database: $userIds")

    return userIds.map { otherUserId ->
        val path = "Direct Messages/${generateConversationId(myUserId, otherUserId)}"
        Log.d("UniAdminWorkManager", "Generated path: $path")
        path
    }
}

suspend fun scheduleFetchMessagesWork(userDao: UserDao, myUserId: String, context: Context) {
    Log.d("UniAdminWorkManager", "Scheduling fetch messages work for userId: $myUserId")

    val paths = generatePathsForFetchingMessages(userDao, myUserId)

    paths.forEach { path ->
        val inputData = workDataOf("path" to path)
        Log.d("UniAdminWorkManager", "Scheduling work with path: $path")

        val fetchMessagesWorkRequest = PeriodicWorkRequestBuilder<FetchMessagesWorker>(15, TimeUnit.MINUTES)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "FetchMessagesWork_$path", // Ensure uniqueness by using the path in the name
            ExistingPeriodicWorkPolicy.UPDATE,

            fetchMessagesWorkRequest
        )
    }
}

class FetchMessagesWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val messageRepository: MessageRepository
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        Log.d("UniAdminWorkManager", "FetchMessagesWorker started.") // Existing log

        // New log statement at the very beginning
        Log.d("UniAdminWorkManager", "doWork() entered")

        val path = inputData.getString("path")
        if (path == null) {
            Log.e("UniAdminWorkManager", "Path is null. Cannot proceed.")
            return Result.failure()
        }

        Log.d("UniAdminWorkManager", "Fetching messages from path: $path")

        messageRepository.fetchMessages(path) { messages ->
            Log.d("UniAdminWorkManager", "Fetched ${messages.size} messages from path: $path")

            if (messages.isNotEmpty()) {
                val newMessage = messages.last()
                Log.d("UniAdminWorkManager", "New message received: $newMessage")

                val title = "New Message from ${newMessage.senderID}" // Customize the title
                val description = newMessage.message // Customize the description

                // Show notification
                showNotification(applicationContext, title, description)
                Log.d("UniAdminWorkManager", "Notification shown: Title = $title, Description = $description")
            } else {
                Log.d("UniAdminWorkManager", "No new messages found.")
            }
        }

        return Result.success()
    }
}

