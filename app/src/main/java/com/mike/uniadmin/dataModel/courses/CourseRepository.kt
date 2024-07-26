package com.mike.uniadmin.dataModel.courses

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val viewModelScope = CoroutineScope(Dispatchers.Main)

class CourseRepository(private val courseDao: CourseDao) {
    private val database = FirebaseDatabase.getInstance().reference.child("Courses")


    fun fetchCourses(onResult: (List<CourseEntity>) -> Unit) {
        viewModelScope.launch {
            val cachedData = courseDao.getCourses()
            if (cachedData.isNotEmpty()) {
                onResult(cachedData)
            } else {
                database.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val courses = mutableListOf<CourseEntity>()
                        for (childSnapshot in snapshot.children) {
                            val course = childSnapshot.getValue(CourseEntity::class.java)
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
        }
    }

    fun saveCourse(course: CourseEntity, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            courseDao.insertCourse(listOf(course))
            database.child(course.courseCode).setValue(course).addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
        }
    }

    fun deleteCourse(courseId: String, onSuccess: () -> Unit, onFailure: (Exception?) -> Unit) {
        viewModelScope.launch {
            courseDao.deleteCourse(courseId)
            database.child(courseId).removeValue() // Use the consistent database reference
                .addOnSuccessListener {
                    onSuccess()
                }.addOnFailureListener { exception ->
                    onFailure(exception)
                }
        }
    }

    fun getCourseDetailsByCourseID(courseCode: String, onResult: (CourseEntity?) -> Unit) {
        val courseDetailsRef = database.child(courseCode)
        viewModelScope.launch {
            courseDao.getCourse(courseCode)
            courseDetailsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val courseInfo = snapshot.getValue(CourseEntity::class.java)
                    onResult(courseInfo)
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error fetching course details: ${error.message}")
                    onResult(null) // Indicate failure by returning null
                }
            })
        }
    }
}