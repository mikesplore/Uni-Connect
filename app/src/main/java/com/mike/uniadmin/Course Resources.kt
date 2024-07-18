package com.mike.uniadmin

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.rememberAsyncImagePainter
import java.util.Locale
import com.mike.uniadmin.model.GridItem
import com.mike.uniadmin.model.MyDatabase.deleteItem
import com.mike.uniadmin.model.MyDatabase.readItems
import com.mike.uniadmin.model.MyDatabase.writeItem
import com.mike.uniadmin.model.Section
import com.mike.uniadmin.CommonComponents as CC

object CourseName {
    var name: MutableState<String> = mutableStateOf("")
    var courseID: MutableState<String> = mutableStateOf("")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseScreen(courseCode: String, context: Context) {
    val notes = remember { mutableStateListOf<GridItem>() }
    val pastPapers = remember { mutableStateListOf<GridItem>() }
    val resources = remember { mutableStateListOf<GridItem>() }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var addItemToSection by remember { mutableStateOf<Section?>(null) }

    LaunchedEffect(courseCode) {
        isLoading = true
        readItems(courseCode, Section.NOTES) { fetchedNotes ->
            notes.addAll(fetchedNotes)
            isLoading = false
        }
        readItems(courseCode, Section.PAST_PAPERS) { fetchedPastPapers ->
            pastPapers.addAll(fetchedPastPapers)
            isLoading = false
        }
        readItems(courseCode, Section.RESOURCES) { fetchedResources ->
            resources.addAll(fetchedResources)
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(CourseName.name.value, style = CC.titleTextStyle(context), fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CC.primary(),
                    titleContentColor = CC.textColor())
            )
        },
        containerColor = CC.primary()
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CC.primary()),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CC.textColor())
            }
        } else {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
                    .background(CC.primary())
                    .padding(it)
            ) {
                Section(
                    title = "Notes",
                    items = notes,
                    onAddClick = { addItemToSection = Section.NOTES; showAddDialog = true },
                    onDelete = { notes.remove(it); deleteItem(courseCode, Section.NOTES, it) },
                    context = context
                )
                Section(
                    title = "Past Papers",
                    items = pastPapers,
                    onAddClick = { addItemToSection = Section.PAST_PAPERS; showAddDialog = true },
                    onDelete = { pastPapers.remove(it); deleteItem(courseCode, Section.PAST_PAPERS, it) },
                    context = context
                )
                Section(
                    title = "Additional Resources",
                    items = resources,
                    onAddClick = { addItemToSection = Section.RESOURCES; showAddDialog = true },
                    onDelete = { resources.remove(it); deleteItem(courseCode, Section.RESOURCES, it) },
                    context = context
                )
            }
        }
    }

    if (showAddDialog) {
        AddItemDialog(
            onDismiss = { showAddDialog = false },
            onAddItem = { title, description, fileType, link ->
                val newItem = GridItem(title = title, description = description, link = link, fileType = fileType)
                when (addItemToSection) {
                    Section.NOTES -> {
                        notes.add(newItem)
                        writeItem(courseCode, Section.NOTES, newItem)
                    }
                    Section.PAST_PAPERS -> {
                        pastPapers.add(newItem)
                        writeItem(courseCode, Section.PAST_PAPERS, newItem)
                    }
                    Section.RESOURCES -> {
                        resources.add(newItem)
                        writeItem(courseCode, Section.RESOURCES, newItem)
                    }
                    null -> { /* Do nothing */ }
                }
                showAddDialog = false
            },
            context
        )
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
    Text(
        text = title,
        style = CC.titleTextStyle(context),
        modifier = Modifier.padding(start = 15.dp)
    )

    Spacer(modifier = Modifier.height(10.dp))

    if (items.isEmpty()) {
        Text(
            text = "No items available",
            style = CC.descriptionTextStyle(context),
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

    Button(onClick = onAddClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF007BFF),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.padding(start = 15.dp)
    ) {
        Text("Add Item", style = CC.descriptionTextStyle(context = context))
    }

    Spacer(modifier = Modifier.height(20.dp))
}

@Composable
fun GridItemCard(item: GridItem, onDelete: (GridItem) -> Unit, context: Context) {
    val uriHandler = LocalUriHandler.current
    val thumbnail = when (item.fileType) {
        "pdf" -> R.drawable.pdf
        "word" -> R.drawable.word
        "excel" -> R.drawable.excel
        else -> item.thumbnail // Assuming thumbnail is a URL for image file types
    }

    Surface(
        modifier = Modifier
            .width(200.dp)
            .padding(start = 15.dp),
        shape = RoundedCornerShape(8.dp),
        color = CC.secondary(),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = thumbnail),
                contentDescription = item.title,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color.LightGray, RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = item.title,
                style = CC.titleTextStyle(context),
                fontSize = 20.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.description,
                style = CC.descriptionTextStyle(context),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = { uriHandler.openUri(item.link) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF)),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Open", style = CC.descriptionTextStyle(context))
            }
            IconButton(
                onClick = { onDelete(item) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(onDismiss: () -> Unit, onAddItem: (String, String, String, String) -> Unit, context: Context) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    var fileType by remember { mutableStateOf("image") } // Default to image type

    val fileTypes = listOf("image", "pdf", "word", "excel")

    BasicAlertDialog(onDismissRequest = { onDismiss() }) {
        Column(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = Color.Gray,
                    shape = RoundedCornerShape(10.dp)
                )
                .background(CC.primary(), RoundedCornerShape(10.dp))
                .width(270.dp)
        ) {
            Row(modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Add New Item", style = CC.titleTextStyle(context))
            }
            Column(
                modifier = Modifier.padding(start = 5.dp, end = 5.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                InputDialogTextField(title, { title = it }, "Title", context)
                Spacer(modifier = Modifier.height(10.dp))
                InputDialogTextField(description, { description = it }, "Description", context)
                Spacer(modifier = Modifier.height(10.dp))
                InputDialogTextField(link, { link = it }, "Link", context)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Select File Type:",
                style = CC.descriptionTextStyle(context),
                modifier = Modifier.padding(start = 10.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                fileTypes.forEach { type ->
                    Box(
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = Color.Gray,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .size(50.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (fileType == type) CC.secondary() else CC.primary())
                            .clickable { fileType = type },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(type.capitalize(Locale.ROOT), style = CC.descriptionTextStyle(context), fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CC.primary()
                    )
                ) {
                    Text("Cancel", style = CC.descriptionTextStyle(context))
                }
                Button(onClick = {
                    if (title.isNotEmpty() && description.isNotEmpty() && link.isNotEmpty()) {
                        onAddItem(title, description, fileType, link)
                    } else {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    }
                },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CC.primary()
                    )) {
                    Text("Add", style = CC.descriptionTextStyle(context))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

    }
}

@Preview(showBackground = true)
@Composable
fun CourseScreenPreview() {
    CourseScreen(
        courseCode = "CP123456",
        context = LocalContext.current
    )
}
@Composable
fun InputDialogTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    context: Context

){
    TextField(value = value,
        onValueChange =  onValueChange,
        label = { Text(label, style = CC.descriptionTextStyle(context)) },
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = CC.tertiary(),
            unfocusedIndicatorColor = CC.secondary(),
            focusedTextColor = CC.textColor(),
            unfocusedTextColor = CC.textColor(),
            focusedContainerColor = CC.secondary(),
            unfocusedContainerColor = CC.secondary()
        )
    )
}