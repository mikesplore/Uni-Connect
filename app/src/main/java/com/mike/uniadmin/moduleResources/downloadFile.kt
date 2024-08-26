package com.mike.uniadmin.moduleResources

import android.content.Context
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import android.content.ContentValues
import android.provider.MediaStore
import android.os.Build
import android.net.Uri
import android.util.Log
import com.mike.uniadmin.UniAdminPreferences

suspend fun downloadPdfFile(context: Context, url: String, fileName: String): Uri? {
    val client = OkHttpClient()
    val request = Request.Builder().url(url).build()

    return withContext(Dispatchers.IO) {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Failed to download file: $response")

            response.body?.let { body ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.pdf")
                        put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/Campus Connect/Module Resources/${UniAdminPreferences.moduleID.value}")
                    }

                    val uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

                    uri?.let {
                        context.contentResolver.openOutputStream(it)?.use { outputStream ->
                            body.byteStream().copyTo(outputStream)
                        }
                        return@withContext uri
                    }
                } else {
                    val externalDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Campus Connect/Module Resources/${UniAdminPreferences.moduleID.value}")
                    if (!externalDir.exists()) {
                        externalDir.mkdirs()
                    }

                    val pdfFile = File(externalDir, "$fileName.pdf")
                    pdfFile.outputStream().use { outputStream ->
                        body.byteStream().copyTo(outputStream)
                    }

                    return@withContext Uri.fromFile(pdfFile)
                }
            }
        }
    }
}

