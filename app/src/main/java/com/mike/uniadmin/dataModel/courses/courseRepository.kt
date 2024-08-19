package com.mike.uniadmin.dataModel.courses

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.dataModel.programs.ProgramDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

val viewModelScope = CoroutineScope(Dispatchers.Main)

class CourseRepository(
    private val courseDao: CourseDao,
    private val attendanceStateDao: AttendanceStateDao,
    private val programDao: ProgramDao
) {
    private val database = FirebaseDatabase.getInstance().reference.child("Courses")
    private val attendanceStateDatabase = FirebaseDatabase.getInstance().reference.child("AttendanceStates")

    init {
        startCourseListener()
        startAttendanceStateListener()
    }

    private fun startAttendanceStateListener() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val attendanceStates = mutableListOf<AttendanceState>()
                for (childSnapshot in snapshot.children) {
                    val attendanceState = childSnapshot.getValue(AttendanceState::class.java)
                    attendanceState?.let { attendanceStates.add(it) }
                }
                viewModelScope.launch {
                    attendanceStateDao.insertAttendanceStates(attendanceStates)

                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the read error (e.g., log the error)
                println("Error reading courses: ${error.message}")
            }
        })
    }

    fun fetchAttendanceStates(onResult: (List<AttendanceState>) -> Unit) {
        viewModelScope.launch {
            attendanceStateDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val attendanceStates = mutableListOf<AttendanceState>()
                    for (childSnapshot in snapshot.children) {
                        val attendanceState = childSnapshot.getValue(AttendanceState::class.java)
                        attendanceState?.let { attendanceStates.add(it) }
                    }
                    viewModelScope.launch {
                        attendanceStateDao.insertAttendanceStates(attendanceStates)
                    }
                    onResult(attendanceStates)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle the read error (e.g., log the error)
                    println("Error reading courses: ${error.message}")
                    viewModelScope.launch {
                        val cachedData = attendanceStateDao.getAttendanceStates()
                        onResult(cachedData)
                    }
                }
            })
        }
    }

    fun saveAttendanceState(attendanceState: AttendanceState, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            attendanceStateDao.insertAttendanceState(attendanceState)
            attendanceStateDatabase.child(attendanceState.courseID).setValue(attendanceState).addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
        }
    }

    fun saveCourse(course: CourseEntity, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            courseDao.insertCourse(course)
            database.child(course.courseCode).setValue(course).addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
        }
    }


    fun fetchCourses(onResult: (List<CourseEntity>) -> Unit) {
        viewModelScope.launch {
            val cachedData = courseDao.getCourses()
            if (cachedData.isNotEmpty()) {
                onResult(cachedData)
            } else {
                database.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val courses = mutableListOf<CourseEntity>()
                        for (childSnapshot in snapshot.children) {
                            val course = childSnapshot.getValue(CourseEntity::class.java)
                            course?.let { courses.add(it) }
                        }
                        viewModelScope.launch {
                            courseDao.insertCourses(courses)
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

    private fun startCourseListener() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val courses = mutableListOf<CourseEntity>()
                for (childSnapshot in snapshot.children) {
                    val course = childSnapshot.getValue(CourseEntity::class.java)
                    course?.let { courses.add(it) }
                }
                viewModelScope.launch {
                    courseDao.insertCourses(courses)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the read error (e.g., log the error)
                println("Error reading courses: ${error.message}")
            }
        })
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
