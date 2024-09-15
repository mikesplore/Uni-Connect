package com.mike.uniadmin

import android.Manifest
import android.content.Context
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
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.model.users.UserEntity
import com.mike.uniadmin.model.users.UserStateEntity
import com.mike.uniadmin.helperFunctions.MyDatabase
import com.mike.uniadmin.helperFunctions.MyDatabase.writeUserActivity
import com.mike.uniadmin.notification.createNotificationChannel
import com.mike.uniadmin.settings.BiometricPromptManager
import com.mike.uniadmin.ui.theme.UniAdminTheme
import com.mike.uniadmin.ui.theme.CommonComponents as CC


class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var currentUser: UserEntity = UserEntity()
    private val database = FirebaseDatabase.getInstance()
    val promptManager by lazy { BiometricPromptManager(this) }

    // Listener to track authentication state changes
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        if (user != null) {
            val userEmail = user.email ?: "" // Handle potential null email
            fetchUserDataByEmail(userEmail) { fetchedUser ->
                if (fetchedUser == null) {
                    Log.e("UniAdminMainActivity", "User not found for email: $userEmail")
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                    return@fetchUserDataByEmail
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

    // Lifecycle observer to track online/offline status
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
        CourseManager.initialize(this)
        createNotificationChannel(this)

        setTheme(R.style.Theme_UniAdmin)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()
        auth.addAuthStateListener(authStateListener)

        // Check and request notification permission
        checkAndRequestNotificationPermission()

        setContent {
            val systemUiController = rememberSystemUiController()
            if (UniAdminPreferences.darkMode.value) {
                systemUiController.setSystemBarsColor(
                    color = Color.Transparent ,
                    darkIcons = false
                )
            } else {
                systemUiController.setSystemBarsColor(
                    color = Color.Transparent,
                    darkIcons = true
                )
            }
            UniAdminTheme(dynamicColor = false, darkTheme = UniAdminPreferences.darkMode.value) {
                NavigationGraph(this, this)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        auth.removeAuthStateListener(authStateListener)
    }

    // Checks if notification permission is granted, updates preferences, and requests if needed
    fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isPermissionGranted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!isPermissionGranted) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                UniAdminPreferences.saveNotificationsPreference(false)
            } else {
                UniAdminPreferences.saveNotificationsPreference(true)
            }
        }
    }

    // Permission launcher for notification permission
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            UniAdminPreferences.saveNotificationsPreference(isGranted)
        }

    // Lifecycle observer setup
    private fun setupLifecycleObservers(userId: String) {
        lifecycle.addObserver(lifecycleObserver)
        registerConnectivityListener(this, userId)
    }

    // Writes user's online status to Firebase
    private fun writeUserOnlineStatus(userId: String) {
        val userStatusRef = database.getReference().child("Users Online Status").child(userId)
        val userState = UserStateEntity(
            userID = userId,
            id = "${userId}2024",
            online = "online",
            lastTime = CC.getTimeStamp(),
            lastDate = CC.getTimeStamp()
        )
        userStatusRef.setValue(userState)
        userStatusRef.onDisconnect().setValue(userState.copy(online = "offline"))

        writeUserActivity(userState) { success ->
            if (!success) {
                Log.e("MainActivity", "Failed to write user online status")
            }
        }
    }

    // Writes user's offline status to Firebase
    private fun writeUserOfflineStatus(userId: String) {
        val userStatusRef = database.getReference().child("Users Online Status").child(userId)
        val userState = UserStateEntity(
            userID = userId,
            id = "${userId}2024",
            online = "offline",
            lastTime = CC.getTimeStamp(),
            lastDate = CC.getTimeStamp()
        )
        userStatusRef.setValue(userState)

        writeUserActivity(userState) { success ->
            if (!success) {
                Log.e("MainActivity", "Failed to write user offline status")
            }
        }
    }

    // Fetches user data from Firebase by email
    private fun fetchUserDataByEmail(email: String, onUserFetched: (UserEntity?) -> Unit) {
        MyDatabase.database.child("Users").orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userSnapshot = snapshot.children.firstOrNull()
                    val user = userSnapshot?.getValue(UserEntity::class.java)
                    onUserFetched(user)
                }

                override fun onCancelled(error: DatabaseError) {
                    onUserFetched(null)
                }
            })
    }

    // Registers a network connectivity listener
    private fun registerConnectivityListener(context: Context, userId: String) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(object :
            ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                writeUserOnlineStatus(userId)
            }

            override fun onLost(network: Network) {
                writeUserOfflineStatus(userId)
            }
        })
    }
}



