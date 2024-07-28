package com.mike.uniadmin.ui.theme

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.BorderColor
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mike.uniadmin.model.MyDatabase
import com.mike.uniadmin.model.ScreenTime
import kotlinx.coroutines.delay
import com.mike.uniadmin.ui.theme.CommonComponents as CC

data class ColorScheme(
    val primaryColor: String,
    val secondaryColor: String,
    val tertiaryColor: String,
    val textColor: String,
    val extraColor1: String,
    val extraColor2: String
)

fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(if (hex.startsWith("#")) hex else "#$hex"))
    } catch (e: IllegalArgumentException) {
        Color.Unspecified
    }
}

object GlobalColors {
    private const val PREFS_NAME = "color_scheme_prefs"
    private const val THEME_MODE_KEY = "theme_mode"

    private val lightScheme = ColorScheme(
        primaryColor = "#E1F5FE", // Light Blue
        secondaryColor = "#B2EBF2", // Lighter Blue
        tertiaryColor = "#80DEEA", // Light Teal
        textColor = "#212121", // Dark Gray for Text
        extraColor1 = "#BBDEFB", // Light Blue Accent
        extraColor2 = "#00BCD4"  // Cyan
    )

    private val darkScheme = ColorScheme(
        primaryColor = "#15202B", // Deep Blue
        secondaryColor = "#1B4F72", // Dark Blue
        tertiaryColor = "#117A65", // Blue-Green Accent
        textColor = "#E1E8ED", // Light Gray for Text
        extraColor1 = "#1C2833", // Dark Gray
        extraColor2 = "#2980B9"  // Bright Blue
    )

    private var currentScheme by mutableStateOf(lightScheme)
    var isDarkMode by mutableStateOf(true)

    fun loadColorScheme(context: Context): ColorScheme {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isDark = sharedPreferences.getBoolean(THEME_MODE_KEY, true)
        isDarkMode = isDark
        currentScheme = if (isDark) darkScheme else lightScheme
        return currentScheme
    }

    fun saveColorScheme(context: Context, isDark: Boolean) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(THEME_MODE_KEY, isDark)
        editor.apply()
        isDarkMode = isDark
        currentScheme = if (isDark) darkScheme else lightScheme
    }

    val primaryColor: Color
        get() = parseColor(currentScheme.primaryColor)

    val secondaryColor: Color
        get() = parseColor(currentScheme.secondaryColor)

    val tertiaryColor: Color
        get() = parseColor(currentScheme.tertiaryColor)

    val textColor: Color
        get() = parseColor(currentScheme.textColor)

    val extraColor1: Color
        get() = parseColor(currentScheme.extraColor1)

    val extraColor2: Color
        get() = parseColor(currentScheme.extraColor2)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Appearance(navController: NavController, context: Context) {
    var isDarkMode by remember { mutableStateOf(GlobalColors.isDarkMode) }
    var currentFont by remember { mutableStateOf<FontFamily?>(null) }
    var fontUpdated by remember { mutableStateOf(false) }
    val startTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var timeSpent by remember { mutableLongStateOf(0L) }
    val screenID = "SC2"

    LaunchedEffect(Unit) {
        while (true) {
            timeSpent = System.currentTimeMillis() - startTime
            delay(1000) // Update every second (adjust as needed)
        }
    }

    DisposableEffect(Unit) {
        GlobalColors.loadColorScheme(context)
        onDispose {
            // Fetch the screen details
            MyDatabase.getScreenDetails(screenID) { screenDetails ->
                if (screenDetails != null) {
                    MyDatabase.writeScren(courseScreen = screenDetails) {}
                    // Fetch existing screen time
                    MyDatabase.getScreenTime(screenID) { existingScreenTime ->
                        val totalScreenTime = if (existingScreenTime != null) {
                            Log.d("Screen Time", "Retrieved Screen time: $existingScreenTime")
                            existingScreenTime.time + timeSpent
                        } else {
                            timeSpent
                        }

                        // Create a new ScreenTime object
                        val screentime = ScreenTime(
                            id = screenID,
                            screenName = screenDetails.screenName,
                            time = totalScreenTime
                        )

                        // Save the updated screen time
                        MyDatabase.saveScreenTime(screenTime = screentime, onSuccess = {
                            Log.d("Screen Time", "Saved $totalScreenTime to the database")
                        }, onFailure = {
                            Log.d("Screen Time", "Failed to save $totalScreenTime to the database")
                        })
                    }

                } else {
                    Log.d("Screen Time", "Screen details not found for ID: $screenID")
                }
            }
        }
    }
    // Load color scheme from SharedPreferences and update dark mode based on system settings
    LaunchedEffect(Unit) {
        GlobalColors.loadColorScheme(context)
        isDarkMode = GlobalColors.isDarkMode
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.ArrowBackIosNew, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CC.primary(),
                    titleContentColor = CC.textColor(),
                    navigationIconContentColor = CC.textColor(),
                )
            )
        },
        containerColor = CC.primary(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            CustomTextStyle(context = LocalContext.current) { selectedFont ->
                currentFont = selectedFont
                fontUpdated = !fontUpdated // Toggle the state to trigger recomposition
            }
        }

    }
}



