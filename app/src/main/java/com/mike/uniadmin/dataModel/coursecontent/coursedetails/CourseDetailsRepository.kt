package com.mike.uniadmin.dataModel.coursecontent.coursedetails

import com.google.firebase.database.*
import com.mike.uniadmin.dataModel.courses.CourseEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CourseDetailRepository(private val courseDetailDao: CourseDetailDao) {
    private val database = FirebaseDatabase.getInstance().reference.child("CourseContent")

    // Scope for running coroutines
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    init {
        // Start listening for changes in Firebase
        setupRealtimeUpdates()
    }

    private fun setupRealtimeUpdates() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (courseSnapshot in snapshot.children) {
                    val courseID = courseSnapshot.key ?: continue
                    val courseDetailRef = courseSnapshot.child("Course Details")
                    val details = courseDetailRef.children.mapNotNull { it.getValue(CourseDetail::class.java) }
                    viewModelScope.launch {
                        details.forEach { courseDetailDao.insertCourseDetail(it) }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error setting up real-time updates: ${error.message}")
            }
        })
    }

    fun getCourseDetailsByCourseID(courseCode: String, onResult: (CourseEntity?) -> Unit) {
        val courseDetailsRef = FirebaseDatabase.getInstance().reference.child("Courses").child(courseCode)
        viewModelScope.launch {
            val cachedData = courseDetailDao.getCourseDetailsByID(courseCode)
            if (cachedData != null) {
                onResult(cachedData)
            } else {
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

    fun writeCourseDetail(courseID: String, courseDetail: CourseDetail, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // Insert into local database
                courseDetailDao.insertCourseDetail(courseDetail)
                // Insert into Firebase
                val courseDetailRef = database.child(courseID).child("Course Details")
                courseDetailRef.child(courseDetail.detailID).setValue(courseDetail)
                    .addOnSuccessListener { onResult(true) } // Indicate success
                    .addOnFailureListener { exception ->
                        println("Error writing detail: ${exception.message}")
                        onResult(false) // Indicate failure
                    }
            } catch (e: Exception) {
                println("Error writing detail: ${e.message}")
                onResult(false) // Indicate failure
            }
        }
    }

    fun getCourseDetails(courseID: String, onResult: (CourseDetail?) -> Unit) {
        viewModelScope.launch {
            val cachedData = courseDetailDao.getCourseDetail(courseID)
            if (cachedData != null) {
                onResult(cachedData)
            } else {
                val courseDetailRef = database.child(courseID).child("Course Details").limitToFirst(1)
                courseDetailRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.hasChildren()) {
                            val detail = snapshot.children.first().getValue(CourseDetail::class.java)
                            detail?.let {
                                viewModelScope.launch {
                                    courseDetailDao.insertCourseDetail(it)
                                }
                                onResult(it)
                            }
                        } else {
                            onResult(null) // No course detail found
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println("Error reading details: ${error.message}")
                        onResult(null)
                    }
                })
            }
        }
    }
}
