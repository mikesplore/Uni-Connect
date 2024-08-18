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
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mike.uniadmin.model.MyDatabase.getUpdate
import com.mike.uniadmin.model.Update
import com.mike.uniadmin.ui.theme.CommonComponents as CC
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckUpdate(context: Context) {
    var update by remember { mutableStateOf(false) }
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName
    var isDownloading by remember { mutableStateOf(false) }
    var downloadId by remember { mutableLongStateOf(-1L) }
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
    fun startDownload(context: Context, url: String, onProgress: (Int, Long) -> Unit) {
        val request = DownloadManager.Request(Uri.parse(url)).setTitle("UniKonnect Update")
            .setDescription("Downloading update")
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "UniKonnect.apk")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true).setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadid = downloadManager.enqueue(request)

        // Registering receiver for download complete
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadid) {
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

                            // Log for debugging
                            Log.d(
                                "DownloadManager",
                                "Downloaded: $bytesDownloaded, Total: $bytesTotal"
                            )

                            if (bytesTotal > 0) {
                                val progress = ((bytesDownloaded * 100) / bytesTotal).toInt()
                                onProgress(progress, downloadId)

                                // Update progress in UI
                                Log.d("DownloadProgress", "Progress: $progress%")

                                if (progress < 100) {
                                    progressHandler.postDelayed(this, 1000)
                                }
                            }
                        } else {
                            Log.e("DownloadManager", "Column index not found")
                        }
                    }
                }
                cursor?.close()
            }
        })
    }




    if (update) {
        BasicAlertDialog(
            onDismissRequest = {
                isDownloading = false
                update = false
            }, modifier = Modifier.background(
                Color.Transparent, RoundedCornerShape(10.dp)
            )
        ) {
            Column(
                modifier = Modifier
                    .background(CC.secondary(), RoundedCornerShape(10.dp))
                    .padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "New Update available!", style = CC.titleTextStyle(context).copy(
                        fontSize = 18.sp, fontWeight = FontWeight.Bold
                    ), modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    "A new version of this app is available. The update contains bug fixes and improvements.",
                    style = CC.descriptionTextStyle(context),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                if (isDownloading) {
                    Text(
                        "Downloading update...please wait",
                        style = CC.descriptionTextStyle(context),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LinearProgressIndicator(
                        color = CC.textColor(), trackColor = CC.extraColor1()
                    )
                }
                val downloadUrl = myUpdate.updateLink
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            if (!isDownloading && downloadUrl.isNotEmpty()) {
                                startDownload(context, url = downloadUrl) { progress, id ->
                                    downloadId = id
                                    isDownloading = progress < 100
                                }
                                isDownloading = true
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
                        Text("Update", style = CC.descriptionTextStyle(context))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { update = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                    ) {
                        Text("Cancel", color = CC.primary())
                    }
                }
            }
        }
    }

}