package com.cjrodriguez.cjchatgpt.presentation.screens.chatScreen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.cjrodriguez.cjchatgpt.R.string

@Composable
fun ImageFullScreenView(
    isExpanded: Boolean,
    imageUrl: String,
    toggleExpandedState: () -> Unit
) {
    val modifier = if (isExpanded) Modifier.fillMaxSize() else Modifier
        .width(300.dp)
        .height(300.dp)
        .padding(bottom = 8.dp)
        .clip(RoundedCornerShape(percent = 20))
    if (isExpanded) {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        val screenHeight = configuration.screenHeightDp.dp
        Box(
            modifier = modifier
        ) {
            ImageView(modifier, imageUrl, toggleExpandedState)
            IconButton(
                onClick = { /*TODO*/ },
                modifier = Modifier.align(Companion.BottomCenter)
            ) {
                Box(
                    modifier = Modifier.background(
                        color = MaterialTheme.colorScheme.onTertiary,
                        shape = CircleShape
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = stringResource(string.scroll_down)
                    )
                }
            }
        }
    } else {
        ImageView(
            modifier,
            imageUrl,
            toggleExpandedState
        )
    }
}

@Composable
private fun ImageView(
    modifier: Modifier,
    imageUrl: String,
    toggleExpandedState: () -> Unit
) {
    SubcomposeAsyncImage(
        modifier = modifier.clickable(onClick = toggleExpandedState),
        model = imageUrl,
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