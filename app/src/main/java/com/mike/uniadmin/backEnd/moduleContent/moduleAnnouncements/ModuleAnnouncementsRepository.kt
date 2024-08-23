package com.mike.uniadmin.backEnd.moduleContent.moduleAnnouncements

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.UniAdminPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val viewModelScope = CoroutineScope(Dispatchers.Main)

class ModuleAnnouncementRepository(private val moduleAnnouncementDao: ModuleAnnouncementDao) {
    private val courseCode = UniAdminPreferences.courseCode.value
    private val database =
        FirebaseDatabase.getInstance().reference.child(courseCode).child("ModuleContent")

    // Write a new module announcement to both Firebase and the local database
    fun writeModuleAnnouncement(
        moduleID: String,
        moduleAnnouncement: ModuleAnnouncement,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
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
    fun getModuleAnnouncements(moduleID: String, onResult: (List<ModuleAnnouncement>) -> Unit) {
        viewModelScope.launch {
            // Step 1: Load Local Data
            val localAnnouncements = moduleAnnouncementDao.getModuleAnnouncements(moduleID)
            if (localAnnouncements.isNotEmpty()) {
                onResult(localAnnouncements) // Return local data immediately
            }

            // Step 2: Set Up Firebase Listener to Sync Data
            val moduleAnnouncementRef = database.child(moduleID).child("Module Announcements")
            moduleAnnouncementRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val announcements = mutableListOf<ModuleAnnouncement>()
                    for (childSnapshot in snapshot.children) {
                        val announcement = childSnapshot.getValue(ModuleAnnouncement::class.java)
                        announcement?.let { announcements.add(it) }
                    }

                    // Step 3: Update Local Database with Firebase Data
                    viewModelScope.launch {
                        moduleAnnouncementDao.clearAnnouncements(moduleID) // Clear old data
                        moduleAnnouncementDao.insertModuleAnnouncements(announcements) // Insert new data
                    }

                    onResult(announcements) // Return the updated list
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error reading announcements: ${error.message}")
                    onResult(emptyList()) // Return an empty list in case of error
                }
            })
        }
    }
}