@Composable
fun currentFontFamily(context: Context): FontFamily {
    val fontPrefs = remember { FontPreferences(context) }
    val selectedFontName = fontPrefs.getSelectedFont()

    return when (selectedFontName) {
        "Indie Flower" -> IndieFlower
        "Dancing Script" -> DancingScript
        "Shadow Into Light" -> ShadowIntoLight
        "Caveat" -> Caveat
        else -> FontFamily.Default // Use system font if no preference is saved
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextStyle(context: Context, onFontSelected: (FontFamily) -> Unit) {
    val fontPrefs = remember { FontPreferences(context) }
    var fontUpdated by remember { mutableStateOf(false) }
    var selectedFontFamily by remember { mutableStateOf<FontFamily?>(null) }
    val fontFamilies = mapOf(
        "Indie Flower" to IndieFlower,
        "Shadow Into Light" to ShadowIntoLight,
        "Dancing Script" to DancingScript,
        "Caveat" to Caveat,
        "System" to FontFamily.Default
    )

    // Load saved font preference on launch
    LaunchedEffect(fontUpdated) {
        val savedFont = fontPrefs.getSelectedFont()
        selectedFontFamily = fontFamilies[savedFont]

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(CC.primary())
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "Font Style",
                style = CC.titleTextStyle(context),
                fontSize = 40.sp,
            )
        }

        fontFamilies.forEach { (fontName, fontFamily) ->
            val isSelected = selectedFontFamily == fontFamily
            Row(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .border(
                        width = 1.dp,
                        color = CC.secondary(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = {
                        selectedFontFamily = fontFamily
                    },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = CC.secondary(),
                        unselectedColor = CC.textColor()
                    )
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = fontName,
                    fontFamily = fontFamily,
                    fontSize = 18.sp,
                    color = if (isSelected) CC.extraColor2() else CC.textColor()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Selected Font Preview:",
            style = CC.titleTextStyle(context),
            fontSize = 18.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 16.dp)
        )

        Row(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .border(
                    width = 1.dp,
                    color = CC.secondary(),
                    shape = RoundedCornerShape(10.dp)
                )
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Debugging the complex algorithm required a thorough review of every line of code.",
                fontFamily = selectedFontFamily,
                fontSize = 16.sp,
                color = CC.textColor(),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                fontPrefs.saveSelectedFont(fontFamilies.entries.find { it.value == selectedFontFamily }?.key)
                selectedFontFamily?.let { onFontSelected(it) }
                fontUpdated = !fontUpdated // Trigger recomposition in parent
                Toast.makeText(context, "Font updated", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(CC.secondary()),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 16.dp)
        ) {
            Text("Save", style = CC.descriptionTextStyle(context))
        }
    }
}






class FontPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("font_prefs", Context.MODE_PRIVATE)

    fun saveSelectedFont(fontName: String?) {
        prefs.edit().putString("selected_font", fontName).apply()
    }

    fun getSelectedFont(): String? {
        return prefs.getString("selected_font", null) // Default to null (system font)
    }

}

@Composable
fun Background(context: Context) {
    val icons = listOf(
        Icons.Outlined.Home,
        Icons.AutoMirrored.Outlined.Assignment,
        Icons.Outlined.School,
        Icons.Outlined.AccountCircle,
        Icons.Outlined.BorderColor,
        Icons.Outlined.Book,
    )
    LaunchedEffect(Unit) {
        GlobalColors.loadColorScheme(context)

    }
    // Calculate the number of repetitions needed to fill the screen
    val repetitions = 1000 // Adjust this value as needed
    val repeatedIcons = mutableListOf<ImageVector>()
    repeat(repetitions) {
        repeatedIcons.addAll(icons.shuffled())
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(10),
        modifier = Modifier
            .fillMaxSize()
            .background(CC.primary())
            .padding(10.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(repeatedIcons) { icon ->
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CC.textColor().copy(0.3f),
                modifier = Modifier
                    .padding(8.dp)
            )
        }
    }
}

@Preview
@Composable
fun ColorSettingsPreview() {
    val context = LocalContext.current
    GlobalColors.loadColorScheme(context)
    Appearance(rememberNavController(), context)
}
