package com.mike.uniadmin.dataModel.coursecontent.coursetimetable

import android.util.Log
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val timetableViewModelScope = CoroutineScope(Dispatchers.Main)

class CourseTimetableRepository(private val courseTimetableDao: CourseTimetableDao) {
    private val database = FirebaseDatabase.getInstance().reference.child("CourseContent")


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
        timetableViewModelScope.launch {
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
        timetableViewModelScope.launch {
            Log.d("Timetables", "Searching repository timetable for course: $courseID")

            // Fetch cached data from local database
            val cachedData = courseTimetableDao.getCourseTimetables(courseID)

            if (cachedData.isNotEmpty()) {
                // Return cached data if available
                onResult(cachedData)
                Log.d("Timetables", "Found timetable in database")
            } else {
                Log.d("Timetables", "No timetable found in database, fetching from repository")
                val courseTimetableRef = database.child(courseID).child("Course Timetable")

                // Fetch data from the remote database
                courseTimetableRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val timetables = mutableListOf<CourseTimetable>()
                        for (childSnapshot in snapshot.children) {
                            val timetable = childSnapshot.getValue(CourseTimetable::class.java)
                            timetable?.let {
                                Log.d("Timetables", "Found timetable in repository")
                                timetables.add(it)
                            }
                        }
                        timetableViewModelScope.launch {
                            // Update local database with fetched timetables
                            if (timetables.isNotEmpty()) {
                                courseTimetableDao.insertCourseTimetables(timetables)
                                val updatedData = courseTimetableDao.getCourseTimetables(courseID)
                                onResult(updatedData)
                            } else {
                                onResult(emptyList())
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Timetables", "Error reading timetables: ${error.message}")
                        onResult(emptyList())
                    }
                })

                // Listen for real-time updates to the remote database
                courseTimetableRef.addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        val timetable = snapshot.getValue(CourseTimetable::class.java)
                        timetable?.let {
                            timetableViewModelScope.launch {
                                courseTimetableDao.insertCourseTimetable(it)
                                val updatedData = courseTimetableDao.getCourseTimetables(courseID)
                                onResult(updatedData)
                            }
                        }
                    }

                    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                        val timetable = snapshot.getValue(CourseTimetable::class.java)
                        timetable?.let {
                            timetableViewModelScope.launch {
                                courseTimetableDao.insertCourseTimetable(it)
                                val updatedData = courseTimetableDao.getCourseTimetables(courseID)
                                onResult(updatedData)
                            }
                        }
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        val timetable = snapshot.getValue(CourseTimetable::class.java)
                        timetable?.let {
                            timetableViewModelScope.launch {
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
                        Log.e("Timetables", "Error handling child event: ${error.message}")
                        onResult(emptyList())
                    }
                })
            }
        }
    }


    fun getAllCourseTimetables(onResult: (List<CourseTimetable>) -> Unit) {
        timetableViewModelScope.launch(Dispatchers.IO) { // Ensure this runs on a background thread
            val cachedData = courseTimetableDao.getAllCourseTimetables()
            onResult(cachedData)
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
                    timetableViewModelScope.launch {
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
                timetableViewModelScope.launch {
                    courseTimetableDao.insertCourseTimetables(timetables)
                }
            }
        })
    }

    fun getTimetableByDay(day: String, onResult: (List<CourseTimetable>?) -> Unit) {
        timetableViewModelScope.launch {
            val cachedData = courseTimetableDao.getCourseTimetablesByDay(day)
            if (cachedData.isNotEmpty()) {
                onResult(cachedData)
            } else {
                Log.e("Empty", "Empty database")
            }
        }
    }
}
