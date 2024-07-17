package com.mike.uniadmin

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.model.MyDatabase.fetchUserDataByEmail
import com.mike.uniadmin.model.MyDatabase.generateAccountDeletionID
import com.mike.uniadmin.model.MyDatabase.writeAccountDeletionData
import com.mike.uniadmin.model.AccountDeletion
import com.mike.uniadmin.model.MyDatabase.fetchPreferences
import com.mike.uniadmin.model.MyDatabase.generateSharedPreferencesID
import com.mike.uniadmin.model.MyDatabase.writePreferences
import com.mike.uniadmin.model.MyDatabase.writeUsers
import kotlin.random.Random
import com.mike.uniadmin.model.User
import com.mike.uniadmin.model.UserPreferences
import com.mike.uniadmin.ui.theme.GlobalColors
import com.mike.uniadmin.CommonComponents as CC

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, context: Context) {
    val auth = FirebaseAuth.getInstance()
    var currentUser by remember { mutableStateOf(User()) }

    LaunchedEffect(auth.currentUser?.email) {
        auth.currentUser?.email?.let {
            fetchUserDataByEmail(it) { fetchedUser ->
                fetchedUser?.let {
                    currentUser = it
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {}, navigationIcon = {
                IconButton(onClick = { navController.navigate("settings") }) {
                    Icon(
                        Icons.Default.ArrowBackIosNew, "Back", tint = CC.textColor()
                    )
                }
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = CC.primary()
            )
            )
        }, containerColor = CC.primary()
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(it)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth(0.9f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Profile",
                    style = CC.titleTextStyle(context),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            DisplayImage(context)
            Spacer(modifier = Modifier.height(20.dp))
            ProfileDetails(navController, context)
            Spacer(modifier = Modifier.height(20.dp))
            GenderRow(context)
            Spacer(modifier = Modifier.height(50.dp))
            DangerZone(context)


        }
    }

}

@Composable
fun DisplayImage(context: Context) {
    val auth = FirebaseAuth.getInstance()
    var currentUser by remember { mutableStateOf(User()) }
    val user = auth.currentUser
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var signInMethod by remember { mutableStateOf("") }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }
    LaunchedEffect(key1 = Unit) {
        GlobalColors.loadColorScheme(context)
        if (user != null) {
            for (userInfo in user.providerData) {
                when (userInfo.providerId) {
                    "password" -> {
                        // User signed in with email and password
                        signInMethod = "password"
                        Log.d("Auth", "User signed in with email/password")
                    }

                    "google.com" -> {
                        // User signed in with Google
                        signInMethod = "google.com"
                        Log.d("Auth", "User signed in with Google")
                    }

                    "github.com" -> {
                        // User signed in with GitHub
                        signInMethod = "github.com"
                        Log.d("Auth", "User signed in with GitHub")
                    }
                }
            }
        }
    }

    LaunchedEffect(user?.email) {
        user?.email?.let {
            fetchUserDataByEmail(it) { fetchedUser ->
                fetchedUser?.let {
                    currentUser = it
                    fetchPreferences(currentUser.id) { preferences ->
                        preferences?.let {
                            selectedImageUri = Uri.parse(preferences.profileImageLink)
                        }
                    }

                }
            }
        }
    }

    Row(modifier = Modifier.fillMaxWidth(0.9f)) {
        Text("Profile Picture", style = CC.descriptionTextStyle(context))
    }

    Column(
        modifier = Modifier.fillMaxWidth(0.9f),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (user?.photoUrl != null && user.photoUrl.toString().isNotEmpty()) {
            AsyncImage(
                model = user.photoUrl,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else if (selectedImageUri != null && signInMethod == "password") {
            AsyncImage(
                model = selectedImageUri,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profile Picture",
                tint = Color.Gray, // Or your preferred color
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { launcher.launch("image/*") },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CC.secondary()
            ),
            enabled = selectedImageUri == null || signInMethod == "password"
        ) {
            Text("Upload Image", style = CC.descriptionTextStyle(context))
        }
    }

    // Store image URI in SharedPreferences when it changes
    LaunchedEffect(selectedImageUri) {
        selectedImageUri?.let {
            generateSharedPreferencesID { id ->
                val preferences = UserPreferences(
                    studentID = currentUser.id, id = id, profileImageLink = it.toString()
                )
                writePreferences(preferences) {
                    Log.d("Shared Preferences", "Image URI stored in SharedPreferences")
                }
            }
        }
    }
}


@Composable
fun ProfileDetails(navController: NavController, context: Context) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    var currentFirstName by remember { mutableStateOf("") }
    var currentLastName by remember { mutableStateOf("") }
    var currentPhone by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var currentGender by remember { mutableStateOf("") }
    var currentEmail by remember { mutableStateOf("") }
    var currentAdmissionNumber by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        fetchUserDataByEmail(user?.email!!) { fetchedUser ->
            fetchedUser?.let {
                currentEmail = it.email
                currentPhone = it.phoneNumber
                currentFirstName = it.firstName
                currentLastName = it.lastName
                currentGender = it.gender
                currentAdmissionNumber = it.id
                Log.d("Phone number:", currentPhone)
            }
        }
    }


    fun saveUserData() {
        writeUsers(
            user = User(
                id = currentAdmissionNumber,
                firstName = currentFirstName,
                lastName = currentLastName,
                phoneNumber = currentPhone,
                gender = currentGender,
                email = currentEmail,
            )
        ) {
            Toast.makeText(context, "Profile Updated", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(0.7f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = {
                if (isEditing) {
                    saveUserData()
                }
                isEditing = !isEditing
            }) {
                Icon(
                    if (isEditing) Icons.Filled.Check else Icons.Default.Edit,
                    contentDescription = "save",
                    tint = CC.textColor()
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        MyDetails(
            title = "First Name",
            value = currentFirstName,
            onValueChange = { currentFirstName = it },
            context = context,
            isEditing = isEditing
        )

        Spacer(modifier = Modifier.height(10.dp))

        MyDetails(
            title = "Last Name",
            value = currentLastName,
            onValueChange = { currentLastName = it },
            context = context,
            isEditing = isEditing
        )

        Spacer(modifier = Modifier.height(10.dp))

        MyDetails(
            title = "Email",
            value = currentEmail,
            onValueChange = {},
            context = context,
            isEditing = isEditing,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        MyDetails(
            title = "Phone Number",
            value = currentPhone,
            onValueChange = { currentPhone = it },
            context = context,
            isEditing = isEditing
        )
    }
}


@Composable
fun MyDetails(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    context: Context,
    fontSize: TextUnit = 18.sp,
    isEditing: Boolean
) {
    Column(
        modifier = Modifier
            .height(100.dp)  // Adjusted height to accommodate both the title and text field
            .fillMaxWidth()
    ) {
        Text(
            text = title,
            style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 4.dp)  // Padding to separate the title from the text field
        )

        TextField(
            value = value,
            textStyle = CC.titleTextStyle(context).copy(fontSize = fontSize),
            onValueChange = onValueChange,
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = CC.tertiary(),
                focusedIndicatorColor = CC.tertiary(),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = CC.textColor(),
                unfocusedTextColor = CC.textColor(),
                disabledContainerColor = Color.Transparent
            ),
            enabled = isEditing,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        )
    }
}


@Composable
fun GenderRow(context: Context) {
    var selectedMale by remember { mutableStateOf(false) }
    var selectedFemale by remember { mutableStateOf(false) }
    var currentUser by remember { mutableStateOf(User()) }
    var save by remember { mutableStateOf(false) }
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    var gender by remember { mutableStateOf(currentUser.gender ) }

    gender = if (!selectedFemale && !selectedMale) {
        "not set"
    } else if (!selectedFemale) {
        "Male"
    } else {
        "Female"
    }
    if (gender == "Male") {
        selectedMale = true
    } else {
        selectedFemale = true
    }

    LaunchedEffect(user?.email) {
        user?.email?.let { it ->
            fetchUserDataByEmail(it) { fetchedUser ->
                fetchedUser?.let {
                    currentUser = it
                    gender = currentUser.gender

                }
            }
        }

    }
    if (save) {
        writeUsers(user = User(
            id = currentUser.id,
            firstName = currentUser.firstName,
            lastName = currentUser.lastName,
            phoneNumber = currentUser.phoneNumber,
            gender = gender,
            email = currentUser.email,
        ), {})
    }
    Row(
        modifier = Modifier
            .height(70.dp)
            .fillMaxWidth(0.9f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Gender", style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.width(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(1f), horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = {
                    save = true
                    selectedMale = true
                    selectedFemale = false
                }, modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (selectedMale) CC.extraColor1() else CC.secondary(),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Male,
                    contentDescription = "Male",
                    tint = if (selectedMale) CC.extraColor2() else CC.extraColor1(),
                    modifier = Modifier.size(50.dp)
                )
            }
            Spacer(modifier = Modifier.width(100.dp))
            IconButton(
                onClick = {
                    save = true
                    selectedFemale = true
                    selectedMale = false
                }, modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (selectedFemale) CC.extraColor1() else CC.secondary(),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Female,
                    contentDescription = "Female",
                    tint = if (selectedFemale) CC.extraColor2() else CC.extraColor1(),
                    modifier = Modifier.size(50.dp)
                )
            }
        }
    }
}

