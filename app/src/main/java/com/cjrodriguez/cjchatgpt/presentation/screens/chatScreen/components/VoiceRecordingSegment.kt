package com.cjrodriguez.cjchatgpt.presentation.screens.chatScreen.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstrainScope
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.cjrodriguez.cjchatgpt.R.string
import com.cjrodriguez.cjchatgpt.presentation.util.RecordingState
import com.cjrodriguez.cjchatgpt.presentation.util.RecordingState.ERROR
import com.cjrodriguez.cjchatgpt.presentation.util.RecordingState.RECORDING
import kotlinx.coroutines.delay

@Preview
@Composable
fun VoiceRecordingSegment(
    modifier: Modifier = Modifier,
    recordingState: RecordingState = RECORDING,
    circlePowerLevel: Float = 140f,
    minimizePopUp: () -> Unit = {},
    setRecordingState: (Boolean) -> Unit = {},
    retryTranscription: () -> Unit = {},
    updatePowerLevel: () -> Unit = {},
    stopListening: () -> Unit = {}
) {
    val keyboardHeight = 300.dp

    var timeMillis by remember { mutableStateOf(0L) }
    val currentCircleColor = MaterialTheme.colorScheme.onPrimary

    val animatedRadius by animateFloatAsState(
        targetValue = circlePowerLevel, label = "",
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    LaunchedEffect(key1 = true) {
        while (true) {
            delay(1000L)
            timeMillis += 1000L
        }
    }

    val timeText = remember(timeMillis) { formatTime(timeMillis) }
    ConstraintLayout(
        modifier
            .height(keyboardHeight)
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp),
    ) {
        val (minimizeRef, errorColumnRefs, timerRef, recordingColumRefs, processingRefs) = createRefs()

        when (recordingState) {
            RECORDING -> {
                val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_RESUME, Lifecycle.Event.ON_START -> {
                                setRecordingState(true)
                                updatePowerLevel()
                            }

                            else -> {
                                setRecordingState(false)
                            }
                        }
                    }

                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }
                Text(
                    text = timeText,
                    fontSize = 16.sp,
                    modifier = Modifier.constrainAs(timerRef) {
                        start.linkTo(parent.start, 16.dp)
                    })

                Box(modifier = Modifier.constrainAs(recordingColumRefs) {
                    constraintToCenter()
                }) {
                    Canvas(
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.Center)
                    ) {
                        drawCircle(
                            color = currentCircleColor,
                            center = Offset(x = center.x, y = center.y - 25.dp.toPx()),
                            radius = animatedRadius
                        )

//                        Log.e("circle", "size ${circlePowerLevel}")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = stopListening) {
                            Icon(
                                imageVector = Icons.Rounded.Stop,
                                contentDescription = stringResource(string.stop_listening)
                            )
                        }

                        Text(
                            text = "Tap to stop recording",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                }
            }

            ERROR -> {
                IconButton(
                    onClick = minimizePopUp,
                    modifier = Modifier.constrainAs(minimizeRef) {
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                    }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(string.close)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.constrainAs(errorColumnRefs) {
                        constraintToCenter()
                    }
                ) {
                    Text(
                        text = "Network Error",
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = retryTranscription) {
                        Text(text = "Retry transcription")
                    }
                }
            }

            else -> {
                Row(modifier = Modifier.constrainAs(processingRefs) {
                    constraintToCenter()
                }) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 16.dp))
                    Text(text = stringResource(string.converting_to_text), fontSize = 16.sp)
                }
            }
        }
    }
}

private fun ConstrainScope.constraintToCenter() {
    start.linkTo(parent.start)
    end.linkTo(parent.end)
    top.linkTo(parent.top)
    bottom.linkTo(parent.bottom)
}

private fun formatTime(millis: Long): String {
    val hours = millis / 1000 / 3600
    val minutes = millis / 1000 % 3600 / 60
    val seconds = millis / 1000 % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}