package com.mike.uniadmin.model.courses

import android.util.Log
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
    private val enrollmentDao: EnrollmentDao,
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


    // Enroll user into a course (Room and Firebase)
    suspend fun enrollUser(enrollment: Enrollment) {
        enrollmentDao.insertEnrollment(enrollment)
        firebaseDatabase.child("Enrollments").child(enrollment.userId).setValue(enrollment)
    }

    // Retrieve all courses from Room
    suspend fun getAllCourses(): List<Course> {
        return courseDao.getAllCourses()
    }

    // Retrieve all enrollments from Room
    suspend fun getAllEnrollments(): List<Enrollment> {
        return enrollmentDao.getAllEnrollments()
    }

    // Retrieve user's enrolled course (JOIN between courses and enrollments)
    suspend fun getUserEnrolledCourse(userId: String): CourseWithEnrollment? {
        return enrollmentDao.getUserEnrolledCourse(userId)
    }

    // Restore enrollments from Firebase to Room
    suspend fun restoreEnrollmentsFromFirebase() {
        val enrollmentsData = firebaseDatabase.child("Enrollments").get().await()
        for (snapshot in enrollmentsData.children) {
            val enrollment = snapshot.getValue(Enrollment::class.java)
            enrollment?.let { enrollmentDao.insertEnrollment(it) }
        }
    }

    // Initialize Firebase listeners to update Room database
    private fun initializeFirebaseListeners() {
        // Listener for courses
        firebaseDatabase.child("Courses").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val courseList = mutableListOf<Course>()
                for (courseSnapshot in snapshot.children) {
                    val course = courseSnapshot.getValue(Course::class.java)
                    course?.let { courseList.add(it) }
                }
                // Insert or update all courses in Room
                courseList.forEach { course ->
                    viewModelScope.launch {
                        courseDao.insertCourse(course)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseListener", "Failed to listen to courses: ${error.message}")
            }
        })

        // Listener for enrollments
        firebaseDatabase.child("Enrollments").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val enrollmentList = mutableListOf<Enrollment>()
                for (enrollmentSnapshot in snapshot.children) {
                    val enrollment = enrollmentSnapshot.getValue(Enrollment::class.java)
                    enrollment?.let { enrollmentList.add(it) }
                }
                // Insert or update all enrollments in Room
                enrollmentList.forEach { enrollment ->
                    viewModelScope.launch {
                        enrollmentDao.insertEnrollment(enrollment)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseListener", "Failed to listen to enrollments: ${error.message}")
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

