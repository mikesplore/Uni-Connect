package com.mike.uniadmin.backEnd.announcements

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.mike.uniadmin.UniAdminPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

val uniConnectScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

class AnnouncementRepository(private val announcementsDao: AnnouncementsDao) {
    private val courseCode = UniAdminPreferences.courseCode.value
    private val database = FirebaseDatabase.getInstance().reference.child(courseCode).child("Announcements")

    init {
        Log.d("AnnouncementRepository", "Initializing and starting announcement listener")
        startAnnouncementsListener()
    }

     fun startAnnouncementsListener() {
        Log.d("AnnouncementRepository", "Starting announcements listener on Firebase path: $courseCode/Announcements")
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val announcement = snapshot.getValue(AnnouncementEntity::class.java)
                Log.d("AnnouncementRepository", "onChildAdded called: ${announcement?.id}")
                announcement?.let {
                    uniConnectScope.launch {
                        announcementsDao.insertAnnouncements(listOf(it))
                        Log.d("AnnouncementRepository", "Inserted announcement: ${it.id} into local database")
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val announcement = snapshot.getValue(AnnouncementEntity::class.java)
                Log.d("AnnouncementRepository", "onChildChanged called: ${announcement?.id}")
                announcement?.let {
                    uniConnectScope.launch {
                        announcementsDao.insertAnnouncement(it) // Ensure you have an update method
                        Log.d("AnnouncementRepository", "Updated announcement: ${it.id} in local database")
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val announcementId = snapshot.key
                Log.d("AnnouncementRepository", "onChildRemoved called: $announcementId")
                announcementId?.let {
                    uniConnectScope.launch {
                        announcementsDao.deleteAnnouncement(it)
                        Log.d("AnnouncementRepository", "Deleted announcement: $it from local database")
                    }
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("AnnouncementRepository", "onChildMoved called but not implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AnnouncementRepository", "Error listening to announcements: ${error.message}")
            }
        })
    }

    fun fetchAnnouncements(onResult: (List<AnnouncementsWithAuthor>) -> Unit) {
        Log.d("AnnouncementRepository", "Fetching announcements from local database")
        uniConnectScope.launch {
            val cachedData = announcementsDao.getAnnouncements()
            Log.d("AnnouncementRepository", "Fetched ${cachedData.size} announcements from local database")
            onResult(cachedData)
        }
    }

    fun saveAnnouncement(announcement: AnnouncementEntity, onComplete: (Boolean) -> Unit) {
        Log.d("AnnouncementRepository", "Saving announcement: ${announcement.id}")
        // Save to local database
        uniConnectScope.launch {
            announcementsDao.insertAnnouncements(listOf(announcement))
            Log.d("AnnouncementRepository", "Saved announcement: ${announcement.id} to local database")
        }

        // Save to Firebase (no coroutine needed)
        database.child(announcement.id).setValue(announcement).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("AnnouncementRepository", "Successfully saved announcement: ${announcement.id} to Firebase")
            } else {
                Log.e("AnnouncementRepository", "Failed to save announcement: ${announcement.id} to Firebase")
            }
            onComplete(task.isSuccessful)
        }
    }

    fun deleteAnnouncement(
        announcementId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception?) -> Unit
    ) {
        Log.d("AnnouncementRepository", "Deleting announcement: $announcementId")
        // Delete from local database
        uniConnectScope.launch {
            announcementsDao.deleteAnnouncement(announcementId)
            Log.d("AnnouncementRepository", "Deleted announcement: $announcementId from local database")
        }

        // Delete from Firebase (no coroutine needed)
        database.child(announcementId).removeValue()
            .addOnSuccessListener {
                Log.d("AnnouncementRepository", "Successfully deleted announcement: $announcementId from Firebase")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e("AnnouncementRepository", "Failed to delete announcement: $announcementId from Firebase", exception)
                onFailure(exception)
            }
    }
}
