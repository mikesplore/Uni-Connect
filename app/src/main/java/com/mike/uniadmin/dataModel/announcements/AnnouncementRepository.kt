package com.mike.uniadmin.dataModel.announcements


import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AnnouncementRepository {
    private val database = FirebaseDatabase.getInstance().reference.child("Announcements")

    fun fetchAnnouncements(onResult: (List<Announcement>) -> Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val announcements = mutableListOf<Announcement>()
                for (childSnapshot in snapshot.children) {
                    val announcement = childSnapshot.getValue(Announcement::class.java)
                    announcement?.let { announcements.add(it) }
                }
                onResult(announcements)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the read error (e.g., log the error)
                println("Error reading announcements: ${error.message}")
            }
        })
    }

    fun saveAnnouncement(announcement: Announcement, onComplete: (Boolean) -> Unit) {
        database.child(announcement.id).setValue(announcement).addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }

    fun deleteAnnouncement(announcementId: String, onSuccess: () -> Unit, onFailure: (Exception?) -> Unit) {
        database.child(announcementId).removeValue() // Use the consistent database reference
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}