package com.mike.uniadmin.moduleResources

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mike.uniadmin.UniConnectPreferences
import com.mike.uniadmin.helperFunctions.GridItem
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@Composable
fun InputDialogTextField(
    value: String, onValueChange: (String) -> Unit, label: String

) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                label,
                style = CC.descriptionTextStyle()
                    .copy(color = CC.textColor().copy(0.5f))
            )
        },
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = CC.tertiary(),
            unfocusedIndicatorColor = CC.secondary(),
            focusedTextColor = CC.textColor(),
            unfocusedTextColor = CC.textColor(),
            focusedContainerColor = CC.secondary(),
            unfocusedContainerColor = CC.secondary(),
            cursorColor = CC.textColor()
        ),
        modifier = Modifier.fillMaxWidth()
    )
}


@Composable
fun AddItemSection(
    context: Context,
    onAddItem: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var fileUrl by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }

    if (showErrorDialog) {
        AlertDialog(
            containerColor = CC.primary(),
            onDismissRequest = { showErrorDialog = false },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK", color = CC.primary())
                }
            },
            title = { Text("Invalid File URL") },
            text = { Text("The file URL must end with .pdf.") }
        )
    }
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = CC
                        .extraColor2()
                        .copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                )
                .fillMaxWidth(0.9f)
                .background(CC.extraColor1(), RoundedCornerShape(8.dp))
                .padding(20.dp)
                .imePadding()
        ) {
            Text(
                "Add New Item",
                style = CC.descriptionTextStyle().copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            InputDialogTextField(
                value = title,
                onValueChange = { title = it },
                label = "Title",
            )

            Spacer(modifier = Modifier.height(12.dp))

            InputDialogTextField(
                value = fileUrl,
                onValueChange = { fileUrl = it },
                label = "File URL",
            )

            Spacer(modifier = Modifier.height(12.dp))

            InputDialogTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = "Image URL",
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        if (title.isEmpty() || imageUrl.isEmpty() || fileUrl.isEmpty()) {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT)
                                .show()
                            return@Button
                        }

                        // Check if file URL ends with ".pdf"
                        if (!fileUrl.endsWith(".pdf", ignoreCase = true)) {
                            showErrorDialog = true
                            return@Button
                        }

                        // Add item and reset input fields
                        onAddItem(title, imageUrl, fileUrl)
                        title = ""
                        imageUrl = ""
                        fileUrl = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CC.secondary()
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        "Add",
                        style = CC.descriptionTextStyle().copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}


@Composable
fun Section(
    title: String,
    items: List<GridItem>,
    onAddClick: () -> Unit,
    onDelete: (GridItem) -> Unit,
    context: Context
) {
    val userType = UniConnectPreferences.userType.value
    Text(
        text = title,
        style = CC.titleTextStyle().copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
        modifier = Modifier.padding(start = 15.dp)
    )

    Spacer(modifier = Modifier.height(10.dp))

    if (items.isEmpty()) {
        Text(
            text = "No items available",
            style = CC.descriptionTextStyle(),
            modifier = Modifier.padding(start = 15.dp)
        )
    } else {
        LazyRow {
            items(items) { item ->
                GridItemCard(item = item, onDelete = onDelete, context = context)
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))
    if (userType == "admin") {
        Button(
            onClick = onAddClick, colors = ButtonDefaults.buttonColors(
                containerColor = CC.extraColor2(), contentColor = Color.White
            ), shape = RoundedCornerShape(10.dp), modifier = Modifier.padding(start = 15.dp)
        ) {
            Text("Add Item", style = CC.descriptionTextStyle())
        }
    }

    Spacer(modifier = Modifier.height(20.dp))
}