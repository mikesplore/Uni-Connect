package com.mike.uniadmin.dataModel.coursecontent.coursedetails

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.model.MyDatabase

class CourseDetailRepository {
    private val database = FirebaseDatabase.getInstance().reference

    //Course Content database
    fun writeCourseDetail(courseID: String, courseDetail: CourseDetails, onResult: (Boolean) -> Unit) {
        val courseDetailRef = MyDatabase.database.child("CourseContent")
            .child(courseID)
            .child("courseDetails")

        courseDetailRef.child(courseDetail.detailsID).setValue(courseDetail)
            .addOnSuccessListener {
                onResult(true) // Indicate success
            }
            .addOnFailureListener { exception ->
                println("Error writing detail: ${exception.message}")
                onResult(false) // Indicate failure
            }
    }


    fun getCourseDetails(courseID: String, onResult: (List<CourseDetails>) -> Unit) {
        val courseDetailRef = database.child("CourseContent")
            .child(courseID)
            .child("courseDetails")

        courseDetailRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val details = mutableListOf<CourseDetails>()
                for (childSnapshot in snapshot.children) {
                    val detail = childSnapshot.getValue(CourseDetails::class.java)
                    detail?.let { details.add(it) }
                }
                onResult(details)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the read error (e.g., log the error)
                println("Error reading details: ${error.message}")
            }
        })
    }

}