package com.mike.uniadmin.backEnd.courses

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.backEnd.announcements.uniConnectScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class CourseRepository(
    private val courseDao: CourseDao, private val courseStateDao: CourseStateDao
) {
    private val database =
        FirebaseDatabase.getInstance().reference.child("Courses")
    private val courseStateDatabase =
        FirebaseDatabase.getInstance().reference.child("CourseStates")

    init {
        startCourseListener()
        startCourseStateListener()
    }


    private fun startCourseStateListener() {
        courseStateDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val courseStates = mutableListOf<CourseState>()
                for (childSnapshot in snapshot.children) {
                    val courseState = childSnapshot.getValue(CourseState::class.java)
                    courseState?.let { courseStates.add(it) }
                }
                uniConnectScope.launch {
                    courseStateDao.insertCourseStates(courseStates)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error reading course states: ${error.message}")
            }
        })
    }

    fun fetchCourseStates(onResult: (List<CourseState>) -> Unit) {
        uniConnectScope.launch {
            courseStateDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val courseStates = mutableListOf<CourseState>()
                    for (childSnapshot in snapshot.children) {
                        val courseState = childSnapshot.getValue(CourseState::class.java)
                        courseState?.let { courseStates.add(it) }
                    }
                    uniConnectScope.launch {
                        courseStateDao.insertCourseStates(courseStates)
                    }
                    onResult(courseStates)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle the read error (e.g., log the error)
                    println("Error reading courses: ${error.message}")
                    uniConnectScope.launch {
                        val cachedData = courseStateDao.getCourseStates()
                        onResult(cachedData)
                    }
                }
            })
        }
    }

    fun saveCourseState(courseState: CourseState, onComplete: (Boolean) -> Unit) {
        uniConnectScope.launch {
            courseStateDao.insertCourseState(courseState)
            courseStateDatabase.child(courseState.courseID).setValue(courseState)
                .addOnCompleteListener { task ->
                    onComplete(task.isSuccessful)
                }
        }
    }

    fun saveCourse(course: CourseEntity, onComplete: (Boolean) -> Unit) {
        uniConnectScope.launch {
            courseDao.insertCourse(course)
            database.child(course.courseCode).setValue(course).addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
        }
    }


    fun fetchCourses(onResult: (List<CourseEntity>) -> Unit) {
        uniConnectScope.launch {
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
                        uniConnectScope.launch {
                            courseDao.insertCourses(courses)
                        }
                        onResult(courses)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println("Error reading courses: ${error.message}")
                    }
                })
            }
        }
    }


    fun deleteCourse(courseId: String, onSuccess: () -> Unit, onFailure: (Exception?) -> Unit) {
        uniConnectScope.launch {
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
                uniConnectScope.launch {
                    courseDao.insertCourses(courses) // Update local cache
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error reading courses: ${error.message}")
            }
        })

        // Use ChildEventListener for more detailed events, especially handling deletions
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val course = snapshot.getValue(CourseEntity::class.java)
                course?.let {
                    uniConnectScope.launch {
                        courseDao.insertCourse(it)
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val course = snapshot.getValue(CourseEntity::class.java)
                course?.let {
                    uniConnectScope.launch {
                        courseDao.insertCourse(it)
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val course = snapshot.getValue(CourseEntity::class.java)
                course?.let {
                    uniConnectScope.launch {
                        courseDao.deleteCourse(it.courseCode)
                    }
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle if needed
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error handling child events: ${error.message}")
            }
        })
    }

    fun getCourseDetailsByCourseID(courseCode: String, onResult: (CourseEntity?) -> Unit) {
        val courseDetailsRef = database.child(courseCode)
        uniConnectScope.launch {
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

