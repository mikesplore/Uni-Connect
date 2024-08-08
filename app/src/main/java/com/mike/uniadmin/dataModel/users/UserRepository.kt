package com.mike.uniadmin.dataModel.users

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

val viewModelScope = CoroutineScope(Dispatchers.Main)

class UserRepository
    (
    private val userDao: UserDao,
    private val userStateDao: UserStateDao,
    private val accountDeletionDao: AccountDeletionDao,
    private val userPreferencesDao: UserPreferencesDao,
    private val databaseDao: DatabaseDao
) {
    private val database = FirebaseDatabase.getInstance().reference

    init {
        startUserListener()
        startUserStateListener()
    }


    fun getSignedInUser(onSuccess: (SignedInUser?) -> Unit) {
        viewModelScope.launch {
            val signedInUser = userDao.getSignedInUser()
            onSuccess(signedInUser)
        }
    }


    fun setSignedInUser(signedInUser: SignedInUser) {
        viewModelScope.launch {
            userDao.insertSignedInUser(signedInUser)
        }
    }

    fun deleteSignedInUser() {
        viewModelScope.launch {
            userDao.deleteSignedInUser()
        }
    }

    fun deleteAllTables() {
        viewModelScope.launch {
            databaseDao.deleteAllTables()
        }
    }

    private fun <T> startDatabaseListener(
        path: String, convert: (DataSnapshot) -> T?, onResult: suspend (List<T>) -> Unit
    ) {
        database.child(path).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<T>()
                for (childSnapshot in snapshot.children) {
                    val item = convert(childSnapshot)
                    item?.let { items.add(it) }
                }
                viewModelScope.launch {
                    onResult(items)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error reading $path: ${error.message}")
            }
        })
    }

    private fun startUserListener() {
        startDatabaseListener("Users",
            convert = { it.getValue(UserEntity::class.java) },
            onResult = { users ->
                userDao.insertUsers(users)
            })
    }

    private fun startUserStateListener() {
        startDatabaseListener("Users Online Status",
            convert = { it.getValue(UserStateEntity::class.java) },
            onResult = { userStates ->
                userStateDao.insertUserStates(userStates)
            })
    }

    fun fetchUsers(onResult: (List<UserEntity>) -> Unit) {
        viewModelScope.launch {
            // 1. Fetch users from local database first
            val localUsers = userDao.getUsers()
            onResult(localUsers)

            // 2. Then, fetch from remote and update local if needed
            try {
                val remoteUsers = fetchUsersFromRemoteDatabase()
                if (remoteUsers != localUsers) { // Check for differences
                    userDao.insertUsers(remoteUsers)
                    onResult(remoteUsers) // Update UI if there are changes
                }
            } catch (e: Exception) {
                println("Error fetching users from remote database: ${e.message}")
            }
        }
    }

    private suspend fun fetchUsersFromRemoteDatabase(): List<UserEntity> {
        return suspendCoroutine { continuation ->
            val allUsers = mutableListOf<UserEntity>()

            // Fetch users from "Users" node
            database.child("Users").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (childSnapshot in snapshot.children) {
                        val user = childSnapshot.getValue(UserEntity::class.java)
                        user?.let { allUsers.add(it) }
                    }

                    // Fetch users from "Admins" node after fetching from "Users"
                    database.child("Admins")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (childSnapshot in snapshot.children) {
                                    val admin = childSnapshot.getValue(UserEntity::class.java)
                                    admin?.let { allUsers.add(it) }
                                }
                                continuation.resume(allUsers) // Resume with combined list
                            }

                            override fun onCancelled(error: DatabaseError) {
                                continuation.resumeWithException(Exception("Error reading admins: ${error.message}"))
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWithException(Exception("Error reading users: ${error.message}"))
                }
            })
        }
    }


    fun saveUser(user: UserEntity, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            userDao.insertUser(user)
            database.child("Admins").child(user.id).setValue(user).addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
        }
    }

    fun fetchUserDataByEmail(email: String, callback: (UserEntity?) -> Unit) {
        viewModelScope.launch {
            val databaseUser = userDao.getUserByEmail(email)
            if (databaseUser != null) {
                callback(databaseUser)
            } else {
                database.child("Admins").orderByChild("email").equalTo(email)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val userSnapshot =
                                snapshot.children.firstOrNull() // Get the first matching user
                            val user = userSnapshot?.getValue(UserEntity::class.java)
                            callback(user) // Return the User object or null if not found
                        }

                        override fun onCancelled(error: DatabaseError) {
                            callback(null) // Handle or log the error as needed
                        }
                    })
            }
        }
    }

    fun fetchUserDataByAdmissionNumber(admissionNumber: String, callback: (UserEntity?) -> Unit) {
        viewModelScope.launch {
            val databaseUser = userDao.getUserByID(admissionNumber)
            if (databaseUser != null) {
                callback(databaseUser)
            } else {
                // Check Users node
                val usersQuery = database.child("Users").orderByChild("id").equalTo(admissionNumber)
                usersQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userSnapshot = snapshot.children.firstOrNull()
                        val user = userSnapshot?.getValue(UserEntity::class.java)
                        if (user != null) {
                            callback(user) // Found in Users node
                        } else {
                            // Check Admins node if not found in Users
                            val adminsQuery =
                                database.child("Admins").orderByChild("id").equalTo(admissionNumber)
                            adminsQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val adminSnapshot = snapshot.children.firstOrNull()
                                    val admin = adminSnapshot?.getValue(UserEntity::class.java)
                                    callback(admin) // Return admin or null
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    callback(null) // Handle or log the error
                                }
                            })
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        callback(null) // Handle or log the error
                    }
                })
            }
        }
    }


    fun deleteUser(userId: String, onSuccess: (Boolean) -> Unit) {
        viewModelScope.launch {
            userDao.deleteUser(userId)
            database.child("Users").child(userId).removeValue().addOnSuccessListener {
                onSuccess(true)
            }.addOnFailureListener { exception ->
                onSuccess(false)
                Log.e("Error", "$exception")
            }
        }
    }


    fun writeAccountDeletionData(
        accountDeletion: AccountDeletionEntity, onSuccess: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            accountDeletionDao.insertAccountDeletion(accountDeletion)
            database.child("Account Deletion").child(accountDeletion.id).setValue(accountDeletion)
                .addOnSuccessListener {
                    onSuccess(true)
                }.addOnFailureListener {
                    onSuccess(false)
                }
        }
    }

    fun checkAccountDeletionData(userId: String, onComplete: (AccountDeletionEntity?) -> Unit) {
        viewModelScope.launch {
            val cachedData = accountDeletionDao.getAccountDeletion(userId)
            if (cachedData != null) {
                onComplete(cachedData)
            } else {
                database.child("Account Deletion").child(userId).get()
                    .addOnSuccessListener { snapshot ->
                        val accountDeletion = snapshot.getValue(AccountDeletionEntity::class.java)
                        accountDeletion?.let { onComplete(it) }
                    }.addOnFailureListener {
                        println("Error reading account deletion: ${it.message}")
                    }
            }
        }
    }

    fun writePreferences(preferences: UserPreferencesEntity, onSuccess: (Boolean) -> Unit) {
        preferences.studentID.let {
            viewModelScope.launch {
                userPreferencesDao.insertUserPreferences(preferences)
                onSuccess(true)
            }
        }
    }

    fun fetchPreferences(userId: String, onPreferencesFetched: (UserPreferencesEntity?) -> Unit) {
        viewModelScope.launch {
            val cachedPreferences = userPreferencesDao.getUserPreferences(userId)
            if (cachedPreferences != null) {
                onPreferencesFetched(cachedPreferences)
            }
        }
    }


    fun fetchAllUserStatuses(onUserStatesFetched: (List<UserStateEntity>) -> Unit) {
        database.child("Users Online Status").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userStates = mutableListOf<UserStateEntity>()
                for (childSnapshot in snapshot.children) {
                    val userState = childSnapshot.getValue(UserStateEntity::class.java)
                    userState?.let { userStates.add(it) }
                }
                onUserStatesFetched(userStates)
            }

            override fun onCancelled(error: DatabaseError) {
                // Firebase connection failed, fetch from local database
                viewModelScope.launch {
                    val cachedUserStates = userStateDao.getAllUserStates()
                    onUserStatesFetched(cachedUserStates)
                }
            }
        })
    }

    fun fetchUserStateByUserId(userId: String, onUserStateFetched: (UserStateEntity?) -> Unit) {
        database.child("Users Online Status").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userState = snapshot.getValue(UserStateEntity::class.java)
                    onUserStateFetched(userState)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Firebase connection failed, fetch from local database
                    viewModelScope.launch {
                        val cachedUserState = userStateDao.getUserState(userId)
                        onUserStateFetched(cachedUserState)
                    }
                }
            })
    }


}