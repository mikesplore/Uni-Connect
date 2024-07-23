package com.mike.uniadmin.dataModel.coursecontent.courseassignments


import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.model.MyDatabase

class CourseAssignmentRepository {
    private val database = FirebaseDatabase.getInstance().reference

    //Course Content database
    fun writeCourseAssignment(courseID: String, courseAssignment: CourseAssignment, onResult: (Boolean) -> Unit) {
        val courseAssignmentRef = MyDatabase.database.child("CourseContent")
            .child(courseID)
            .child("courseAssignments")

        courseAssignmentRef.child(courseAssignment.assignmentID).setValue(courseAssignment)
            .addOnSuccessListener {
                onResult(true) // Indicate success
            }
            .addOnFailureListener { exception ->
                println("Error writing assignment: ${exception.message}")
                onResult(false) // Indicate failure
            }
    }


    fun getCourseAssignments(courseID: String, onResult: (List<CourseAssignment>) -> Unit) {
        val courseAssignmentRef = database.child("CourseContent")
            .child(courseID)
            .child("courseAssignments")

        courseAssignmentRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val assignments = mutableListOf<CourseAssignment>()
                for (childSnapshot in snapshot.children) {
                    val assignment = childSnapshot.getValue(CourseAssignment::class.java)
                    assignment?.let { assignments.add(it) }
                }
                onResult(assignments)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the read error (e.g., log the error)
                println("Error reading assignments: ${error.message}")
            }
        })
    }

}