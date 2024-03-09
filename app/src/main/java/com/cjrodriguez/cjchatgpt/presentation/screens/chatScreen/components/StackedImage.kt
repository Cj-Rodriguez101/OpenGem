package com.cjrodriguez.cjchatgpt.presentation.screens.chatScreen.components

import androidx.annotation.FloatRange
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.cjrodriguez.cjchatgpt.R.string

@Composable
fun StackedImages(
    imageUrls: List<String>,
    setSelectedImage: (String) -> Unit = {}
) {
    OverlappingRow(
        modifier = Modifier.wrapContentSize()
    ) {
        imageUrls.forEach { url ->
            SubcomposeAsyncImage(
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 8.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable(onClick = {
                        setSelectedImage(url)
                    }),
                contentScale = ContentScale.Crop,
                model = url,
                loading = {
                    CircularProgressIndicator()
                },
                error = {
                    Image(
                        painter = rememberVectorPainter(image = Icons.Default.Error),
                        contentDescription = stringResource(string.generated_image)
                    )
                },
                contentDescription = stringResource(string.generated_image)
            )
        }
    }
}

//https://proandroiddev.com/custom-layouts-with-jetpack-compose-bc1bdf10f5fd
@Composable
fun OverlappingRow(
    modifier: Modifier = Modifier,
    @FloatRange(from = 0.1, to = 1.0) overlapFactor: Float = 0.5f,
    content: @Composable () -> Unit,
) {
    val measurePolicy = overlappingRowMeasurePolicy(overlapFactor)
    Layout(
        measurePolicy = measurePolicy,
        content = content,
        modifier = modifier
    )
}

fun overlappingRowMeasurePolicy(overlapFactor: Float) = MeasurePolicy { measurables, constraints ->
    val placeables = measurables.map { measurable -> measurable.measure(constraints) }
    val height = placeables.maxOf { it.height }
    val width = (placeables.subList(1, placeables.size)
        .sumOf { it.width } * overlapFactor + placeables[0].width).toInt()
    layout(width, height) {
        var xPos = 0
        for (placeable in placeables) {
            placeable.placeRelative(xPos, 0, 0f)
            xPos += (placeable.width * overlapFactor).toInt()
        }
    }
}
