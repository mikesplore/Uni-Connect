package com.mike.uniadmin.profile

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mike.uniadmin.backEnd.users.UserViewModel
import com.mike.uniadmin.ui.theme.CommonComponents

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
                    containerColor = CommonComponents.secondary(), contentColor = CommonComponents.textColor()

                )
            ) {
                Icon(
                    if (isEditing) Icons.Filled.Check else Icons.Default.Edit,
                    contentDescription = "save",
                    tint = CommonComponents.textColor()
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