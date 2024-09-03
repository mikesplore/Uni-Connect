import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.tooling.preview.Preview
import com.mike.uniadmin.helperFunctions.randomColor
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@Composable
fun ScatteredCirclesBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CC.primary()) // Dark background color
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val circles = listOf(
                // Larger circles
                Pair(Offset(size.width * 0.2f, size.height * 0.3f), 180f),
                Pair(Offset(size.width * 0.7f, size.height * 0.1f), 160f),
                Pair(Offset(size.width * 0.5f, size.height * 0.8f), 200f),
                Pair(Offset(size.width * 0.85f, size.height * 0.5f), 170f),

                // Medium circles
                Pair(Offset(size.width * 0.3f, size.height * 0.6f), 120f),
                Pair(Offset(size.width * 0.6f, size.height * 0.3f), 110f),
                Pair(Offset(size.width * 0.1f, size.height * 0.8f), 130f),

                // Smaller circles
                Pair(Offset(size.width * 0.4f, size.height * 0.2f), 80f),
                Pair(Offset(size.width * 0.9f, size.height * 0.7f), 70f),
                Pair(Offset(size.width * 0.15f, size.height * 0.5f), 60f),
                Pair(Offset(size.width * 0.75f, size.height * 0.9f), 90f)
            )

            circles.forEach { (offset, radius) ->
                drawCircle(
                    color = randomColor.random().copy(alpha = 0.1f), // Blue color for the circles
                    radius = radius,
                    center = offset,
                    style = Fill
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ScatteredCirclesBackgroundPreview() {
    ScatteredCirclesBackground()
}
