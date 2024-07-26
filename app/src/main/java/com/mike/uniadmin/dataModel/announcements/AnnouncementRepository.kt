package com.mike.uniadmin.dataModel.announcements

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
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val announcements = mutableListOf<AnnouncementEntity>()
                for (childSnapshot in snapshot.children) {
                    val announcement = childSnapshot.getValue(AnnouncementEntity::class.java)
                    announcement?.let { announcements.add(it) }
                }

                viewModelScope.launch {
                    announcementsDao.insertAnnouncements(announcements)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the read error (e.g., log the error)
                println("Error reading users: ${error.message}")
            }
        })
    }

    fun fetchAnnouncements(onResult: (List<AnnouncementEntity>) -> Unit) {
        viewModelScope.launch {
            val cachedData = announcementsDao.getAnnouncements()
            if (cachedData.isNotEmpty()) {
                onResult(cachedData)
            } else {
                // Fetch from remote database if local database is empty
                database.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val announcements = mutableListOf<AnnouncementEntity>()
                        for (childSnapshot in snapshot.children) {
                            val announcement =
                                childSnapshot.getValue(AnnouncementEntity::class.java)
                            announcement?.let { announcements.add(it) }
                        }

                        viewModelScope.launch {
                            announcementsDao.insertAnnouncements(announcements)
                            onResult(announcements)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle the read error (e.g., log the error)
                        println("Error reading users: ${error.message}")
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
