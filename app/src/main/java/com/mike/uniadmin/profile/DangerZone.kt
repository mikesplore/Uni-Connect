package com.mike.uniadmin.profile

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mike.uniadmin.backEnd.users.AccountDeletionEntity
import com.mike.uniadmin.backEnd.users.UserViewModel
import com.mike.uniadmin.ui.theme.CommonComponents as CC
import kotlin.random.Random

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
        AccountDeletionRequests(accountStatus!!)
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
                    style = CC.titleTextStyle().copy(color = Color.Red.copy(0.7f))
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row {
                Text("Delete Account", style = CC.descriptionTextStyle())
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    showPuzzle = !showPuzzle
                },
                colors = ButtonDefaults.buttonColors(containerColor = CC.secondary()),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Solve a Puzzle before proceeding", style = CC.descriptionTextStyle())
            }

            if (showPuzzle) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Please enter the following code:",
                    style = CC.descriptionTextStyle().copy(textAlign = TextAlign.Center)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    puzzleWords, style = CC.titleTextStyle(), color = CC.tertiary()
                )
                Spacer(modifier = Modifier.height(10.dp))
                TextField(
                    value = userInput, textStyle = CC.titleTextStyle().copy(
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
                        Text("Proceed", style = CC.descriptionTextStyle())
                    }
                    Button(
                        onClick = { showPuzzle = false },
                        colors = ButtonDefaults.buttonColors(containerColor = CC.secondary()),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Abort", style = CC.descriptionTextStyle())
                    }
                }

                if (showWarning && !deleteConfirmed) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Important Notice: Account Deletion",
                            style = CC.descriptionTextStyle().copy(
                                fontWeight = FontWeight.Bold, color = Color.Red
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Deletion Timeline",
                            style = CC.descriptionTextStyle()
                                .copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Your account will be permanently deleted within 30 days. You will have full access until then.",
                            style = CC.descriptionTextStyle().copy(
                                textAlign = TextAlign.Center, color = Color.Red.copy(0.5f)
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Data Deletion",
                            style = CC.descriptionTextStyle()
                                .copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Upon deletion, all associated data will be permanently erased, including profile information, user content, and settings.",
                            style = CC.descriptionTextStyle().copy(
                                textAlign = TextAlign.Center
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Account Reversal",
                            style = CC.descriptionTextStyle()
                                .copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "If you wish to reverse the deletion process, please contact our support team before the 30-day period expires.",
                            style = CC.descriptionTextStyle().copy(
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
                                style = CC.descriptionTextStyle()
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
                style = CC.titleTextStyle().copy(fontSize = 18.sp),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "You have a pending account deletion request that was initiated on ${
                    CC.getDateFromTimeStamp(
                        accountStatus.date
                    )
                }. Your account will be permanently deleted after 30 days from this date.",
                style = CC.descriptionTextStyle()
            )
        }
    }
}


fun generateRandomNonsenseWord(length: Int = 6): String {
    val allowedChars =
        ('a'..'z') + ('A'..'Z') + ('0'..'9') + listOf('!', '@', '#', '$', '%', '^', '&', '*')
    return (1..length).map { allowedChars.random(Random) }.joinToString("")
}


