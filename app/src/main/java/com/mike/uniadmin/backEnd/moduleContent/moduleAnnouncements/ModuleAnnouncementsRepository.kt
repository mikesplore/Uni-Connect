package com.mike.uniadmin.backEnd.moduleContent.moduleAnnouncements

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.UniAdminPreferences
import com.mike.uniadmin.backEnd.announcements.uniConnectScope
import kotlinx.coroutines.launch

class ModuleAnnouncementRepository(private val moduleAnnouncementDao: ModuleAnnouncementDao) {
    private val courseCode = UniAdminPreferences.courseCode.value
    private val database =
        FirebaseDatabase.getInstance().reference.child(courseCode).child("ModuleContent")

    init {
        listenToFirebaseChanges()

    }
    // Write a new module announcement to both Firebase and the local database
    fun writeModuleAnnouncement(
        moduleID: String,
        moduleAnnouncement: ModuleAnnouncement,
        onResult: (Boolean) -> Unit
    ) {
        uniConnectScope.launch {
            // Save to local database first
            moduleAnnouncementDao.insertModuleAnnouncement(moduleAnnouncement)

            // Save to Firebase
            database.child(moduleID).child("Module Announcements")
                .child(moduleAnnouncement.announcementID)
                .setValue(moduleAnnouncement)
                .addOnSuccessListener { onResult(true) }
                .addOnFailureListener { exception ->
                    println("Error writing announcement: ${exception.message}")
                    onResult(false)
                }
        }
    }

    // Get module announcements and keep the local database in sync with Firebase
    fun getModuleAnnouncements(
        moduleID: String,
        onResult: (List<ModuleAnnouncementsWithAuthor>) -> Unit
    ) {
        uniConnectScope.launch {
            // Step 1: Load Local Data
            val localAnnouncements = moduleAnnouncementDao.getModuleAnnouncements(moduleID)
            if (localAnnouncements.isNotEmpty()) {
                onResult(localAnnouncements) // Return local data immediately
            }

        }
    }

    // Separate function to listen to Firebase changes and update the local Room database
    private fun listenToFirebaseChanges(
    ) {
        val moduleAnnouncementRef = database.child("Module Announcements")
        moduleAnnouncementRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val announcements = mutableListOf<ModuleAnnouncement>()
                for (childSnapshot in snapshot.children) {
                    val announcement = childSnapshot.getValue(ModuleAnnouncement::class.java)
                    announcement?.let { announcements.add(it) }
                }
                updateLocalDatabase(announcements)

            }

            override fun onCancelled(error: DatabaseError) {
                println("Error reading announcements: ${error.message}")
            }
        })
    }

    // Separate function to update the local Room database with announcements from Firebase
    private fun updateLocalDatabase(announcements: List<ModuleAnnouncement>) {
        uniConnectScope.launch {
            moduleAnnouncementDao.insertModuleAnnouncements(announcements)
        }
    }
}
