package com.mike.uniadmin.dataModel.coursecontent.coursetimetable

import android.util.Log
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CourseTimetableRepository(private val courseTimetableDao: CourseTimetableDao) {
    private val database = FirebaseDatabase.getInstance().reference.child("CourseContent")

    // Scope for running coroutines
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    init {
        // Start listening for changes in Firebase
        listenForFirebaseUpdates()
    }

    // Write a course timetable to both local and remote databases
    fun writeCourseTimetable(
        courseID: String,
        courseTimetable: CourseTimetable,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Insert into local database
                courseTimetableDao.insertCourseTimetable(courseTimetable)
                // Insert into Firebase
                val courseTimetableRef = database.child(courseID).child("Course Timetable")
                courseTimetableRef.child(courseTimetable.timetableID).setValue(courseTimetable)
                    .addOnSuccessListener { onResult(true) } // Indicate success
                    .addOnFailureListener { exception ->
                        println("Error writing timetable: ${exception.message}")
                        onResult(false) // Indicate failure
                    }
            } catch (e: Exception) {
                println("Error writing timetable: ${e.message}")
                onResult(false) // Indicate failure
            }
        }
    }

    fun getCourseTimetables(courseID: String, onResult: (List<CourseTimetable>) -> Unit) {
        viewModelScope.launch {
            val cachedData = courseTimetableDao.getCourseTimetables(courseID)
            if (cachedData.isNotEmpty()) {
                onResult(cachedData)
            }

            val courseTimetableRef = database.child(courseID).child("Course Timetable")

            courseTimetableRef.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val timetable = snapshot.getValue(CourseTimetable::class.java)
                    timetable?.let {
                        viewModelScope.launch {
                            courseTimetableDao.insertCourseTimetable(it)
                            val updatedData = courseTimetableDao.getCourseTimetables(courseID)
                            onResult(updatedData)
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val timetable = snapshot.getValue(CourseTimetable::class.java)
                    timetable?.let {
                        viewModelScope.launch {
                            courseTimetableDao.insertCourseTimetable(it)
                            val updatedData = courseTimetableDao.getCourseTimetables(courseID)
                            onResult(updatedData)
                        }
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val timetable = snapshot.getValue(CourseTimetable::class.java)
                    timetable?.let {
                        viewModelScope.launch {
                            courseTimetableDao.deleteCourseTimetable(it.timetableID)
                            val updatedData = courseTimetableDao.getCourseTimetables(courseID)
                            onResult(updatedData)
                        }
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    // Handle if necessary
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error reading timetables: ${error.message}")
                    onResult(emptyList())
                }
            })
        }
    }

    //get all course timetables
    fun getAllCourseTimetables(onResult: (List<CourseTimetable>) -> Unit) {
        viewModelScope.launch {
            val cachedData = courseTimetableDao.getAllCourseTimetables()
            if (cachedData.isNotEmpty()) {
                onResult(cachedData)
            }else{
                Log.e("Empty", "Empty database")
            }
        }
    }


    // Listen for changes in Firebase and update local database accordingly
    private fun listenForFirebaseUpdates() {
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                updateLocalDatabase(snapshot)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                updateLocalDatabase(snapshot)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val timetable = snapshot.getValue(CourseTimetable::class.java)
                timetable?.let {
                    viewModelScope.launch {
                        courseTimetableDao.deleteCourseTimetable(it.timetableID)
                    }
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                println("Error listening for updates: ${error.message}")
            }

            private fun updateLocalDatabase(snapshot: DataSnapshot) {
                val timetables = mutableListOf<CourseTimetable>()
                for (childSnapshot in snapshot.children) {
                    val timetable = childSnapshot.getValue(CourseTimetable::class.java)
                    timetable?.let { timetables.add(it) }
                }
                viewModelScope.launch {
                    courseTimetableDao.insertCourseTimetables(timetables)
                }
            }
        })
    }

    fun getTimetableByDay(day: String, onResult: (List<CourseTimetable>?) -> Unit) {
        viewModelScope.launch {
            val cachedData = courseTimetableDao.getCourseTimetablesByDay(day)
            if (cachedData.isNotEmpty()) {
                onResult(cachedData)
            } else {
                Log.e("Empty", "Empty database")
            }
        }
    }
}
