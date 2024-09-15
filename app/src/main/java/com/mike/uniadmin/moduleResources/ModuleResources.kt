package com.mike.uniadmin.moduleResources

import ScatteredCirclesBackground
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mike.uniadmin.UniConnectPreferences
import com.mike.uniadmin.dashboard.isDeviceOnline
import com.mike.uniadmin.getModuleViewModel
import com.mike.uniadmin.helperFunctions.GridItem
import com.mike.uniadmin.helperFunctions.MyDatabase
import com.mike.uniadmin.helperFunctions.MyDatabase.deleteItem
import com.mike.uniadmin.helperFunctions.MyDatabase.readItems
import com.mike.uniadmin.helperFunctions.MyDatabase.writeItem
import com.mike.uniadmin.helperFunctions.Section
import com.mike.uniadmin.ui.theme.CommonComponents as CC


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleResources(moduleCode: String, context: Context, navController: NavController) {
    val notes = remember { mutableStateListOf<GridItem>() }
    val pastPapers = remember { mutableStateListOf<GridItem>() }
    val resources = remember { mutableStateListOf<GridItem>() }
    var isLoading by remember { mutableStateOf(true) }
    var showAddSection by remember { mutableStateOf<Section?>(null) }

    val moduleViewModel = getModuleViewModel(context)
    val module by moduleViewModel.fetchedModule.observeAsState()
    var refresh by remember { mutableStateOf(false) }


    LaunchedEffect(refresh) {
        isLoading = true
        moduleViewModel.getModuleDetailsByModuleID(moduleCode)

        readItems(moduleCode, Section.NOTES) { fetchedNotes ->
            notes.addAll(fetchedNotes)
        }
        readItems(moduleCode, Section.PAST_PAPERS) { fetchedPastPapers ->
            pastPapers.addAll(fetchedPastPapers)
        }
        readItems(moduleCode, Section.RESOURCES) { fetchedResources ->
            resources.addAll(fetchedResources)
        }
        isLoading = false

    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("homeScreen") }) {
                        Icon(
                            Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            tint = CC.textColor()
                        )
                    }
                },

                actions = {
                    TextButton(onClick = { showAddSection = null }) {
                        if (showAddSection != null) {
                            Text("Cancel", style = CC.descriptionTextStyle())
                        }
                    }
                    TextButton(onClick = { navController.navigate("downloads") }) {
                        Text("Downloads", style = CC.descriptionTextStyle())
                    }

                },
                colors = TopAppBarDefaults.topAppBarColors(
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
        } else if (!isDeviceOnline(context)) {
            Offline({ refresh = true }, navController)

        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CC.primary())
            ) {
                ScatteredCirclesBackground()

                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxSize()
                        .padding(it)
                        .imePadding(),
                    horizontalAlignment = Alignment.Start

                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .height(150.dp)
                                .fillMaxWidth(0.95f),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = module?.moduleImageLink,
                                contentDescription = "Module Image",
                                modifier = Modifier
                                    .blur(2.6.dp)
                                    .fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            val textBrush = Brush.horizontalGradient(
                                listOf(
                                    CC.extraColor2(),
                                    CC.textColor(),
                                    CC.extraColor1()

                                )
                            )
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .background(
                                        CC
                                            .primary()
                                            .copy(0.5f), RoundedCornerShape(10.dp)
                                    )
                            ) {
                                Text(
                                    UniConnectPreferences.moduleName.value,
                                    style = CC.titleTextStyle()
                                        .copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 30.sp,
                                            brush = textBrush
                                        ),
                                    modifier = Modifier
                                        .padding(10.dp),
                                    textAlign = TextAlign.Center
                                )
                            }

                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Section(
                        title = "Notes",
                        items = notes,
                        onAddClick = { showAddSection = Section.NOTES },
                        onDelete = { gridItem ->
                            notes.remove(gridItem); deleteItem(
                            moduleCode,
                            Section.NOTES,
                            gridItem
                        )
                        },
                        context = context
                    )
                    AnimatedVisibility(showAddSection == Section.NOTES) {
                        AddItemSection(context) { title, imageUrl, fileUrl ->
                            MyDatabase.generateGridItemID { id ->
                                val newItem = GridItem(
                                    title = title,
                                    id = id,
                                    fileLink = fileUrl,
                                    imageLink = imageUrl
                                )
                                notes.add(newItem)
                                writeItem(moduleCode, Section.NOTES, newItem)
                                showAddSection = null
                            }
                        }
                    }
                    Section(
                        title = "Past Papers",
                        items = pastPapers,
                        onAddClick = { showAddSection = Section.PAST_PAPERS },
                        onDelete = { gridItem ->
                            pastPapers.remove(gridItem); deleteItem(
                            moduleCode,
                            Section.PAST_PAPERS,
                            gridItem
                        )
                        },
                        context = context
                    )
                    AnimatedVisibility(showAddSection == Section.PAST_PAPERS) {
                        AddItemSection(context) { title, fileUrl, imageUrl ->
                            MyDatabase.generateGridItemID { id ->
                            val newItem = GridItem(
                                title = title,
                                id = id,
                                fileLink = fileUrl,
                                imageLink = imageUrl
                            )
                            pastPapers.add(newItem)
                            writeItem(moduleCode, Section.PAST_PAPERS, newItem)
                            showAddSection = null
                            }
                        }
                    }

                    Section(
                        title = "Additional Resources",
                        items = resources,
                        onAddClick = { showAddSection = Section.RESOURCES },
                        onDelete = { gridItem ->
                            resources.remove(gridItem); deleteItem(
                            moduleCode,
                            Section.RESOURCES,
                            gridItem
                        )
                        },
                        context = context
                    )
                    AnimatedVisibility(showAddSection == Section.RESOURCES) {
                        AddItemSection(context) { title, imageUrl, fileUrl ->
                            MyDatabase.generateGridItemID { id ->
                            val newItem = GridItem(
                                title = title,
                                id = id,
                                fileLink = fileUrl,
                                imageLink = imageUrl
                            )
                            resources.add(newItem)
                            writeItem(moduleCode, Section.RESOURCES, newItem)
                            showAddSection = null
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun Offline(onRefresh: () -> Unit, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.WifiOff, // Example icon
            contentDescription = "Offline",
            tint = Color.Yellow // Example color
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Oops! It seems you're offline.",
            style = CC.descriptionTextStyle().copy(textAlign = TextAlign.Center)
        )

        Text(
            "Connect to the internet to view this page or view your downloads.",
            style = CC.descriptionTextStyle().copy(textAlign = TextAlign.Center)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onRefresh()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = CC.extraColor2(), contentColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text("Refresh", style = CC.descriptionTextStyle())
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(onClick = { navController.navigate("downloads") }) {
            Text("View Downloads", style = CC.descriptionTextStyle())
        }
    }
}

