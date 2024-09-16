package com.mike.uniadmin.settings

import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.storage.StorageManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.MainActivity
import com.mike.uniadmin.R
import com.mike.uniadmin.UniConnectPreferences
import com.mike.uniadmin.getUserViewModel
import java.io.IOException
import java.util.UUID
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(navController: NavController, context: Context, mainActivity: MainActivity) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val savedFont = remember { UniConnectPreferences.fontStyle.value }

    val userViewModel = getUserViewModel(context)
    val currentUser by userViewModel.user.observeAsState()


    LaunchedEffect(savedFont) {
        userViewModel.findUserByEmail(user?.email!!) {}

    }

    Scaffold(
        topBar = {
            TopAppBar(title = {}, navigationIcon = {
                IconButton(onClick = { navController.navigate("homeScreen") }) {
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
        BoxWithConstraints {
            val columnWidth = maxWidth
            val rowHeight = columnWidth * 0.15f
            val iconSize = columnWidth * 0.15f

            val density = LocalDensity.current
            val textSize = with(density) { (columnWidth * 0.07f).toSp() }


            Column(
                modifier = Modifier
                    .background(CC.primary())
                    .verticalScroll(rememberScrollState())
                    .padding(it)
                    .fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .height(rowHeight)
                        .fillMaxWidth(0.9f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Settings",
                        style = CC.titleTextStyle(),
                        fontSize = textSize * 1.2f,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        "Account",
                        style = CC.titleTextStyle().copy(fontSize = textSize * 0.8f)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                //Profile Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(80.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {

                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .border(
                                    1.dp, CC.textColor(), CircleShape
                                )
                                .clip(CircleShape)
                                .background(CC.secondary(), CircleShape)
                                .size(iconSize * 1.1f), contentAlignment = Alignment.Center
                        ) {
                            if (currentUser?.profileImageLink?.isNotEmpty() == true) {
                                AsyncImage(model = currentUser?.profileImageLink,
                                    contentDescription = "Profile Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    onError = { R.drawable.logo },
                                    onLoading = { R.drawable.logo }

                                )
                            } else {
                                Text(
                                    "${currentUser?.firstName?.get(0)}${currentUser?.lastName?.get(0)}",
                                    style = CC.titleTextStyle()
                                        .copy(fontWeight = FontWeight.Bold, fontSize = 40.sp),
                                )
                            }
                        }
                        Column(
                            modifier = Modifier.fillMaxHeight(0.9f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                currentUser?.firstName + " " + currentUser?.lastName,
                                style = CC.descriptionTextStyle(),
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                "Personal Info",
                                style = CC.descriptionTextStyle(),
                                color = CC.textColor().copy(0.8f)
                            )
                        }
                    }
                    MyIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowForwardIos, navController, "profile"
                    )

                }
                Spacer(modifier = Modifier.height(40.dp))
                Row(modifier = Modifier.fillMaxWidth(0.9f)) {
                    Text(
                        "System",
                        style = CC.titleTextStyle().copy(fontSize = textSize * 0.8f)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                DarkMode()
                Spacer(modifier = Modifier.height(20.dp))
                Notifications(context)
                Spacer(modifier = Modifier.height(40.dp))
                Row(modifier = Modifier.fillMaxWidth(0.9f)) {
                    Text(
                        "Security",
                        style = CC.titleTextStyle().copy(fontSize = textSize * 0.8f)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Biometrics(mainActivity)
                Spacer(modifier = Modifier.height(20.dp))
                PasswordUpdateSection(context)
                Spacer(modifier = Modifier.height(20.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Font Style", style = CC.titleTextStyle())

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(50.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Current Font: $savedFont",
                            style = CC.descriptionTextStyle().copy(fontSize = 17.sp)
                        )
                        IconButton(
                            onClick = { navController.navigate("appearance") },
                            modifier = Modifier
                                .size(iconSize * 0.5f)
                                .background(
                                    CC.secondary(), RoundedCornerShape(10.dp)
                                )
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = "Font Style",
                                tint = CC.textColor()
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                AppSize(context)
                Spacer(modifier = Modifier.height(20.dp))
                Row {
                    Text("We care about your feedback", style = CC.titleTextStyle())
                }
                currentUser?.let { it1 -> RatingAndFeedbackScreen(it1, context) }
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(0.9f)) {
                    Text("About", style = CC.titleTextStyle())
                }
                MyAbout(context)
            }
        }
    }
}


@Composable
fun MyIconButton(icon: ImageVector, navController: NavController, route: String) {
    BoxWithConstraints {
        val columnWidth = maxWidth
        val iconSize = columnWidth * 0.15f

        Box(modifier = Modifier
            .background(CC.secondary(), RoundedCornerShape(10.dp))
            .clickable { navController.navigate(route) }
            .size(iconSize * 0.8f)
            .clip(RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = CC.textColor())
        }
    }

}


@Composable
fun MyAbout(context: Context) {
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Uni Connect", style = CC.descriptionTextStyle().copy(
                fontWeight = FontWeight.Bold, fontSize = 20.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Version $versionName", style = CC.descriptionTextStyle())
        Text("Developed by Mike", style = CC.descriptionTextStyle())
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
        ) {

            //Phone Icon
            IconButton(
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_DIAL, Uri.parse("tel:+254799013845")
                    )
                    context.startActivity(intent)
                }, modifier = Modifier
                    .background(CC.extraColor1(), CircleShape)
                    .size(35.dp)
            ) {
                Icon(Icons.Default.Call, "Call", tint = CC.textColor())
            }
            Spacer(modifier = Modifier.width(10.dp))

            // GitHub Icon with Link
            IconButton(
                onClick = { uriHandler.openUri("https://github.com/mikesplore") },
                modifier = Modifier
                    .background(CC.extraColor1(), CircleShape)
                    .size(35.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.github),
                    tint = CC.textColor(),
                    contentDescription = "GitHub Profile",
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))

            // Google Icon with Link
            IconButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:") // Only email apps should handle this
                        putExtra(
                            Intent.EXTRA_EMAIL, arrayOf("mikepremium8@gmail.com")
                        ) // Recipients
                        putExtra(Intent.EXTRA_SUBJECT, "Email Subject")
                        putExtra(Intent.EXTRA_TEXT, "Email body text")
                    }
                    ContextCompat.startActivity(context, intent, null) // Start the activity
                }, modifier = Modifier
                    .background(CC.extraColor1(), CircleShape)
                    .size(35.dp)
            ) {
                Icon(
                    painter = painterResource(com.google.android.gms.base.R.drawable.googleg_standard_color_18),
                    tint = CC.textColor(),
                    contentDescription = "Open Gmail",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("All rights reserved © 2024", style = CC.descriptionTextStyle())
    }
}


@Composable
fun switchColors(): SwitchColors {
    return SwitchDefaults.colors(
        checkedThumbColor = CC.extraColor1(),
        uncheckedThumbColor = CC.extraColor2(),
        checkedTrackColor = CC.extraColor2(),
        uncheckedTrackColor = CC.extraColor1(),
        checkedIconColor = CC.textColor(),
        uncheckedIconColor = CC.textColor()
    )
}

@Composable
fun AppSize(context: Context) {
    var appSize by remember { mutableStateOf(getAppStorageSize(context)) }
    val userViewModel = getUserViewModel(context)

    var showDialog by remember { mutableStateOf(false) }
    var deleteDatabase by remember { mutableStateOf(false) }
    var deletePreferencesOption by remember { mutableStateOf(false) }
    var deleteCacheOption by remember { mutableStateOf(false) }

    // Function to clear data based on selected options
    fun clearSelectedData() {
        if (deleteDatabase) userViewModel.deleteAllTables()
        if (deletePreferencesOption) UniConnectPreferences.clearAllData()
        if (deleteCacheOption) clearCache(context)
        Toast.makeText(context, "Selected Data Cleared", Toast.LENGTH_SHORT).show()
        appSize = getAppStorageSize(context) // Refresh app size after clearing
    }

    // Convert app size to a human-readable format (MB)
    val appSizeInMb = appSize / (1024 * 1024 * 1024)/2

    // Layout
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CC.primary())
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App size display
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = "App Size",
                    tint = CC.textColor(),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Total App Size: $appSizeInMb MB",
                    style = CC.descriptionTextStyle(),
                    color = CC.textColor()
                )
            }

            // Divider
            HorizontalDivider(thickness = 1.dp, color = CC.secondary().copy(alpha = 0.2f))

            // Clear tables button with icon
            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    imageVector = Icons.Default.CleaningServices,
                    contentDescription = "Clear Database",
                    tint = CC.extraColor1()
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear App Cache and Database", style = CC.descriptionTextStyle())
            }
        }
    }

    // Confirmation Dialog
    if (showDialog) {
        AlertDialog(
            containerColor = CC.surfaceContainer(),
            onDismissRequest = { showDialog = false },
            title = { Text("Clear Data Confirmation", style = CC.titleTextStyle().copy(fontSize = 17.sp, fontWeight = FontWeight.Bold)) },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = deleteDatabase,
                            onCheckedChange = { deleteDatabase = it },
                            colors = CheckboxDefaults.colors(CC.secondary())
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear Database", style = CC.descriptionTextStyle())
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = deletePreferencesOption,
                            onCheckedChange = { deletePreferencesOption = it },
                            colors = CheckboxDefaults.colors(CC.secondary())
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear User Preferences", style = CC.descriptionTextStyle())
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = deleteCacheOption,
                            onCheckedChange = { deleteCacheOption = it },
                            colors = CheckboxDefaults.colors(CC.secondary())
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear App Cache", style = CC.descriptionTextStyle())
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clearSelectedData()
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(CC.secondary())
                ) {
                    Text("Confirm", style = CC.descriptionTextStyle())
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.buttonColors(CC.extraColor1())
                ) {
                    Text("Cancel", style = CC.descriptionTextStyle())
                }
            }
        )
    }
}


// Function to get app storage size
fun getAppStorageSize(context: Context): Long {
    val storageStatsManager = context.getSystemService(StorageStatsManager::class.java)
    val appSpecificInternalDirUuid: UUID = StorageManager.UUID_DEFAULT
    return try {
        storageStatsManager.getTotalBytes(appSpecificInternalDirUuid)
    } catch (e: IOException) {
        0L // Return 0 if an error occurs
    }
}

fun clearCache(context: Context) {
    val cacheDir = context.cacheDir
    if (cacheDir?.exists() == true) {
        cacheDir.deleteRecursively()
    }
}




