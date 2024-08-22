package com.mike.uniadmin.backEnd.moduleContent.moduleAssignments

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.courses.CourseCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val viewModelScope = CoroutineScope(Dispatchers.Main)

class ModuleAssignmentRepository(private val moduleAssignmentDao: ModuleAssignmentDao) {
    private val courseCode = CourseCode.courseCode.value
    private val database = FirebaseDatabase.getInstance().reference.child(courseCode).child("ModuleContent")

    //Module Content database
    fun writeModuleAssignment(
        moduleID: String,
        moduleAssignment: ModuleAssignment,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            moduleAssignmentDao.insertModuleAssignment(moduleAssignment)
            database.child(moduleID).child("Module Assignments").child(moduleAssignment.assignmentID)
                .setValue(moduleAssignment).addOnSuccessListener {
                    onResult(true) // Indicate success
                }.addOnFailureListener { exception ->
                    println("Error writing assignment: ${exception.message}")
                    onResult(false) // Indicate failure
                }
        }
    }


    fun getModuleAssignments(moduleID: String, onResult: (List<ModuleAssignment>) -> Unit) {
        viewModelScope.launch {
            val cachedData = moduleAssignmentDao.getModuleAssignments(moduleID)
            if (cachedData.isNotEmpty()) {
                onResult(cachedData)
            } else {
                val moduleAssignmentRef =
                    database.child(moduleID).child("Module Assignments")

                moduleAssignmentRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val assignments = mutableListOf<ModuleAssignment>()
                        for (childSnapshot in snapshot.children) {
                            val assignment =
                                childSnapshot.getValue(ModuleAssignment::class.java)
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