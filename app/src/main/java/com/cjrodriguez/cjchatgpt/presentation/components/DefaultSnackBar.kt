package com.cjrodriguez.cjchatgpt.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DefaultSnackBar(
    snackBarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onDismiss: ()-> Unit,
    onAction: (()->Unit)? = null
){
    if (snackBarHostState.currentSnackbarData == null) { return }
    var size by remember { mutableStateOf(Size.Zero) }
    //val swipeableState = rememberSwipeableState(SwipeDirection.Initial)

//    val anchors = remember { DraggableAnchors { Start at -100.dp.toPx()
//        Center at 0f
//        End at 100.dp.toPx() } }
//    val state = remember { AnchoredDraggableState(initialValue = false) }
//    val density = LocalDensity.current
//    SideEffect {
//        state.updateAnchors(anchors)
//    }


    val width = remember(size) {
        if (size.width == 0f) {
            1f
        } else {
            size.width
        }
    }

//    if (swipeableState.isAnimationRunning) {
//        DisposableEffect(Unit) {
//            onDispose {
//                when (swipeableState.currentValue) {
//                    SwipeDirection.Right,
//                    SwipeDirection.Left -> {
//                        snackBarHostState.currentSnackbarData?.dismiss()
//                    }
//                    else -> {
//                        return@onDispose
//                    }
//                }
//            }
//        }
//    }

//    val offset = with(LocalDensity.current) {
//        swipeableState.offset.value.toDp()
//    }
    SnackbarHost(hostState = snackBarHostState,
        snackbar = {data->
            Snackbar(//modifier = Modifier.offset(x = offset).padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                action = {
                    data.visuals.actionLabel?.let { actionLabel->
                        TextButton(onClick = {
                            onDismiss()
                        }) {
                            Text(text = actionLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White)
                        }
                    }
                }
            ) {
                Text(text = data.visuals.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White)
            }
        }, modifier = modifier.onSizeChanged { size = Size(it.width.toFloat(), it.height.toFloat()) }
//            .swipeable(
//                state = swipeableState,
//                anchors = mapOf(
//                    -width to SwipeDirection.Left,
//                    0f to SwipeDirection.Initial,
//                    width to SwipeDirection.Right,
//                ),
//                thresholds = { _, _ -> FractionalThreshold(0.3f) },
//                orientation = Orientation.Horizontal
//            )
    )
}