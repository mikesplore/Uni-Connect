package com.mike.uniadmin.courseResources

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.mike.uniadmin.model.GridItem
import com.mike.uniadmin.model.MyDatabase.deleteItem
import com.mike.uniadmin.model.MyDatabase.readItems
import com.mike.uniadmin.model.MyDatabase.writeItem
import com.mike.uniadmin.model.Section
import com.mike.uniadmin.ui.theme.CommonComponents as CC

object CourseName {
    var name: MutableState<String> = mutableStateOf("")
    var courseID: MutableState<String> = mutableStateOf("")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseResources(courseCode: String, context: Context) {
    val notes = remember { mutableStateListOf<GridItem>() }
    val pastPapers = remember { mutableStateListOf<GridItem>() }
    val resources = remember { mutableStateListOf<GridItem>() }
    var isLoading by remember { mutableStateOf(false) }
    var showAddSection by remember { mutableStateOf<Section?>(null) }

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
            TopAppBar(
                title = {}, colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CC.primary(), titleContentColor = CC.textColor()
                )
            )
        }, containerColor = CC.primary()
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
                Row(modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center) {
                    Text(
                        CourseName.name.value,
                        style = CC.titleTextStyle(context)
                            .copy(fontWeight = FontWeight.Bold, fontSize = 30.sp),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }

                Section(title = "Notes",
                    items = notes,
                    onAddClick = { showAddSection = Section.NOTES },
                    onDelete = { notes.remove(it); deleteItem(courseCode, Section.NOTES, it) },
                    context = context
                )
                AnimatedVisibility(showAddSection == Section.NOTES) {
                AddItemSection(context) { title, description, imageUrl, fileUrl ->
                        val newItem = GridItem(
                            title = title,
                            description = description,
                            fileLink = fileUrl,
                            imageLink = imageUrl
                        )
                        notes.add(newItem)
                        writeItem(courseCode, Section.NOTES, newItem)
                        showAddSection = null
                    }
                }
                Section(
                    title = "Past Papers",
                    items = pastPapers,
                    onAddClick = { showAddSection = Section.PAST_PAPERS },
                    onDelete = {
                        pastPapers.remove(it); deleteItem(
                        courseCode,
                        Section.PAST_PAPERS,
                        it
                    )
                    },
                    context = context
                )
                AnimatedVisibility(showAddSection == Section.PAST_PAPERS) {
                    AddItemSection(context) { title, description, fileUrl, imageUrl ->
                        val newItem = GridItem(
                            title = title,
                            description = description,
                            fileLink = fileUrl,
                            imageLink = imageUrl
                        )
                        pastPapers.add(newItem)
                        writeItem(courseCode, Section.PAST_PAPERS, newItem)
                        showAddSection = null
                    }
                }

                Section(
                    title = "Additional Resources",
                    items = resources,
                    onAddClick = { showAddSection = Section.RESOURCES },
                    onDelete = {
                        resources.remove(it); deleteItem(
                        courseCode,
                        Section.RESOURCES,
                        it
                    )
                    },
                    context = context
                )
                AnimatedVisibility(showAddSection == Section.RESOURCES) {
                AddItemSection(context) { title, description, imageUrl, fileUrl ->
                        val newItem = GridItem(
                            title = title,
                            description = description,
                            fileLink = fileUrl,
                            imageLink = imageUrl
                        )
                        resources.add(newItem)
                        writeItem(courseCode, Section.RESOURCES, newItem)
                        showAddSection = null
                    }
                }

                VideoCard("https://www.youtube.com/watch?v=uLDcOCZhZII", context)
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
    Text(
        text = title, style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(start = 15.dp)
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

    Button(
        onClick = onAddClick, colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF007BFF), contentColor = Color.White
        ), shape = RoundedCornerShape(10.dp), modifier = Modifier.padding(start = 15.dp)
    ) {
        Text("Add Item", style = CC.descriptionTextStyle(context = context))
    }

    Spacer(modifier = Modifier.height(20.dp))
}

@Composable
fun GridItemCard(item: GridItem, onDelete: (GridItem) -> Unit, context: Context) {
    val uriHandler = LocalUriHandler.current

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
                painter = rememberAsyncImagePainter(model = item.imageLink),
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
                onClick = { uriHandler.openUri(item.fileLink) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CC.extraColor2()),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Open", style = CC.descriptionTextStyle(context))
            }
            IconButton(
                onClick = { onDelete(item) }, modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red)
            }
        }
    }
}

@Composable
fun AddItemSection(context: Context, onAddItem: (String, String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var fileUrl by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .background(CC.extraColor2(), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text("Add New Item", style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold, fontSize = 20.sp))

        Spacer(modifier = Modifier.height(10.dp))

        InputDialogTextField(
            value = title, onValueChange = { title = it }, label = "Title", context = context
        )

        Spacer(modifier = Modifier.height(10.dp))

        InputDialogTextField(
            value = description,
            onValueChange = { description = it },
            label = "Description",
            context = context
        )

        Spacer(modifier = Modifier.height(10.dp))

        InputDialogTextField(
            value = fileUrl, onValueChange = { fileUrl = it }, label = "File URL", context = context
        )
        Spacer(modifier = Modifier.height(10.dp))
        InputDialogTextField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = "Image URL",
            context = context
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = {
                onAddItem(
                    title, description, imageUrl, fileUrl)
                     title = ""
                     description = ""
                    imageUrl = ""
                    fileUrl = ""

                             },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CC.extraColor1()
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Add", style = CC.descriptionTextStyle(context))
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CourseScreenPreview() {
    CourseResources(
        courseCode = "CP123456", context = LocalContext.current
    )
}

@Composable
fun InputDialogTextField(
    value: String, onValueChange: (String) -> Unit, label: String, context: Context

) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = CC.descriptionTextStyle(context)) },
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = CC.tertiary(),
            unfocusedIndicatorColor = CC.secondary(),
            focusedTextColor = CC.textColor(),
            unfocusedTextColor = CC.textColor(),
            focusedContainerColor = CC.secondary(),
            unfocusedContainerColor = CC.secondary()
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun VideoCard(link: String, context: Context) {
    var fullScreen by remember { mutableStateOf(false) }
    val myVideoId = extractVideoId(link)
    if (myVideoId == null) {
        Toast.makeText(context, "Invalid YouTube link", Toast.LENGTH_SHORT).show()
        return
    }
}

fun extractVideoId(youtubeLink: String): String? {
    val pattern =
        "(?<=watch\\?v=|/videos/|embed/|youtu.be/|/v/|/e/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#&?]*"
    val compiledPattern = Regex(pattern)
    val match = compiledPattern.find(youtubeLink)
    return match?.value
}