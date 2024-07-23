package com.mike.uniadmin.dataModel.courses

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CourseRepository {
    private val database = FirebaseDatabase.getInstance().reference.child("Courses")

    fun fetchCourses(onResult: (List<Course>) -> Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val courses = mutableListOf<Course>()
                for (childSnapshot in snapshot.children) {
                    val course = childSnapshot.getValue(Course::class.java)
                    course?.let { courses.add(it) }
                }
                onResult(courses)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the read error (e.g., log the error)
                println("Error reading courses: ${error.message}")
            }
        })
    }

    fun saveCourse(course: Course, onComplete: (Boolean) -> Unit) {
        database.child(course.courseCode).setValue(course).addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }

    fun deleteCourse(courseId: String, onSuccess: () -> Unit, onFailure: (Exception?) -> Unit) {
        database.child(courseId).removeValue() // Use the consistent database reference
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun getCourseDetailsByCourseID(courseCode: String, onResult: (Course?) -> Unit) {
        val courseDetailsRef = database.child(courseCode)

        courseDetailsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val courseInfo = snapshot.getValue(Course::class.java)
                onResult(courseInfo)
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error fetching course details: ${error.message}")
                onResult(null) // Indicate failure by returning null
            }
        })
    }
}