package com.mike.uniadmin.model.courses

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CourseRepository(
    private val courseDao: CourseDao,
    private val academicYearDao: AcademicYearDao
) {
    private val firebaseDatabase = FirebaseDatabase.getInstance().reference
    private val viewModelScope = CoroutineScope(Dispatchers.IO)

    init {
        // Initialize Firebase listeners
        initializeFirebaseListeners()
        initFirebaseListeners()
        viewModelScope.launch {
            restoreAcademicYearsFromFirebase()
        }
    }

    // Insert course into both Room and Firebase
    suspend fun insertCourse(course: Course) {
        courseDao.insertCourse(course)
        firebaseDatabase.child("Courses").child(course.courseCode).setValue(course)
    }


    // Retrieve all courses from Room
    suspend fun getAllCourses(): List<Course> {
        return courseDao.getAllCourses()
    }

    // Initialize Firebase listeners to update Room database, including deletions
    private fun initializeFirebaseListeners() {
        // Listener for courses
        firebaseDatabase.child("Courses").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val course = snapshot.getValue(Course::class.java)
                course?.let {
                    viewModelScope.launch {
                        courseDao.insertCourse(it)
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val course = snapshot.getValue(Course::class.java)
                course?.let {
                    viewModelScope.launch {
                        courseDao.insertCourse(it)
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val course = snapshot.getValue(Course::class.java)
                course?.let {
                    viewModelScope.launch {
                        courseDao.deleteCourse(it.courseCode)
                    }
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Not needed for most cases
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseListener", "Failed to listen to courses: ${error.message}")
            }
        })

    }


    // Insert academic year into both Room and Firebase
    suspend fun addAcademicYear(academicYear: AcademicYear) {
        academicYearDao.insertAcademicYear(academicYear)
        firebaseDatabase.child("AcademicYears").child(academicYear.year).setValue(academicYear)
    }


    // Retrieve all academic years from Room
    suspend fun getAllAcademicYears(): List<AcademicYear> {
        return academicYearDao.getAllAcademicYears()
    }


    // Restore academic years from Firebase to Room
    private suspend fun restoreAcademicYearsFromFirebase() {
        val academicYearsData = firebaseDatabase.child("AcademicYears").get().await()
        for (snapshot in academicYearsData.children) {
            val academicYear = snapshot.getValue(AcademicYear::class.java)
            academicYear?.let { academicYearDao.insertAcademicYear(it) }
        }
    }


    // Initialize Firebase listeners
    private fun initFirebaseListeners() {
        firebaseDatabase.child("AcademicYears").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    val academicYear = childSnapshot.getValue(AcademicYear::class.java)
                    academicYear?.let {
                        viewModelScope.launch {
                            academicYearDao.insertAcademicYear(it)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

    }
}

