package com.mike.uniadmin.moduleResources

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.mike.uniadmin.UniConnectPreferences
import java.io.File
import com.mike.uniadmin.ui.theme.CommonComponents as CC


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadedResources(context: Context, navController: NavController) {
    val files = remember { mutableStateOf(FileManager.getDownloadedFiles()) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            tint = CC.textColor()
                        )
                    }
                },
                title = {
                    Text(
                        "Downloaded Resources",
                        style = CC.titleTextStyle().copy(fontSize = 18.sp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CC.primary(), titleContentColor = CC.textColor()
                )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CC.primary())
                .padding(it)
        ) {
            if (files.value.isEmpty()) {
                Text(
                    text = "No files downloaded.",
                    style = CC.descriptionTextStyle(),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(files.value) { file ->
                        FileItemCard(file,
                            onFileClick = {
                                FileManager.openFile(context, file)
                            },
                            onDeleteClick = {
                                FileManager.deleteFile(file)
                                files.value =
                                    FileManager.getDownloadedFiles() // Refresh the file list
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun FileItemCard(file: File, onFileClick: () -> Unit, onDeleteClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 8.dp)
            .clickable { onFileClick() },
        shape = RoundedCornerShape(8.dp),
        color = CC.secondary(),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onFileClick) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = "PDF File",
                    tint = CC.textColor()
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.nameWithoutExtension,
                    style = CC.titleTextStyle().copy(fontSize = 18.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red)
            }
        }
    }
}


object FileManager {

    private fun getFilesDirectory(): File {
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        return File(
            downloadsDir,
            "Uni Connect/Module Resources/${UniConnectPreferences.moduleID.value}"
        )
    }

    fun getDownloadedFiles(): List<File> {
        val directory = getFilesDirectory()
        Log.d("FileManager", "Directory path: $directory")
        return if (directory.exists()) {
            directory.listFiles()?.filter { it.extension == "pdf" } ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun deleteFile(file: File) {
        if (file.exists()) {
            file.delete()
        }
    }

    fun openFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooserIntent = Intent.createChooser(intent, "Open with")
        context.startActivity(chooserIntent)
    }
}
