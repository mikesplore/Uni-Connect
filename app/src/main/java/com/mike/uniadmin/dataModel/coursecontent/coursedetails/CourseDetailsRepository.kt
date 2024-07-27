package com.mike.uniadmin.dataModel.coursecontent.coursedetails

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val viewModelScope = CoroutineScope(Dispatchers.Main)

class CourseDetailRepository(private val courseDetailDao: CourseDetailDao) {
    private val database = FirebaseDatabase.getInstance().reference.child("CourseContent")

    init {
        setupRealtimeUpdates()
    }

    private fun setupRealtimeUpdates() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (courseSnapshot in snapshot.children) {
                    val courseID = courseSnapshot.key ?: continue
                    val courseDetailRef = courseSnapshot.child("courseDetails")
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

    fun writeCourseDetail(
        courseID: String,
        courseDetail: CourseDetail,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            courseDetailDao.insertCourseDetail(courseDetail)
            val courseDetailRef = database.child(courseID).child("courseDetails")

            courseDetailRef.child(courseDetail.detailID)
                .setValue(courseDetail).addOnSuccessListener {
                    onResult(true) // Indicate success
                }.addOnFailureListener { exception ->
                    println("Error writing detail: ${exception.message}")
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
                val courseDetailRef = database.child(courseID).child("courseDetails").limitToFirst(1)

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
