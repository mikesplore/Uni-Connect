package com.mike.uniadmin.backEnd.coursecontent.courseannouncements

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.programs.ProgramCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val viewModelScope = CoroutineScope(Dispatchers.Main)

class CourseAnnouncementRepository(private val courseAnnouncementDao: CourseAnnouncementDao) {
    private val programCode = ProgramCode.programCode.value
    private val database =
        FirebaseDatabase.getInstance().reference.child(programCode).child("CourseContent")

    // Write a new course announcement to both Firebase and the local database
    fun writeCourseAnnouncement(
        courseID: String,
        courseAnnouncement: CourseAnnouncement,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            // Save to local database first
            courseAnnouncementDao.insertCourseAnnouncement(courseAnnouncement)

            // Save to Firebase
            database.child(courseID).child("Course Announcements")
                .child(courseAnnouncement.announcementID)
                .setValue(courseAnnouncement)
                .addOnSuccessListener { onResult(true) }
                .addOnFailureListener { exception ->
                    println("Error writing announcement: ${exception.message}")
                    onResult(false)
                }
        }
    }

    // Get course announcements and keep the local database in sync with Firebase
    fun getCourseAnnouncements(courseID: String, onResult: (List<CourseAnnouncement>) -> Unit) {
        viewModelScope.launch {
            // Step 1: Load Local Data
            val localAnnouncements = courseAnnouncementDao.getCourseAnnouncements(courseID)
            if (localAnnouncements.isNotEmpty()) {
                onResult(localAnnouncements) // Return local data immediately
            }

            // Step 2: Set Up Firebase Listener to Sync Data
            val courseAnnouncementRef = database.child(courseID).child("Course Announcements")
            courseAnnouncementRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val announcements = mutableListOf<CourseAnnouncement>()
                    for (childSnapshot in snapshot.children) {
                        val announcement = childSnapshot.getValue(CourseAnnouncement::class.java)
                        announcement?.let { announcements.add(it) }
                    }

                    // Step 3: Update Local Database with Firebase Data
                    viewModelScope.launch {
                        courseAnnouncementDao.clearAnnouncements(courseID) // Clear old data
                        courseAnnouncementDao.insertCourseAnnouncements(announcements) // Insert new data
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
