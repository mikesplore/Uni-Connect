package com.mike.uniadmin.dataModel.programs

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


val viewModelScope = CoroutineScope(Dispatchers.Main)


class ProgramRepository(
    private val programDao: ProgramDao,
    private val programStateDao: ProgramStateDao
) {

    private val database = FirebaseDatabase.getInstance().reference.child("Programs")
    private val programStateDatabase =
        FirebaseDatabase.getInstance().reference.child("ProgramStates")

    init {
        startProgramListener()
        startProgramStateListener()
    }


    private fun startProgramStateListener() {
        programStateDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val programStates = mutableListOf<ProgramState>()
                for (childSnapshot in snapshot.children) {
                    val programState = childSnapshot.getValue(ProgramState::class.java)
                    programState?.let { programStates.add(it) }
                }
                viewModelScope.launch {
                    programStateDao.insertProgramStates(programStates)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error reading program states: ${error.message}")
            }
        })
    }

    fun fetchProgramStates(onResult: (List<ProgramState>) -> Unit) {
        viewModelScope.launch {
            programStateDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val programStates = mutableListOf<ProgramState>()
                    for (childSnapshot in snapshot.children) {
                        val programState = childSnapshot.getValue(ProgramState::class.java)
                        programState?.let { programStates.add(it) }
                    }
                    viewModelScope.launch {
                        programStateDao.insertProgramStates(programStates)
                    }
                    onResult(programStates)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle the read error (e.g., log the error)
                    println("Error reading programs: ${error.message}")
                    viewModelScope.launch {
                        val cachedData = programStateDao.getProgramStates()
                        onResult(cachedData)
                    }
                }
            })
        }
    }

    fun saveProgramState(programState: ProgramState, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            programStateDao.insertProgramState(programState)
            programStateDatabase.child(programState.programID).setValue(programState)
                .addOnCompleteListener { task ->
                    onComplete(task.isSuccessful)
                }
        }
    }

    fun saveProgram(program: ProgramEntity, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            programDao.insertProgram(program)
            database.child(program.programCode).setValue(program).addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
        }
    }


    fun fetchPrograms(onResult: (List<ProgramEntity>) -> Unit) {
        viewModelScope.launch {
            val cachedData = programDao.getPrograms()
            if (cachedData.isNotEmpty()) {
                onResult(cachedData)
            } else {
                database.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val programs = mutableListOf<ProgramEntity>()
                        for (childSnapshot in snapshot.children) {
                            val program = childSnapshot.getValue(ProgramEntity::class.java)
                            program?.let { programs.add(it) }
                        }
                        viewModelScope.launch {
                            programDao.insertPrograms(programs)
                        }
                        onResult(programs)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println("Error reading programs: ${error.message}")
                    }
                })
            }
        }
    }


    fun deleteProgram(programId: String, onSuccess: () -> Unit, onFailure: (Exception?) -> Unit) {
        viewModelScope.launch {
            programDao.deleteProgram(programId)
            database.child(programId).removeValue() // Use the consistent database reference
                .addOnSuccessListener {
                    onSuccess()
                }.addOnFailureListener { exception ->
                    onFailure(exception)
                }
        }
    }

    private fun startProgramListener() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val programs = mutableListOf<ProgramEntity>()
                for (childSnapshot in snapshot.children) {
                    val program = childSnapshot.getValue(ProgramEntity::class.java)
                    program?.let { programs.add(it) }
                }
                viewModelScope.launch {
                    programDao.insertPrograms(programs) // Update local cache
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error reading programs: ${error.message}")
            }
        })

        // Use ChildEventListener for more detailed events, especially handling deletions
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val program = snapshot.getValue(ProgramEntity::class.java)
                program?.let {
                    viewModelScope.launch {
                        programDao.insertProgram(it)
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val program = snapshot.getValue(ProgramEntity::class.java)
                program?.let {
                    viewModelScope.launch {
                        programDao.insertProgram(it)
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val program = snapshot.getValue(ProgramEntity::class.java)
                program?.let {
                    viewModelScope.launch {
                        programDao.deleteProgram(it.programCode)
                    }
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle if needed
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error handling child events: ${error.message}")
            }
        })
    }

    fun getProgramDetailsByProgramID(programCode: String, onResult: (ProgramEntity?) -> Unit) {
        val programDetailsRef = database.child(programCode)
        viewModelScope.launch {
            programDao.getProgram(programCode)
            programDetailsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val programInfo = snapshot.getValue(ProgramEntity::class.java)
                    onResult(programInfo)
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error fetching program details: ${error.message}")
                    onResult(null) // Indicate failure by returning null
                }
            })
        }
    }

    fun insertProgramCode(program: Program) {
        viewModelScope.launch {
            programDao.insertProgramCode(program)
        }
    }

    fun getProgramCode(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val programCode = programDao.getProgramCode()
            onResult(programCode)
        }
    }
}
