package com.mike.uniadmin.dataModel.coursecontent.courseassignments

import com.mike.uniadmin.dataModel.coursecontent.courseassignments.CourseAssignment
import com.mike.uniadmin.dataModel.coursecontent.courseassignments.CourseAssignmentDao



import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.model.MyDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val viewModelScope = CoroutineScope(Dispatchers.Main)

class CourseAssignmentRepository(private val courseAssignmentDao: CourseAssignmentDao) {
    private val database = FirebaseDatabase.getInstance().reference.child("CourseContent")

    //Course Content database
    fun writeCourseAssignment(
        courseID: String,
        courseAssignment: CourseAssignment,
        onResult: (Boolean) -> Unit
    ) {
        com.mike.uniadmin.dataModel.coursecontent.coursedetails.viewModelScope.launch {
            courseAssignmentDao.insertCourseAssignment(courseAssignment)
            val courseAssignmentRef = MyDatabase.database.child(courseID)
                .child("courseAssignments")

            courseAssignmentRef.child(courseAssignment.assignmentID)
                .setValue(courseAssignment).addOnSuccessListener {
                    onResult(true) // Indicate success
                }.addOnFailureListener { exception ->
                    println("Error writing assignment: ${exception.message}")
                    onResult(false) // Indicate failure
                }
        }
    }


    fun getCourseAssignments(courseID: String, onResult: (List<CourseAssignment>) -> Unit) {
        com.mike.uniadmin.dataModel.coursecontent.coursedetails.viewModelScope.launch {
            val cachedData = courseAssignmentDao.getCourseAssignments(courseID)
            if (cachedData.isNotEmpty()) {
                onResult(cachedData)
            } else {
                val courseAssignmentRef =
                    database.child(courseID).child("courseAssignments")

                courseAssignmentRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val assignments = mutableListOf<CourseAssignment>()
                        for (childSnapshot in snapshot.children) {
                            val assignment =
                                childSnapshot.getValue(CourseAssignment::class.java)
                            assignment?.let { assignments.add(it) }
                        }
                        onResult(assignments)
                    }

                    override fun onCancelled(error: DatabaseError) {

                        println("Error reading assignments: ${error.message}")
                    }
                })
            }
        }
    }

}