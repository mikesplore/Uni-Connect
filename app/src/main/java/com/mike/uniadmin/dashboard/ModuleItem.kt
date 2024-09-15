package com.mike.uniadmin.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mike.uniadmin.model.modules.ModuleEntity
import com.mike.uniadmin.helperFunctions.randomColor
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@Composable
fun ModuleItem(module: ModuleEntity, navController: NavController) {
    BoxWithConstraints(
        contentAlignment = Alignment.Center, modifier = Modifier.padding(start = 10.dp)
    ) {
        val cardSize = minOf(maxWidth, maxHeight) * 0.6f // Adaptive size based on available space

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .border(
                        1.dp, randomColor.random(), CircleShape
                    )
                    .size(cardSize)
                    .clip(CircleShape)
                    .clickable {
                        navController.navigate("moduleContent/${module.moduleCode}")
                    }
                    .background(CC.tertiary(), CircleShape),
                elevation = CardDefaults.elevatedCardElevation(5.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (module.moduleImageLink.isNotEmpty()) {
                        AsyncImage(
                            model = module.moduleImageLink,
                            contentDescription = module.moduleName,
                            modifier = Modifier
                                .clip(CircleShape)
                                .fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.School, contentDescription = null, tint = CC.textColor()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(cardSize * 0.1f)) // Adaptive spacer size

            Text(
                text = module.moduleName.take(10) + if (module.moduleName.length > 10) "..." else "",
                style = CC.descriptionTextStyle().copy(fontSize = 13.sp),
                maxLines = 1
            )
        }
    }
}

@Composable
fun LoadingModuleItem() {
    BoxWithConstraints(
        contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)
    ) {
        val cardSize = minOf(maxWidth, maxHeight) * 0.6f // Adaptive size based on available space

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(cardSize)
                    .clip(CircleShape)
                    .background(CC.primary(), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CC.ColorProgressIndicator(modifier = Modifier.fillMaxSize())
            }

            Spacer(modifier = Modifier.height(cardSize * 0.1f)) // Adaptive spacer size

            Box(
                modifier = Modifier
                    .width(cardSize * 1.3f) // Adaptive width based on card size
                    .height(cardSize * 0.2f) // Adaptive height based on card size
                    .clip(RoundedCornerShape(10.dp))
                    .background(CC.primary())
            ) {
                CC.ColorProgressIndicator(modifier = Modifier.fillMaxSize())
            }
        }
    }
}


@Composable
fun ModuleItemList(modules: List<ModuleEntity>, navController: NavController) {
    BoxWithConstraints {
        // Calculate the adaptive item width
        val screenWidth = maxWidth
        val itemWidth = screenWidth * 0.28f // Each item takes 30% of the screen width

        // Set a minimum and maximum width for the items
        val adaptiveItemWidth = itemWidth.coerceIn(minimumValue = 100.dp, maximumValue = 150.dp)

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(modules) { module ->
                Box(
                    modifier = Modifier.width(adaptiveItemWidth) // Apply the adaptive width
                ) {
                    ModuleItem(module, navController)
                }
            }
        }
    }
}

