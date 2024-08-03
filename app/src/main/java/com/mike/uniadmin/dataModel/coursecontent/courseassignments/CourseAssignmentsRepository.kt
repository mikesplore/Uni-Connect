package com.mike.uniadmin.dataModel.coursecontent.courseassignments

import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CourseAssignmentRepository(private val courseAssignmentDao: CourseAssignmentDao) {
    private val database = FirebaseDatabase.getInstance().reference.child("CourseContent")

    // Scope for running coroutines
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    init {
        // Start listening for changes in Firebase
        listenForFirebaseUpdates()
    }

    // Write a course assignment to both local and remote databases
    fun writeCourseAssignment(
        courseID: String,
        courseAssignment: CourseAssignment,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Insert into local database
                courseAssignmentDao.insertCourseAssignment(courseAssignment)
                // Insert into Firebase
                val courseAssignmentRef = database.child(courseID).child("Course Assignments")
                courseAssignmentRef.child(courseAssignment.assignmentID).setValue(courseAssignment)
                    .addOnSuccessListener { onResult(true) } // Indicate success
                    .addOnFailureListener { exception ->
                        println("Error writing assignment: ${exception.message}")
                        onResult(false) // Indicate failure
                    }
            } catch (e: Exception) {
                println("Error writing assignment: ${e.message}")
                onResult(false) // Indicate failure
            }
        }
    }

    // Fetch assignments from local database or Firebase if not present locally
    fun getCourseAssignments(courseID: String, onResult: (List<CourseAssignment>) -> Unit) {
        viewModelScope.launch {
            val cachedData = courseAssignmentDao.getCourseAssignments(courseID)
            if (cachedData.isNotEmpty()) {
                onResult(cachedData)
            } else {
                val courseAssignmentRef = database.child(courseID).child("Course Assignments")
                courseAssignmentRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val assignments = mutableListOf<CourseAssignment>()
                        for (childSnapshot in snapshot.children) {
                            val assignment = childSnapshot.getValue(CourseAssignment::class.java)
                            assignment?.let { assignments.add(it) }
                        }
                        onResult(assignments)
                        // Update local database with fetched assignments
                        viewModelScope.launch {
                            courseAssignmentDao.insertCourseAssignments(assignments)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println("Error reading assignments: ${error.message}")
                    }
                })
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
                val assignment = snapshot.getValue(CourseAssignment::class.java)
                assignment?.let {
                    viewModelScope.launch {
                        courseAssignmentDao.deleteCourseAssignments(it.assignmentID)
                    }
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                println("Error listening for updates: ${error.message}")
            }

            private fun updateLocalDatabase(snapshot: DataSnapshot) {
                val assignments = mutableListOf<CourseAssignment>()
                for (childSnapshot in snapshot.children) {
                    val assignment = childSnapshot.getValue(CourseAssignment::class.java)
                    assignment?.let { assignments.add(it) }
                }
                viewModelScope.launch {
                    courseAssignmentDao.insertCourseAssignments(assignments)
                }
            }
        })
    }
}