@Composable
fun DangerZone(context: Context) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    var showPuzzle by remember { mutableStateOf(false) }
    var puzzleWords by remember { mutableStateOf(generateRandomNonsenseWord()) }
    var userInput by remember { mutableStateOf("") }
    var showWarning by remember { mutableStateOf(false) }
    var deleteConfirmed by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var currentAdmissionNumber by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var currentEmail by remember { mutableStateOf("") }
    LaunchedEffect(user?.email) {
        user?.email?.let {
            fetchUserDataByEmail(it) { fetchedUser ->
                fetchedUser?.let {
                    currentAdmissionNumber = it.id
                    currentEmail = it.email
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))
    Column(modifier = Modifier.fillMaxWidth(0.9f),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Row {
            Text(
                "Danger Zone",
                style = CC.titleTextStyle(context).copy(color = Color.Red.copy(0.7f))
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row {
            Text("Delete Account", style = CC.descriptionTextStyle(context))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = {
                showPuzzle = true
            }, colors = ButtonDefaults.buttonColors(
                containerColor = CC.secondary()
            ), shape = RoundedCornerShape(10.dp)
        ) {
            Text("Solve a Puzzle before proceeding", style = CC.descriptionTextStyle(context))
        }

        if (showPuzzle) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Please enter the following code to proceed with account deletion:",
                style = CC.descriptionTextStyle(context).copy(textAlign = TextAlign.Center)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                puzzleWords,
                style = CC.titleTextStyle(context),
                color = CC.tertiary()
            )
            Spacer(modifier = Modifier.height(10.dp))
            TextField(
                value = userInput,
                textStyle = CC.titleTextStyle(context).copy(
                    fontSize = 18.sp,
                    color = if (isError) Color.Red else CC.textColor()
                ),
                onValueChange = {
                    isError = false
                    userInput = it
                },
                isError = isError,
                colors = TextFieldDefaults.colors(
                    unfocusedIndicatorColor = CC.tertiary(),
                    focusedIndicatorColor = CC.tertiary(),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = CC.textColor(),
                    unfocusedTextColor = CC.textColor(),
                    errorIndicatorColor = Color.Red,
                    errorContainerColor = CC.primary()
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(60.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        if (userInput == puzzleWords) {
                            showWarning = true
                            puzzleWords = generateRandomNonsenseWord()
                            userInput = ""
                        } else {
                            isError = true
                            puzzleWords = generateRandomNonsenseWord()
                        }
                    }, colors = ButtonDefaults.buttonColors(
                        containerColor = CC.secondary()
                    ), shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Proceed", style = CC.descriptionTextStyle(context))
                }
                Button(
                    onClick = { showPuzzle = false }, colors = ButtonDefaults.buttonColors(
                        containerColor = CC.secondary()
                    ), shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Abort", style = CC.descriptionTextStyle(context))
                }
            }
            if (showWarning) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Important Notice: Account Deletion",
                        style = CC.descriptionTextStyle(context).copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Deletion Timeline",
                        style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "Your account will be permanently deleted within 3 days. You will have full access until then.",
                        style = CC.descriptionTextStyle(context).copy(
                            textAlign = TextAlign.Center,
                            color = Color.Red.copy(0.5f)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Data Deletion",
                        style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "Upon deletion, all associated data will be permanently erased, including profile information, user content, and settings.",
                        style = CC.descriptionTextStyle(context).copy(
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Account Reversal",
                        style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "If you wish to reverse the deletion process, please contact our support team before the 3-day period expires.",
                        style = CC.descriptionTextStyle(context).copy(
                            textAlign = TextAlign.Center
                        )
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        deleteConfirmed = true
                    }, colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    ), shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            color = CC.primary(),
                            modifier = Modifier.size(24.dp)
                        )}
                    else{
                        Text(
                            "Send Account Deletion Request",
                            style = CC.descriptionTextStyle(context)
                        )
                    }
                }
            }
        }

        if (deleteConfirmed) {
            loading = true
            generateAccountDeletionID { id ->
                val account = AccountDeletion(
                    id = id, admissionNumber = currentAdmissionNumber, email = currentEmail

                )
                writeAccountDeletionData(account) {
                    loading = false
                    showWarning = false
                    showPuzzle = false
                    Log.d("Account Deletion Request", "Request sent Successfully!")
                }
            }
        }
    }
}

fun generateRandomNonsenseWord(length: Int = 6): String {
    val allowedChars =
        ('a'..'z') + ('A'..'Z') + ('0'..'9') + listOf('!', '@', '#', '$', '%', '^', '&', '*')
    return (1..length).map { allowedChars.random(Random) }.joinToString("")
}




