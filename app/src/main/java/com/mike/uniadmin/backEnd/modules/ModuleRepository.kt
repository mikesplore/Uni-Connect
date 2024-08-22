package com.mike.uniadmin.backEnd.modules

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.courses.CourseCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val viewModelScope = CoroutineScope(Dispatchers.Main)

class ModuleRepository(
    private val moduleDao: ModuleDao,
    private val attendanceStateDao: AttendanceStateDao,
) {

    private val courseCode = CourseCode.courseCode.value
    private val database = FirebaseDatabase.getInstance().reference.child(courseCode).child("Modules")
    private val attendanceStateDatabase = FirebaseDatabase.getInstance().reference.child(courseCode).child("AttendanceStates")

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
                viewModelScope.launch {
                    attendanceStateDao.insertAttendanceStates(attendanceStates)

                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the read error (e.g., log the error)
                println("Error reading modules: ${error.message}")
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
                    println("Error reading modules: ${error.message}")
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
            attendanceStateDatabase.child(attendanceState.moduleID).setValue(attendanceState).addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
        }
    }

    fun saveModule(module: ModuleEntity, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            moduleDao.insertModule(module)
            database.child(module.moduleCode).setValue(module).addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
        }
    }


    fun fetchModules(onResult: (List<ModuleEntity>) -> Unit) {
        viewModelScope.launch {
            val cachedData = moduleDao.getModules()
                if (cachedData.isNotEmpty()) {
                    onResult(cachedData)
                    return@launch
                }
                database.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val modules = mutableListOf<ModuleEntity>()
                        for (childSnapshot in snapshot.children) {
                            val module = childSnapshot.getValue(ModuleEntity::class.java)
                            module?.let { modules.add(it) }
                        }
                       viewModelScope.launch {
                            moduleDao.insertModules(modules)
                        }
                        onResult(modules)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle the read error (e.g., log the error)
                        println("Error reading modules: ${error.message}")
                    }
                })

        }
    }


    fun deleteModule(moduleId: String, onSuccess: () -> Unit, onFailure: (Exception?) -> Unit) {
        viewModelScope.launch {
            moduleDao.deleteModule(moduleId)
            database.child(moduleId).removeValue() // Use the consistent database reference
                .addOnSuccessListener {
                    onSuccess()
                }.addOnFailureListener { exception ->
                    onFailure(exception)
                }
        }
    }

    private fun startModuleListener() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val modules = mutableListOf<ModuleEntity>()
                for (childSnapshot in snapshot.children) {
                    val module = childSnapshot.getValue(ModuleEntity::class.java)
                    module?.let { modules.add(it) }
                }
                viewModelScope.launch {
                    moduleDao.insertModules(modules)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the read error (e.g., log the error)
                println("Error reading modules: ${error.message}")
            }
        })
    }

    fun getModuleDetailsByModuleID(moduleCode: String, onResult: (ModuleEntity?) -> Unit) {
        val moduleDetailsRef = database.child(moduleCode)
        viewModelScope.launch {
            moduleDao.getModule(moduleCode)
            moduleDetailsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val moduleInfo = snapshot.getValue(ModuleEntity::class.java)
                    onResult(moduleInfo)
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error fetching module details: ${error.message}")
                    onResult(null) // Indicate failure by returning null
                }
            })
        }
    }
}
