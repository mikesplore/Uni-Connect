package com.mike.uniadmin.backEnd.modules

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.CourseManager.courseCode
import com.mike.uniadmin.backEnd.announcements.uniConnectScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ModuleRepository(
    private val moduleDao: ModuleDao,
    private val attendanceStateDao: AttendanceStateDao,
) {

    private var moduleDatabase: DatabaseReference? = null
    private var attendanceStateDatabase: DatabaseReference? = null


    init {
        observeCourseCode()
        startModuleListener()
        startAttendanceStateListener()
    }

    private fun observeCourseCode() {
        // Observe changes in courseCode from CourseManager
        uniConnectScope.launch(Dispatchers.Main) {
            courseCode.collectLatest { code ->
                Log.d("UniAdminPreferences", "Course Code in the scope: $code")
                if (code.isNotEmpty()) {
                    initializeDatabases(code)
                    startModuleListener()
                    startAttendanceStateListener()
                } else {
                    moduleDatabase = null
                    attendanceStateDatabase = null
                }
            }
        }
    }

    private fun initializeDatabases(courseCode: String) {
        moduleDatabase = FirebaseDatabase.getInstance().reference.child(courseCode).child("Modules")
        attendanceStateDatabase =
            FirebaseDatabase.getInstance().reference.child(courseCode).child("AttendanceStates")
    }


    private fun startAttendanceStateListener() {
        Log.d("UniAdminPreferences", "Attendance state listener started")
        attendanceStateDatabase?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val attendanceStates = snapshot.children.mapNotNull { it.getValue(AttendanceState::class.java) }
                // Update Room database in the background
                uniConnectScope.launch(Dispatchers.IO) {
                    attendanceStateDao.insertAttendanceStates(attendanceStates)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ModuleRepository", "Error reading attendance states: ${error.message}")
            }
        })
    }

    // Fetches attendance states from Room database only
    fun fetchAttendanceStates(onResult: (List<AttendanceState>) -> Unit) {
        uniConnectScope.launch(Dispatchers.IO) {
            val cachedData = attendanceStateDao.getAttendanceStates()
            withContext(Dispatchers.Main) {
                onResult(cachedData)
            }
        }
    }

    fun saveAttendanceState(attendanceState: AttendanceState, onComplete: (Boolean) -> Unit) {
        uniConnectScope.launch(Dispatchers.IO) {
            try {
                attendanceStateDao.insertAttendanceState(attendanceState)
                attendanceStateDatabase?.child(attendanceState.moduleID)?.setValue(attendanceState)
                    ?.addOnCompleteListener { task ->
                        launch(Dispatchers.Main) {
                            onComplete(task.isSuccessful)
                        }
                    }
            } catch (e: Exception) {
                Log.e("ModuleRepository", "Error saving attendance state: ${e.message}")
                withContext(Dispatchers.Main) {
                    onComplete(false)
                }
            }
        }
    }

    fun saveModule(module: ModuleEntity, onComplete: (Boolean) -> Unit = {}) {
        saveModuleToFirebase(module)
        uniConnectScope.launch(Dispatchers.IO) {
            try {
                moduleDao.insertModule(module)
                onComplete(true) // Assuming Room insertion is successful
            } catch (e: Exception) {
                Log.e("ModuleRepository", "Error saving module: ${e.message}")
                withContext(Dispatchers.Main) {
                    onComplete(false)
                }
            }
        }
    }

    private fun saveModuleToFirebase(module: ModuleEntity, onComplete: (Boolean) -> Unit = {}) {
        moduleDatabase?.child(module.moduleCode)?.setValue(module)?.addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }

    fun fetchModules(onResult: (List<ModuleEntity>) -> Unit) {
        Log.d("ModuleRepository", "module fetching started")

        // First, fetch cached data from Room without blocking the main thread
        uniConnectScope.launch(Dispatchers.IO) {
            val cachedData = moduleDao.getModules()

            // Return cached data first
            withContext(Dispatchers.Main) {
                onResult(cachedData)
            }
        }
        fetchModulesFromFirebase(onResult)
    }

    fun fetchModulesFromFirebase(onResult: (List<ModuleEntity>) -> Unit) {
        Log.d("UniAdminPreferences", "Course Code in module screen: ${courseCode.value}")
        moduleDatabase?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(
                    "UniAdminPreferences",
                    "module fetching from firebase in path: $moduleDatabase"
                )
                Log.d("ModuleRepository", "module fetching from path: $moduleDatabase")
                val modules = mutableListOf<ModuleEntity>()
                for (childSnapshot in snapshot.children) {
                    val module = childSnapshot.getValue(ModuleEntity::class.java)
                    module?.let {
                        modules.add(it)
                        Log.d("UniAdminPreferences", "Module fetched from Firebase: $it")
                    }
                }
                Log.d("ModuleRepository", "Fetched from firebase: $modules")

                // Insert fetched modules into Room
                uniConnectScope.launch(Dispatchers.IO) {
                    moduleDao.insertModules(modules)
                    Log.d("ModuleRepository", "Modules inserted into Room: ${modules.size} modules")

                    // Fetch updated modules from Room
                    val updatedModules = moduleDao.getModules()
                    withContext(Dispatchers.Main) {
                        onResult(updatedModules)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ModuleRepository", "Error reading modules: ${error.message}")
            }
        })
    }


    fun deleteModule(moduleId: String, onSuccess: () -> Unit, onFailure: (Exception?) -> Unit) {
        uniConnectScope.launch(Dispatchers.IO) {
            try {
                moduleDao.deleteModule(moduleId)
                moduleDatabase?.child(moduleId)?.removeValue()
                    ?.addOnSuccessListener {
                        launch(Dispatchers.Main) {
                            onSuccess()
                        }
                    }
                    ?.addOnFailureListener { exception ->
                        launch(Dispatchers.Main) {
                            onFailure(exception)
                        }
                    }
            } catch (e: Exception) {
                Log.e("ModuleRepository", "Error deleting module: ${e.message}")
                withContext(Dispatchers.Main) {
                    onFailure(e)
                }
            }
        }
    }

    private fun startModuleListener() {
        Log.d("ModuleRepository", "module listener started")
        moduleDatabase?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val modules = mutableListOf<ModuleEntity>()
                for (childSnapshot in snapshot.children) {
                    val module = childSnapshot.getValue(ModuleEntity::class.java)
                    module?.let { modules.add(it) }
                }
                uniConnectScope.launch(Dispatchers.IO) {
                    moduleDao.insertModules(modules)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ModuleRepository", "Error reading modules: ${error.message}")
            }
        })
    }

    fun getModuleDetailsByModuleID(moduleCode: String, onResult: (ModuleEntity?) -> Unit) {
        val moduleDetailsRef = moduleDatabase?.child(moduleCode)
        uniConnectScope.launch(Dispatchers.IO) {
            val localData = moduleDao.getModule(moduleCode)
            if (localData != null) {
                withContext(Dispatchers.Main) {
                    onResult(localData)
                }
            } else {
                moduleDetailsRef?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val moduleInfo = snapshot.getValue(ModuleEntity::class.java)

                        uniConnectScope.launch {
                            withContext(Dispatchers.Main) {
                                onResult(moduleInfo)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(
                            "ModuleRepository",
                            "Error fetching module details: ${error.message}"
                        )
                        uniConnectScope.launch {
                            withContext(Dispatchers.Main) {
                                onResult(null)
                            }
                        }
                    }
                })
            }
        }
    }
}
