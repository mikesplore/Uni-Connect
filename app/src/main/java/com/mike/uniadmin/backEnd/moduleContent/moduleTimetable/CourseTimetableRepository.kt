package com.mike.uniadmin.backEnd.moduleContent.moduleTimetable

import android.util.Log
import com.google.firebase.database.*
import com.mike.uniadmin.UniAdminPreferences
import com.mike.uniadmin.backEnd.announcements.uniConnectScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ModuleTimetableRepository(private val moduleTimetableDao: ModuleTimetableDao) {
    private val courseCode = UniAdminPreferences.courseCode.value
    private val database = FirebaseDatabase.getInstance().reference.child(courseCode).child("ModuleContent")


    init {
        // Start listening for changes in Firebase
        listenForFirebaseUpdates()
    }

    // Write a module timetable to both local and remote databases
    fun writeModuleTimetable(
        moduleID: String,
        moduleTimetable: ModuleTimetable,
        onResult: (Boolean) -> Unit
    ) {
        uniConnectScope.launch {
            try {
                // Insert into local database
                moduleTimetableDao.insertModuleTimetable(moduleTimetable)
                // Insert into Firebase
                val moduleTimetableRef = database.child(moduleID).child("Module Timetable")
                moduleTimetableRef.child(moduleTimetable.timetableID).setValue(moduleTimetable)
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

    fun getModuleTimetables(moduleID: String, onResult: (List<ModuleTimetable>) -> Unit) {
        uniConnectScope.launch {
            Log.d("Timetables", "Searching repository timetable for module: $moduleID")

            // Fetch cached data from local database
            val cachedData = moduleTimetableDao.getModuleTimetables(moduleID)

            if (cachedData.isNotEmpty()) {
                // Return cached data if available
                onResult(cachedData)
                Log.d("Timetables", "Found timetable in database")
            } else {
                Log.d("Timetables", "No timetable found in database, fetching from repository")
                val moduleTimetableRef = database.child(moduleID).child("Module Timetable")

                // Fetch data from the remote database
                moduleTimetableRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val timetables = mutableListOf<ModuleTimetable>()
                        for (childSnapshot in snapshot.children) {
                            val timetable = childSnapshot.getValue(ModuleTimetable::class.java)
                            timetable?.let {
                                Log.d("Timetables", "Found timetable in repository")
                                timetables.add(it)
                            }
                        }
                        uniConnectScope.launch {
                            // Update local database with fetched timetables
                            if (timetables.isNotEmpty()) {
                                moduleTimetableDao.insertModuleTimetables(timetables)
                                val updatedData = moduleTimetableDao.getModuleTimetables(moduleID)
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
                moduleTimetableRef.addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        val timetable = snapshot.getValue(ModuleTimetable::class.java)
                        timetable?.let {
                            uniConnectScope.launch {
                                moduleTimetableDao.insertModuleTimetable(it)
                                val updatedData = moduleTimetableDao.getModuleTimetables(moduleID)
                                onResult(updatedData)
                            }
                        }
                    }

                    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                        val timetable = snapshot.getValue(ModuleTimetable::class.java)
                        timetable?.let {
                            uniConnectScope.launch {
                                moduleTimetableDao.insertModuleTimetable(it)
                                val updatedData = moduleTimetableDao.getModuleTimetables(moduleID)
                                onResult(updatedData)
                            }
                        }
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        val timetable = snapshot.getValue(ModuleTimetable::class.java)
                        timetable?.let {
                            uniConnectScope.launch {
                                moduleTimetableDao.deleteModuleTimetable(it.timetableID)
                                val updatedData = moduleTimetableDao.getModuleTimetables(moduleID)
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


    fun getAllModuleTimetables(onResult: (List<ModuleTimetable>) -> Unit) {
        uniConnectScope.launch(Dispatchers.IO) { // Ensure this runs on a background thread
            val cachedData = moduleTimetableDao.getAllModuleTimetables()
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
                val timetable = snapshot.getValue(ModuleTimetable::class.java)
                timetable?.let {
                    uniConnectScope.launch {
                        moduleTimetableDao.deleteModuleTimetable(it.timetableID)
                    }
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                println("Error listening for updates: ${error.message}")
            }

            private fun updateLocalDatabase(snapshot: DataSnapshot) {
                val timetables = mutableListOf<ModuleTimetable>()
                for (childSnapshot in snapshot.children) {
                    val timetable = childSnapshot.getValue(ModuleTimetable::class.java)
                    timetable?.let { timetables.add(it) }
                }
                uniConnectScope.launch {
                    moduleTimetableDao.insertModuleTimetables(timetables)
                }
            }
        })
    }

    fun getTimetableByDay(day: String, onResult: (List<ModuleTimetable>?) -> Unit) {
        uniConnectScope.launch {
            val cachedData = moduleTimetableDao.getTimetableByDay("Tuesday")
            Log.d("Timetables today", "Searching repository timetable for day: $day")
            if (cachedData.isNotEmpty()) {
                onResult(cachedData)
                Log.d("Timetables today", "Found timetable in database: $cachedData")
            } else {
                Log.e("Empty", "Empty database")
            }
        }
    }
}
