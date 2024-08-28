package com.mike.uniadmin.backEnd.modules

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.UniAdminPreferences
import com.mike.uniadmin.backEnd.announcements.uniConnectScope
import kotlinx.coroutines.launch


class ModuleRepository(
    private val moduleDao: ModuleDao,
    private val attendanceStateDao: AttendanceStateDao,
) {

    private val courseCode = UniAdminPreferences.courseCode.value
    private val database =
        FirebaseDatabase.getInstance().reference.child(courseCode).child("Modules")
    private val attendanceStateDatabase =
        FirebaseDatabase.getInstance().reference.child(courseCode).child("AttendanceStates")

    init {
        startModuleListener()
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
                uniConnectScope.launch {
                    attendanceStateDao.insertAttendanceStates(attendanceStates)

                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the read error (e.g., log the error)
                println("Error reading modules: ${error.message}")
            }
        })
    }

    private fun startModuleListener() {
        //first fetch data from firebase

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val modules = mutableListOf<ModuleEntity>()
                for (childSnapshot in snapshot.children) {
                    val module = childSnapshot.getValue(ModuleEntity::class.java)
                    module?.let { modules.add(it) }
                }
                uniConnectScope.launch {
                    moduleDao.insertModules(modules)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the read error (e.g., log the error)
                println("Error reading modules: ${error.message}")
            }
        })
    }

    fun fetchAttendanceStates(onResult: (List<AttendanceState>) -> Unit) {
        uniConnectScope.launch {
           val attendanceStates =  attendanceStateDao.getAttendanceStates()
            onResult(attendanceStates)

        }
    }

    fun saveAttendanceState(attendanceState: AttendanceState, onComplete: (Boolean) -> Unit) {
        uniConnectScope.launch {
            attendanceStateDao.insertAttendanceState(attendanceState)
            attendanceStateDatabase.child(attendanceState.moduleID).setValue(attendanceState)
                .addOnCompleteListener { task ->
                    onComplete(task.isSuccessful)
                }
        }
    }

    fun saveModule(module: ModuleEntity, onComplete: (Boolean) -> Unit = {}) {
        uniConnectScope.launch {
            moduleDao.insertModule(module)
            database.child(module.moduleCode).setValue(module).addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
        }
    }


    fun fetchModules(onResult: (List<ModuleEntity>) -> Unit) {
        uniConnectScope.launch {
            val cachedData = moduleDao.getModules()
            if (cachedData.isNotEmpty()) {
                onResult(cachedData)
                return@launch
            }
        }
    }


    fun deleteModule(moduleId: String, onSuccess: () -> Unit, onFailure: (Exception?) -> Unit) {
        uniConnectScope.launch {
            moduleDao.deleteModule(moduleId)
            database.child(moduleId).removeValue() // Use the consistent database reference
                .addOnSuccessListener {
                    onSuccess()
                }.addOnFailureListener { exception ->
                    onFailure(exception)
                }
        }
    }


    fun getModuleDetailsByModuleID(moduleCode: String, onResult: (ModuleEntity?) -> Unit) {
        uniConnectScope.launch {
         val fetchedData = moduleDao.getModule(moduleCode)
            onResult(fetchedData)
        }
    }
}
