package com.mike.uniadmin.dataModel.announcements

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.dataModel.users.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val announcementViewModelScope = CoroutineScope(Dispatchers.Main)

class AnnouncementRepository(private val announcementsDao: AnnouncementsDao) {
    private val database = FirebaseDatabase.getInstance().reference.child("Announcements")

    init {
        startAnnouncementsListener()
    }

    private fun startAnnouncementsListener() {
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val announcement = snapshot.getValue(AnnouncementEntity::class.java)
                announcement?.let {
                    announcementViewModelScope.launch {
                        announcementsDao.insertAnnouncements(listOf(it))
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val announcement = snapshot.getValue(AnnouncementEntity::class.java)
                announcement?.let {
                    announcementViewModelScope.launch {
                        announcementsDao.insertAnnouncement(it) // Make sure you have an update method
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val announcementId = snapshot.key
                announcementId?.let {
                    announcementViewModelScope.launch {
                        announcementsDao.deleteAnnouncement(it)
                    }
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle moves if needed
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error (e.g., log it)
                println("Error listening to announcements: ${error.message}")
            }
        })
    }

    fun fetchAnnouncements(onResult: (List<AnnouncementEntity>) -> Unit) {
        announcementViewModelScope.launch {
            val cachedData = announcementsDao.getAnnouncements()
            if (cachedData.isNotEmpty()) {
                onResult(cachedData)
            } else {
                // Fetch from remote database if local database is empty
                database.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val announcements = mutableListOf<AnnouncementEntity>()
                        for (childSnapshot in snapshot.children) {
                            val announcement = childSnapshot.getValue(AnnouncementEntity::class.java)
                            announcement?.let { announcements.add(it) }
                        }

                        announcementViewModelScope.launch {
                            announcementsDao.insertAnnouncements(announcements)
                            onResult(announcements)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle the error (e.g., log it)
                        println("Error reading announcements: ${error.message}")
                    }
                })
            }
        }
    }

    fun saveAnnouncement(announcement: AnnouncementEntity, onComplete: (Boolean) -> Unit) {
        announcementViewModelScope.launch {
            announcementsDao.insertAnnouncements(listOf(announcement))
            database.child(announcement.id).setValue(announcement).addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
        }
    }

    fun deleteAnnouncement(
        announcementId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception?) -> Unit
    ) {
        announcementViewModelScope.launch {
            announcementsDao.deleteAnnouncement(announcementId)
            database.child(announcementId).removeValue().addOnSuccessListener {
                onSuccess()
            }.addOnFailureListener { exception ->
                onFailure(exception)
            }
        }
    }
}
