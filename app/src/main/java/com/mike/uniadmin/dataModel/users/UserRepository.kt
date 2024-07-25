package com.mike.uniadmin.dataModel.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val viewModelScope = CoroutineScope(Dispatchers.Main)

class UserRepository
    (
    private val userDao: UserDao,
    private val userStateDao: UserStateDao,
    private val accountDeletionDao: AccountDeletionDao,
    private val userPreferencesDao: UserPreferencesDao
) {
    private val database = FirebaseDatabase.getInstance().reference



    init {
        startUserListener()
        startUserStateListener()
    }

    private fun startUserListener() {
        database.child("Users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = mutableListOf<UserEntity>()
                for (childSnapshot in snapshot.children) {
                    val user = childSnapshot.getValue(UserEntity::class.java)
                    user?.let { users.add(it) }
                }
                viewModelScope.launch {
                    userDao.insertUsers(users)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the read error (e.g., log the error)
                println("Error reading users: ${error.message}")
            }
        })
    }

    private fun startUserStateListener() {
        database.child("Users Online Status").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userStates = mutableListOf<UserStateEntity>()
                for (childSnapshot in snapshot.children) {
                    val userState = childSnapshot.getValue(UserStateEntity::class.java)
                    userState?.let { userStates.add(it) }
                }
                viewModelScope.launch {
                    userStateDao.insertUserStates(userStates)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the read error (e.g., log the error)
                println("Error reading user states: ${error.message}")
            }
        })
    }

    fun fetchUsers(onResult: (List<UserEntity>) -> Unit) {
        viewModelScope.launch {
            val cachedData = userDao.getUsers()
            if (cachedData.isNotEmpty()) {
                onResult(cachedData)
            }
        }
    }

    fun saveUser(user: UserEntity, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            userDao.insertUser(user)
            database.child("Users").child(user.id).setValue(user).addOnCompleteListener { task ->
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
                database.child("Users").orderByChild("email").equalTo(email)
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
                database.child("Users").orderByChild("id").equalTo(admissionNumber)
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


    fun deleteUser(userId: String, onSuccess: () -> Unit, onFailure: (Exception?) -> Unit) {
        viewModelScope.launch {
            userDao.deleteUser(userId)
            database.child("Users").child(userId)
                .removeValue() // Use the consistent database reference
                .addOnSuccessListener {
                    onSuccess()
                }.addOnFailureListener { exception ->
                    onFailure(exception)
                }
        }
    }

    fun writeAccountDeletionData(
        accountDeletion: AccountDeletionEntity,
        onSuccess: (Boolean) -> Unit
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

    fun checkAccountDeletionData(userId: String, onComplete: (AccountDeletionEntity) -> Unit){
        viewModelScope.launch {
            val cachedData = accountDeletionDao.getAccountDeletion(userId)
            if (cachedData != null) {
                onComplete(cachedData)
            }else{
                database.child("Account Deletion").child(userId).get()
                    .addOnSuccessListener { snapshot ->
                        val accountDeletion = snapshot.getValue(AccountDeletionEntity::class.java)
                        accountDeletion?.let { onComplete(it) }
                        }.addOnFailureListener {
                        println("Error reading account deletion: ${it.message}")
                    }
            }            }
    }

    fun writePreferences(preferences: UserPreferencesEntity, onSuccess: (Boolean) -> Unit) {
        preferences.studentID?.let {
            viewModelScope.launch {
                userPreferencesDao.insertUserPreferences(preferences)
                database.child(" User Preferences").child(it).setValue(preferences)
                    .addOnSuccessListener {
                        onSuccess(true)
                    }.addOnFailureListener {
                        onSuccess(false)
                    }
            }
        }
    }

    fun fetchPreferences(userId: String, onPreferencesFetched: (UserPreferencesEntity?) -> Unit) {
        viewModelScope.launch {
            val cachedPreferences = userPreferencesDao.getUserPreferences(userId)
            if (cachedPreferences != null) {
                onPreferencesFetched(cachedPreferences)
            } else {

                database.child(" User Preferences").child(userId).get()
                    .addOnSuccessListener { snapshot ->
                        val preferences = snapshot.getValue(UserPreferencesEntity::class.java)
                        onPreferencesFetched(preferences)
                    }.addOnFailureListener {
                        onPreferencesFetched(null) // Handle failure, e.g., by returning null
                    }
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
        database.child("Users Online Status").child(userId).addValueEventListener(object : ValueEventListener {
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