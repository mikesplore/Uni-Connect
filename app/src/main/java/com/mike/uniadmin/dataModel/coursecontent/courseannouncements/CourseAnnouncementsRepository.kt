package com.mike.uniadmin.dataModel.coursecontent.courseannouncements

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.model.MyDatabase

class CourseAnnouncementRepository {
    private val database = FirebaseDatabase.getInstance().reference

    //Course Content database
    fun writeCourseAnnouncement(courseID: String, courseAnnouncement: CourseAnnouncement, onResult: (Boolean) -> Unit) {
        val courseAnnouncementRef = MyDatabase.database.child("CourseContent")
            .child(courseID)
            .child("courseAnnouncements")

        courseAnnouncementRef.child(courseAnnouncement.announcementID).setValue(courseAnnouncement)
            .addOnSuccessListener {
                onResult(true) // Indicate success
            }
            .addOnFailureListener { exception ->
                println("Error writing announcement: ${exception.message}")
                onResult(false) // Indicate failure
            }
    }


    fun getCourseAnnouncements(courseID: String, onResult: (List<CourseAnnouncement>) -> Unit) {
        val courseAnnouncementRef = database.child("CourseContent")
            .child(courseID)
            .child("courseAnnouncements")

        courseAnnouncementRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val announcements = mutableListOf<CourseAnnouncement>()
                for (childSnapshot in snapshot.children) {
                    val announcement = childSnapshot.getValue(CourseAnnouncement::class.java)
                    announcement?.let { announcements.add(it) }
                }
                onResult(announcements)
            }

            override fun onCancelled(error: DatabaseError) {

                println("Error reading announcements: ${error.message}")
            }
        })
    }

}