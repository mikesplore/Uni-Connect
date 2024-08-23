package com.mike.uniadmin

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.backEnd.users.UserEntity
import com.mike.uniadmin.backEnd.users.UserStateEntity
import com.mike.uniadmin.helperFunctions.Global
import com.mike.uniadmin.helperFunctions.MyDatabase
import com.mike.uniadmin.helperFunctions.MyDatabase.writeUserActivity
import com.mike.uniadmin.notification.createNotificationChannel
import com.mike.uniadmin.settings.BiometricPromptManager
import com.mike.uniadmin.ui.theme.UniAdminTheme
import com.mike.uniadmin.ui.theme.CommonComponents as CC


class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private var currentUser: UserEntity = UserEntity()
    val promptManager by lazy {
        BiometricPromptManager(this)
    }
    private val database = FirebaseDatabase.getInstance()
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        if (user != null) {
            val userEmail = user.email ?: "" // Handle potential null email
            fetchUserDataByEmail(userEmail) { fetchedUser ->
                if (fetchedUser == null) {
                    Log.e("UniAdminMainActivity", "User not found for email: $userEmail")
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                    return@fetchUserDataByEmail // Exit the callback
                } else {
                    Log.d("UniAdminMainActivity", "User found: ${fetchedUser.id}")
                    currentUser = fetchedUser
                    setupLifecycleObservers(currentUser.id)
                }
            }
        } else {
            lifecycle.removeObserver(lifecycleObserver)
            Log.d("UniAdminMainActivity", "No user logged in")
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            writeUserOnlineStatus(currentUser.id)
        }

        override fun onStop(owner: LifecycleOwner) {
            writeUserOfflineStatus(currentUser.id)
        }

        override fun onDestroy(owner: LifecycleOwner) {
            writeUserOfflineStatus(currentUser.id)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        UniAdminPreferences.initialize(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setTheme(R.style.Theme_UniAdmin)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE)
        createNotificationChannel(this)

        // Initialize DeviceTheme with sharedPreferences
        auth.addAuthStateListener(authStateListener)

        setContent {
            UniAdminTheme(dynamicColor = false, darkTheme = UniAdminPreferences.darkMode.value) {
                NavigationGraph(this, this)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        auth.removeAuthStateListener(authStateListener)
    }

    private fun setupLifecycleObservers(userId: String) {
        lifecycle.addObserver(lifecycleObserver)
        registerConnectivityListener(this, userId)
    }

    private fun writeUserOnlineStatus(userId: String) {
        val userStatusRef = database.getReference().child("Users Online Status").child(userId)
        val userState = UserStateEntity(
            userID = userId,
            id = "${userId}2024",
            online = "online",
            lastTime = CC.getTimeStamp(),
            lastDate = CC.getTimeStamp()
        )
        userStatusRef.setValue(userState) // Set the whole UserStateEntity object
        userStatusRef.onDisconnect()
            .setValue(userState.copy(online = "offline")) // Set offline on disconnect
        Log.d("User status", "Online: $userState")
        writeUserActivity(userState) { success ->
            if (!success) {
                Log.e("MainActivity", "Failed to write user online status")
            }
        }

    }

    private fun writeUserOfflineStatus(userId: String) {
        val userStatusRef = database.getReference().child("Users Online Status").child(userId)
        val userState = UserStateEntity(
            userID = userId,
            id = "${userId}2024",
            online = "offline",
            lastTime = CC.getTimeStamp(),
            lastDate = CC.getTimeStamp()
        )
        userStatusRef.setValue(userState) // Set the whole UserStateEntity object
        Log.d("User status", "Offline: $userState")
        writeUserActivity(userState) { success ->
            if (!success) {
                Log.e("MainActivity", "Failed to write user offline status")
            }
        }
    }

    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                Global.showAlert.value = false
            } else {
                // Permission already granted
                Global.showAlert.value = true
                sharedPreferences.edit().putBoolean("NotificationPermissionGranted", true).apply()
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Global.showAlert.value = true
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
                sharedPreferences.edit().putBoolean("NotificationPermissionGranted", true).apply()
            } else {
                Global.showAlert.value = false
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun fetchUserDataByEmail(email: String, onUserFetched: (UserEntity?) -> Unit) {
        MyDatabase.database.child("Admins").orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userSnapshot = snapshot.children.firstOrNull()
                    val user = userSnapshot?.getValue(UserEntity::class.java)
                    onUserFetched(user) // Call the trailing lambda with the fetched user
                }

                override fun onCancelled(error: DatabaseError) {
                    onUserFetched(null) // Handle error by passing null to the callback
                }
            })
    }

    private fun registerConnectivityListener(context: Context, userId: String) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(object :
            ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                writeUserOnlineStatus(userId)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                writeUserOfflineStatus(userId)
            }
        })
    }
}


