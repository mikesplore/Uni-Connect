package com.mike.uniadmin.dataModel.coursecontent.courseannouncements

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.model.MyDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val viewModelScope = CoroutineScope(Dispatchers.Main)

class CourseAnnouncementRepository(private val courseAnnouncementDao: CourseAnnouncementDao) {
    private val database = FirebaseDatabase.getInstance().reference.child("CourseContent")

    //Course Content database
    fun writeCourseAnnouncement(
        courseID: String,
        courseAnnouncement: CourseAnnouncement,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            courseAnnouncementDao.insertCourseAnnouncement(courseAnnouncement)
            val courseAnnouncementRef = MyDatabase.database.child(courseID)
                .child("courseAnnouncements")

            courseAnnouncementRef.child(courseAnnouncement.announcementID)
                .setValue(courseAnnouncement).addOnSuccessListener {
                    onResult(true) // Indicate success
                }.addOnFailureListener { exception ->
                    println("Error writing announcement: ${exception.message}")
                    onResult(false) // Indicate failure
                }
        }
    }


    fun getCourseAnnouncements(courseID: String, onResult: (List<CourseAnnouncement>) -> Unit) {
        viewModelScope.launch {
            val cachedData = courseAnnouncementDao.getCourseAnnouncements(courseID)
            if (cachedData.isNotEmpty()) {
                onResult(cachedData)
            } else {
                val courseAnnouncementRef =
                    database.child(courseID).child("courseAnnouncements")

                courseAnnouncementRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val announcements = mutableListOf<CourseAnnouncement>()
                        for (childSnapshot in snapshot.children) {
                            val announcement =
                                childSnapshot.getValue(CourseAnnouncement::class.java)
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
    }

}