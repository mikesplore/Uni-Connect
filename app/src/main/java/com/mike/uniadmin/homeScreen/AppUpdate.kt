package com.mike.uniadmin.homeScreen

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mike.uniadmin.R
import com.mike.uniadmin.helperFunctions.MyDatabase.getUpdate
import com.mike.uniadmin.helperFunctions.Update
import kotlinx.coroutines.delay
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@Composable
fun CheckUpdate(context: Context) {
    var update by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableIntStateOf(0) }
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName
    var myUpdate by remember { mutableStateOf(Update()) }

    LaunchedEffect(Unit) {
        while (true) {
            getUpdate { localUpdate ->
                if (localUpdate != null) {
                    myUpdate = localUpdate
                    if (myUpdate.version != versionName) {
                        update = true
                    }
                }
            }
            delay(60000) // Wait for 60 seconds
        }
    }

    fun installApk(context: Context, uri: Uri) {
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(installIntent)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun startDownload(context: Context, url: String, onProgress: (Int) -> Unit) {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Uni Connect Update")
            .setDescription("Downloading update")
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "Uni Connect.apkV${myUpdate.version}"
            )
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        // Register receiver for download complete
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    context.unregisterReceiver(this)
                    val apkUri = downloadManager.getUriForDownloadedFile(id)
                    installApk(context, apkUri)
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_EXPORTED
            )
        }

        // Track progress
        val progressHandler = Handler(Looper.getMainLooper())
        progressHandler.post(object : Runnable {
            override fun run() {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor: Cursor? = downloadManager.query(query)

                cursor?.use {
                    if (it.moveToFirst()) {
                        val bytesDownloadedIndex =
                            it.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                        val bytesTotalIndex =
                            it.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

                        if (bytesDownloadedIndex != -1 && bytesTotalIndex != -1) {
                            val bytesDownloaded = it.getLong(bytesDownloadedIndex)
                            val bytesTotal = it.getLong(bytesTotalIndex)

                            if (bytesTotal > 0) {
                                downloadProgress = ((bytesDownloaded * 100) / bytesTotal).toInt()
                                onProgress(downloadProgress)

                                // Update progress in UI
                                if (downloadProgress < 100) {
                                    progressHandler.postDelayed(this, 1000)
                                } else {
                                    onProgress(100)
                                    isDownloading = false
                                }
                            }
                        }
                    }
                }
                cursor?.close()
            }
        })
    }

    if (update) {
        UpdateDialog(
            onDismiss = { update = false },
            context = context,
            versionName = myUpdate.version,
            startDownload = { url ->
                isDownloading = true
                startDownload(context, url) { progress ->
                    downloadProgress = progress
                }
            },
            downloadUrl = myUpdate.updateLink
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateDialog(
    onDismiss: () -> Unit,
    context: Context,
    versionName: String,
    startDownload: (String) -> Unit,
    downloadUrl: String,
) {
    BasicAlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        modifier = Modifier
            .padding(16.dp)
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .background(CC.secondary(), RoundedCornerShape(16.dp))
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Logo
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .padding(bottom = 16.dp)
            )

            // Title and Version
            Text(
                "New Update Available!",
                style = CC.titleTextStyle().copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                "Version $versionName",
                style = CC.descriptionTextStyle()
                    .copy(color = CC.textColor().copy(alpha = 0.7f)),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Description
            Text(
                "A new version will be downloaded in the background. An install prompt will appear shortly. If dismissed, find the update in your Downloads folder and install it manually",
                style = CC.descriptionTextStyle(),
                modifier = Modifier.padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        if (downloadUrl.isNotEmpty()) {
                            startDownload(downloadUrl)
                            onDismiss()  // Dismiss dialog after initiating download
                        } else {
                            Toast.makeText(
                                context,
                                "Download failed: Could not get download link",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = CC.primary())
                ) {
                    Text("Update", style = CC.descriptionTextStyle())
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = { onDismiss() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                ) {
                    Text("Cancel", color = CC.primary())
                }
            }
        }
    }
}