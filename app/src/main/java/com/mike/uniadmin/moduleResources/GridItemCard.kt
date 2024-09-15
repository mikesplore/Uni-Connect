package com.mike.uniadmin.moduleResources

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mike.uniadmin.UniConnectPreferences
import com.mike.uniadmin.helperFunctions.GridItem
import com.mike.uniadmin.ui.theme.CommonComponents
import kotlinx.coroutines.launch
import java.io.IOException
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@Composable
fun GridItemCard(
    item: GridItem,
    onDelete: (GridItem) -> Unit,
    context: Context
) {
    val scope = rememberCoroutineScope()
    var isDownloading by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    var downloadCompleted by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .height(200.dp)
            .width(150.dp)
            .padding(8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { uriHandler.openUri(item.fileLink) },
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .background(CC.extraColor1())
                .fillMaxSize()
        ) {
            // Background Image
            AsyncImage(
                model = item.imageLink,
                contentDescription = "Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Row(
                modifier = Modifier
                    .padding(5.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                //Download Button
                IconButton(
                    onClick = {
                        scope.launch {
                            isDownloading = true
                            try {
                                downloadPdfFile(context, item.fileLink, item.title)

                            } catch (e: IOException) {
                                Toast.makeText(
                                    context,
                                    "Download failed: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            isDownloading = false
                            downloadCompleted = true
                        }
                    },
                    modifier = Modifier
                        .size(40.dp),
                    colors = IconButtonDefaults.iconButtonColors(containerColor = CC.extraColor2()),
                    enabled = !isDownloading
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(color = CC.primary(), strokeWidth =2.dp)
                    } else if (downloadCompleted) {
                        Icon(Icons.Default.Check, "Download complete", tint = CC.textColor())
                    } else {
                        Icon(
                            Icons.Default.Download, "download",
                            tint = CC.textColor()
                        )
                    }
                }

                //Delete Button
                if (UniConnectPreferences.userType.value == "admin") {
                    IconButton(
                        onClick = { onDelete(item) },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = CC.extraColor2()),
                        modifier = Modifier
                            .size(40.dp),
                    ) {
                        Icon(
                            Icons.Default.Delete, "delete",
                            tint = CC.textColor()
                        )
                    }
                }
            }

            // Title Container
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        color = CC
                            .primary()
                            .copy(alpha = 0.5f),
                        shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = item.title,
                    color = CC.textColor(),
                    style = CommonComponents.titleTextStyle()
                        .copy(fontWeight = FontWeight.Bold),
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


