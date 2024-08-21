package com.mike.uniadmin

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mike.uniadmin.localDatabase.UniAdmin
import com.mike.uniadmin.backEnd.users.AccountDeletionEntity
import com.mike.uniadmin.backEnd.users.UserEntity
import com.mike.uniadmin.backEnd.users.UserViewModel
import com.mike.uniadmin.backEnd.users.UserViewModelFactory
import com.mike.uniadmin.model.randomColor
import kotlin.random.Random
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, context: Context) {
    val userAdmin = context.applicationContext as UniAdmin
    val userRepository = remember { userAdmin.userRepository }
    val userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory(userRepository))
    val signedInUser by userViewModel.signedInUser.observeAsState()
    var currentUser by remember { mutableStateOf<UserEntity?>(null) }
    var updated by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(signedInUser) {

        userViewModel.fetchUsers()
        userViewModel.getSignedInUser()

        if (signedInUser != null) {
            val email = signedInUser!!.email
            Log.d("ProfileScreen", "Finding user by email: $email")
            userViewModel.findUserByEmail(email) { fetchedUser ->
                Log.d("ProfileScreen", "Fetched User: $fetchedUser")
                currentUser = fetchedUser
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    if (isLoading || signedInUser == null || currentUser == null) {

        // Display a loading indicator while data is being fetched
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(CC.primary())
        ) {
            CircularProgressIndicator(color = CC.textColor())
        }
    } else {
        // Display the profile screen content
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
                    .background(CC.primary())
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
                DisplayImage(context, userViewModel, updated, onUpdateChange = { update ->
                    if (update) {
                        userViewModel.getSignedInUser()
                        userViewModel.fetchUsers()
                        Toast.makeText(
                            context,
                            "Updated!, please relaunch the screen",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    updated = !updated
                })
                Spacer(modifier = Modifier.height(20.dp))
                ProfileDetails(
                    context,
                    userViewModel,
                    updated,
                    onUpdateChange = { updated = !updated })
                Spacer(modifier = Modifier.height(50.dp))
                DangerZone(context, userViewModel)
            }
        }
    }
}


@Composable
fun DisplayImage(
    context: Context,
    viewModel: UserViewModel,
    updated: Boolean,
    onUpdateChange: (Boolean) -> Unit
) {
    val currentUser by viewModel.user.observeAsState()
    var showImageLinkBox by remember { mutableStateOf(false) }
    var link by remember { mutableStateOf("") }
    var imageClicked by remember { mutableStateOf(false) }

    val boxSize by animateDpAsState(
        targetValue = if (imageClicked) 160.dp else 100.dp, label = "", animationSpec = tween(200)
    )
    val size by animateDpAsState(
        targetValue = if (imageClicked) 150.dp else 70.dp, label = "", animationSpec = tween(200)
    )


    Column(
        modifier = Modifier.fillMaxWidth(0.9f),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .background(randomColor.random(), RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .height(boxSize)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(model = currentUser?.profileImageLink,
                contentDescription = "Profile Image",
                modifier = Modifier
                    .blur(30.dp)
                    .fillMaxSize(),
                contentScale = ContentScale.Crop,
                onError = { R.drawable.notification },
                onLoading = { R.drawable.logo })
            IconButton(
                onClick = {
                    imageClicked = !imageClicked
                },
                modifier = Modifier
                    .border(
                        1.dp, CC.textColor(), CircleShape
                    )
                    .clip(CircleShape)
                    .background(CC.secondary(), CircleShape)
                    .size(size),
            ) {
                if (currentUser?.profileImageLink?.isNotEmpty() == true) {
                    AsyncImage(model = currentUser?.profileImageLink,
                        contentDescription = "Profile Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onError = { R.drawable.notification },
                        onLoading = { R.drawable.logo }

                    )
                } else {
                    Text(
                        "${currentUser?.firstName?.get(0)}${currentUser?.lastName?.get(0)}",
                        style = CC.titleTextStyle(context)
                            .copy(fontWeight = FontWeight.Bold, fontSize = 40.sp),
                    )
                }
            }
        }

    }
    Spacer(modifier = Modifier.height(10.dp))
    Row(
        modifier = Modifier
            .height(50.dp)
            .fillMaxWidth(0.9f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CC.SingleLinedTextField(
            value = link,
            onValueChange = { link = it },
            label = "Image Link",
            modifier = Modifier.weight(1f),
            context = context,
            singleLine = true
        )
        Spacer(modifier = Modifier.width(5.dp))
        Button(
            onClick = {
                currentUser?.let {
                    viewModel.writeUser(it.copy(profileImageLink = link), onSuccess = {
                        viewModel.getSignedInUser()
                        showImageLinkBox = false
                        onUpdateChange(updated)
                    })
                }
            }, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(
                containerColor = CC.extraColor2(), disabledContainerColor = CC.tertiary()

            ), enabled = link.isNotEmpty()

        ) {
            Text("Save", style = CC.descriptionTextStyle(context).copy(fontSize = 13.sp))
        }
    }


}


@Composable
fun ProfileDetails(
    context: Context,
    viewModel: UserViewModel,
    updated: Boolean,
    onUpdateChange: (Boolean) -> Unit
) {
    val signedUser by viewModel.signedInUser.observeAsState()
    val currentUser by viewModel.user.observeAsState()
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getSignedInUser()
        signedUser?.let {
            it.email.let { email ->
                viewModel.findUserByEmail(email, onUserFetched = { fetchedUser ->
                    firstName = fetchedUser?.firstName.toString()
                    lastName = fetchedUser?.lastName.toString()
                    phoneNumber = fetchedUser?.phoneNumber.toString()

                })
            }
        }
    }

    fun saveUserData() {
        currentUser?.let { user ->
            viewModel.writeUser(
                user.copy(
                    firstName = firstName,
                    lastName = lastName,
                    phoneNumber = phoneNumber
                ), onSuccess = {
                    isEditing = false
                })
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
            IconButton(
                onClick = {
                    if (isEditing) {
                        saveUserData()
                    }
                    isEditing = !isEditing
                    onUpdateChange(updated)
                }, colors = IconButtonDefaults.iconButtonColors(
                    containerColor = CC.secondary(), contentColor = CC.textColor()

                )
            ) {
                Icon(
                    if (isEditing) Icons.Filled.Check else Icons.Default.Edit,
                    contentDescription = "save",
                    tint = CC.textColor()
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        currentUser?.let { user ->

            MyDetails(
                title = "First Name",
                value = firstName,
                onValueChange = { firstName = it },
                context = context,
                isEditing = isEditing
            )

            MyDetails(
                title = "Last Name",
                value = lastName,
                onValueChange = { lastName = it },
                context = context,
                isEditing = isEditing
            )

            MyDetails(
                title = "Email",
                value = user.email,
                onValueChange = {},
                context = context,
                isEditing = false, // Email is not editable
                fontSize = 15.sp
            )

            MyDetails(
                title = "Admission Number",
                value = user.id,
                onValueChange = {},
                context = context,
                isEditing = false, // Admission Number is not editable
                fontSize = 15.sp
            )

            MyDetails(
                title = "Phone Number",
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                context = context,
                isEditing = isEditing
            )
        }
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
            textStyle = CC.titleTextStyle(context).copy(
                fontSize = fontSize,
                color = if (isEditing) CC.textColor() else CC.textColor().copy(0.5f)
            ),
            onValueChange = onValueChange,
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = CC.tertiary(),
                focusedIndicatorColor = CC.tertiary(),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = CC.textColor(),
                unfocusedTextColor = CC.textColor(),
                disabledContainerColor = Color.Transparent,
                cursorColor = CC.textColor()
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
fun DangerZone(context: Context, viewModel: UserViewModel) {
    val accountStatus by viewModel.accountStatus.observeAsState()
    val currentUser by viewModel.user.observeAsState()

    LaunchedEffect(currentUser?.id) {
        Log.d("AccountDeletion", "Fetching account status for user: ${currentUser?.id}")
        currentUser?.id?.let { viewModel.checkAccountDeletionData(it) }
        Log.d("AccountDeletion", "Account Status: $accountStatus")
    }

    if (accountStatus?.status == "pending") {
        AccountDeletionRequests(context, accountStatus!!)
    } else {
        var showPuzzle by remember { mutableStateOf(false) }
        var puzzleWords by remember { mutableStateOf(generateRandomNonsenseWord()) }
        var userInput by remember { mutableStateOf("") }
        var showWarning by remember { mutableStateOf(false) }
        var deleteConfirmed by remember { mutableStateOf(false) }
        var loading by remember { mutableStateOf(false) }
        var isError by remember { mutableStateOf(false) }

        Spacer(modifier = Modifier.height(20.dp))
        Column(
            modifier = Modifier
                .imePadding()
                .fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                    showPuzzle = !showPuzzle
                },
                colors = ButtonDefaults.buttonColors(containerColor = CC.secondary()),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Solve a Puzzle before proceeding", style = CC.descriptionTextStyle(context))
            }

            if (showPuzzle) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Please enter the following code:",
                    style = CC.descriptionTextStyle(context).copy(textAlign = TextAlign.Center)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    puzzleWords, style = CC.titleTextStyle(context), color = CC.tertiary()
                )
                Spacer(modifier = Modifier.height(10.dp))
                TextField(
                    value = userInput, textStyle = CC.titleTextStyle(context).copy(
                        fontSize = 18.sp, color = if (isError) Color.Red else CC.textColor()
                    ), onValueChange = {
                        isError = false
                        userInput = it
                    }, isError = isError, colors = TextFieldDefaults.colors(
                        unfocusedIndicatorColor = CC.tertiary(),
                        focusedIndicatorColor = CC.tertiary(),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = CC.textColor(),
                        unfocusedTextColor = CC.textColor(),
                        errorIndicatorColor = Color.Red,
                        errorContainerColor = CC.primary(),
                        cursorColor = CC.textColor()
                    ), singleLine = true, modifier = Modifier
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
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CC.secondary()),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Proceed", style = CC.descriptionTextStyle(context))
                    }
                    Button(
                        onClick = { showPuzzle = false },
                        colors = ButtonDefaults.buttonColors(containerColor = CC.secondary()),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Abort", style = CC.descriptionTextStyle(context))
                    }
                }

                if (showWarning && !deleteConfirmed) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Important Notice: Account Deletion",
                            style = CC.descriptionTextStyle(context).copy(
                                fontWeight = FontWeight.Bold, color = Color.Red
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Deletion Timeline",
                            style = CC.descriptionTextStyle(context)
                                .copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Your account will be permanently deleted within 30 days. You will have full access until then.",
                            style = CC.descriptionTextStyle(context).copy(
                                textAlign = TextAlign.Center, color = Color.Red.copy(0.5f)
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Data Deletion",
                            style = CC.descriptionTextStyle(context)
                                .copy(fontWeight = FontWeight.SemiBold)
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
                            style = CC.descriptionTextStyle(context)
                                .copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "If you wish to reverse the deletion process, please contact our support team before the 30-day period expires.",
                            style = CC.descriptionTextStyle(context).copy(
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            deleteConfirmed = true
                            loading = true
                            val account = currentUser?.let {
                                AccountDeletionEntity(
                                    id = it.id,
                                    email = it.email,
                                    admissionNumber = it.id,
                                    date = CC.getTimeStamp(),
                                    status = "pending"
                                )
                            }
                            if (account != null) {
                                viewModel.writeAccountDeletionData(account, onSuccess = { success ->
                                    loading = false
                                    if (success) {
                                        Log.d(
                                            "ProfileScreen",
                                            "Account deletion data written successfully"
                                        )
                                        showWarning = false
                                        showPuzzle = false
                                    } else {
                                        Log.d(
                                            "ProfileScreen",
                                            "Failed to write account deletion data"
                                        )
                                        Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
                                    }
                                })
                            }

                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                color = CC.primary(), modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                "Send Account Deletion Request",
                                style = CC.descriptionTextStyle(context)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun AccountDeletionRequests(
    context: Context,
    accountStatus: AccountDeletionEntity,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Pending Account Deletion Request",
                style = CC.titleTextStyle(context = context),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "You have a pending account deletion request that was initiated on ${
                    CC.getCurrentDate(
                        accountStatus.date
                    )
                }. Your account will be permanently deleted after 30 days from this date.",
                style = CC.descriptionTextStyle(context)
            )
        }
    }
}


fun generateRandomNonsenseWord(length: Int = 6): String {
    val allowedChars =
        ('a'..'z') + ('A'..'Z') + ('0'..'9') + listOf('!', '@', '#', '$', '%', '^', '&', '*')
    return (1..length).map { allowedChars.random(Random) }.joinToString("")
}




