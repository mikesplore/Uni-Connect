package com.mike.uniadmin.model.moduleContent.moduleAssignments

import androidx.lifecycle.LiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.CourseManager
import com.mike.uniadmin.model.announcements.uniConnectScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ModuleAssignmentRepository(private val moduleAssignmentDao: ModuleAssignmentDao) {
    private val courseCode = CourseManager.courseCode.value
    private val database =
        FirebaseDatabase.getInstance().reference.child(courseCode).child("ModuleContent")

    fun writeModuleAssignment(
        moduleID: String,
        moduleAssignment: ModuleAssignment,
        onResult: (Boolean) -> Unit
    ) {
        uniConnectScope.launch {
            // Save to local database first
            try {
                moduleAssignmentDao.insertModuleAssignment(moduleAssignment)

                // Save to Firebase
                database.child(moduleID).child("Module Assignments")
                    .child(moduleAssignment.assignmentID)
                    .setValue(moduleAssignment)
                    .addOnSuccessListener { onResult(true) }
                    .addOnFailureListener { exception ->
                        println("Error writing assignment: ${exception.message}")
                        onResult(false)
                    }
            } catch (e: Exception) {
                println("Error writing to local database: ${e.message}")
                onResult(false)
            }
        }
    }

    suspend fun getModuleAssignments(moduleID: String): LiveData<List<ModuleAssignment>> {
        listenToFirebaseChanges(moduleID)  // Pass moduleID to ensure correct updates
        return moduleAssignmentDao.getModuleAssignments(moduleID)
    }

    private fun listenToFirebaseChanges(moduleID: String) {
        val moduleAssignmentRef = database.child(moduleID).child("Module Assignments")
        moduleAssignmentRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val assignments = mutableListOf<ModuleAssignment>()
                for (childSnapshot in snapshot.children) {
                    val assignment = childSnapshot.getValue(ModuleAssignment::class.java)
                    assignment?.let { assignments.add(it) }
                }
                // Call the update function with the correct module ID
                updateLocalDatabase(assignments)
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error reading assignments: ${error.message}")
            }
        })
    }

    private fun updateLocalDatabase(assignments: List<ModuleAssignment>) {
        uniConnectScope.launch {
            try {
                // To handle updates correctly:
                // 1. Clear old assignments for this module
                // 2. Insert the updated list of assignments
                withContext(Dispatchers.IO) {
                    moduleAssignmentDao.insertModuleAssignments(assignments)
                }
            } catch (e: Exception) {
                println("Error updating local database: ${e.message}")
            }
        }
    }
}
