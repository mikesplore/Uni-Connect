package com.mike.uniadmin.attendance


import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mike.uniadmin.getModuleViewModel
import com.mike.uniadmin.ui.theme.CommonComponents as CC


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignAttendance(context: Context) {
    val moduleViewModel = getModuleViewModel(context)
    val modules by moduleViewModel.modules.observeAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabNames = modules?.map { it.moduleCode } ?: emptyList()
    val tabsLoading by moduleViewModel.isLoading.observeAsState()



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sign Attendance", style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp )) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CC.secondary()
                )
            )
        },
        containerColor = CC.primary()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            if (tabsLoading == true) {
                CC.ColorProgressIndicator(modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp))
            } else if (tabNames.isEmpty()) {
                Text("No modules found", style = CC.descriptionTextStyle(context))
            } else {
                Tabs(
                    context = context,
                    tabs = tabNames,
                    selectedTabIndex = selectedTabIndex,
                    onTabSelected = { index ->
                        selectedTabIndex = index

                    }
                )
            }
        }
    }

}


@Composable
fun Tabs(
    context: Context,
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    BoxWithConstraints {
        val screenWidth = maxWidth
        val boxWidth = screenWidth * 0.3f
        val density = LocalDensity.current
        val textSize = with(density) { (screenWidth * 0.03f).toSp() }

        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = CC.secondary()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    modifier = Modifier
                        .height(boxWidth * 0.35f)
                        .width(boxWidth)
                        .background(
                            if (selectedTabIndex == index) CC.primary() else CC.secondary(),
                            RoundedCornerShape(20.dp)
                        ),
                    selected = selectedTabIndex == index,
                    onClick = { onTabSelected(index) },
                    text = {
                        Text(
                            text = title,
                            style = CC.descriptionTextStyle(context).copy(
                                color = if (selectedTabIndex == index) CC.textColor() else CC.primary(),
                                fontSize = textSize
                            ),
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                )
            }
        }
    }
}