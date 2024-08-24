package com.mike.uniadmin.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.setValue
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
import com.mike.uniadmin.getUserViewModel
import com.mike.uniadmin.ui.theme.FontPreferences
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(navController: NavController, context: Context, mainActivity: MainActivity) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val fontPrefs = remember { FontPreferences(context) }
    var savedFont by remember { mutableStateOf("system") }

    val userViewModel = getUserViewModel(context)
    val currentUser by userViewModel.user.observeAsState()


    LaunchedEffect(savedFont) {
        savedFont = fontPrefs.getSelectedFont().toString()
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
                        style = CC.titleTextStyle(context),
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
                        style = CC.titleTextStyle(context).copy(fontSize = textSize * 0.8f)
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
                                    style = CC.titleTextStyle(context)
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
                                style = CC.descriptionTextStyle(context),
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                "Personal Info",
                                style = CC.descriptionTextStyle(context),
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
                        style = CC.titleTextStyle(context).copy(fontSize = textSize * 0.8f)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                DarkMode(context)
                Spacer(modifier = Modifier.height(20.dp))
                Notifications(context)
                Spacer(modifier = Modifier.height(40.dp))
                Row(modifier = Modifier.fillMaxWidth(0.9f)) {
                    Text(
                        "Security",
                        style = CC.titleTextStyle(context).copy(fontSize = textSize * 0.8f)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Biometrics(context, mainActivity)
                Spacer(modifier = Modifier.height(20.dp))
                PasswordUpdateSection(context)
                Spacer(modifier = Modifier.height(20.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Font Style", style = CC.titleTextStyle(context))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(50.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Selected Font: $savedFont",
                            style = CC.descriptionTextStyle(context).copy(fontSize = 20.sp)
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
                Row {
                    Text("We care about your feedback", style = CC.titleTextStyle(context))
                }
                currentUser?.let { it1 -> RatingAndFeedbackScreen(it1, context) }
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(0.9f)) {
                    Text("About", style = CC.titleTextStyle(context))
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
            "Uni Admin", style = CC.descriptionTextStyle(context).copy(
                fontWeight = FontWeight.Bold, fontSize = 20.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Version $versionName", style = CC.descriptionTextStyle(context))
        Text("Developed by Mike", style = CC.descriptionTextStyle(context))
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
        ) {

            //Phone Icon
            IconButton(onClick = {
                val intent = Intent(
                    Intent.ACTION_DIAL, Uri.parse("tel:+254799013845")
                )
                context.startActivity(intent)
            }, modifier = Modifier
                .background(CC.extraColor1(), CircleShape)
                .size(35.dp)) {
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
        Text("All rights reserved © 2024", style = CC.descriptionTextStyle(context))
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


