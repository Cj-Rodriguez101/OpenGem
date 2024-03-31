package com.cjrodriguez.cjchatgpt.presentation.screens.chatScreen.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.cjrodriguez.cjchatgpt.R
import com.cjrodriguez.cjchatgpt.domain.events.ChatListEvents
import com.cjrodriguez.cjchatgpt.presentation.util.AiType
import com.cjrodriguez.cjchatgpt.presentation.util.SpeakingState
import com.cjrodriguez.cjchatgpt.presentation.util.SpeakingState.ERROR
import com.cjrodriguez.cjchatgpt.presentation.util.SpeakingState.PROCESSING
import com.cjrodriguez.cjchatgpt.presentation.util.SpeakingState.RECORDING
import com.cjrodriguez.cjchatgpt.presentation.util.SpeakingState.SPEAKING
import kotlinx.coroutines.delay

@Preview
@Composable
fun VoiceChat(
    speakingState: SpeakingState = RECORDING,
    circlePowerLevel: Float = 0f,
    selectedAiType: AiType = AiType.GEMINI,
    onTriggerEvent: (ChatListEvents) -> Unit = {},
    closeScreen: () -> Unit = {},
) {
    val animatedFloat = remember { Animatable(100f) }
    val thinkingLottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.thinking))
    val speakingLottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.speaking))
    LaunchedEffect(Unit) {
        delay(200)
        animatedFloat.animateTo(
            targetValue = 250f, animationSpec = infiniteRepeatable(
                animation = tween(700, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    var timeMillis by remember { mutableStateOf(0L) }
    val circleColor = MaterialTheme.colorScheme.onBackground
    LaunchedEffect(key1 = true) {
        while (true) {
            delay(1000L)
            timeMillis += 1000L
        }
    }
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        val (canvasRef, actionButtonRef, switchRef, centerRef) = createRefs()
        val constrainButtonModifier = Modifier
            .constrainAs(actionButtonRef) {
                centerHorizontallyTo(parent)
                bottom.linkTo(parent.bottom, 32.dp)
            }
        AiTextSwitch(selectedAi = selectedAiType,
            changeSelectedItem = {
                onTriggerEvent(
                    ChatListEvents.SetGptVersion(it)
                )
            },
            modifier = Modifier.constrainAs(switchRef) {
                top.linkTo(parent.top, 16.dp)
                centerHorizontallyTo(parent)
            })
        when (speakingState) {
            RECORDING -> {
                Canvas(
                    modifier = Modifier
                        .constrainAs(canvasRef) {
                            top.linkTo(switchRef.bottom, 200.dp)
                            centerHorizontallyTo(parent)
                        }
                        .size(100.dp)
                ) {
                    drawCircle(
                        color = circleColor,
                        style = Stroke(width = 20f),
                        center = Offset(x = center.x, y = center.y - 25.dp.toPx()),
                        radius = animatedFloat.value
                    )
                }
            }

            SPEAKING -> {
                speakingLottieComposition?.let {
                    LottieAnimation(
                        modifier = Modifier.constrainAs(canvasRef) {
                            top.linkTo(switchRef.bottom, 200.dp)
                            centerHorizontallyTo(parent)
                        },
                        composition = it,
                        iterations = LottieConstants.IterateForever,
                    )
                } ?: Canvas(
                    modifier = Modifier
                        .constrainAs(canvasRef) {
                            top.linkTo(switchRef.bottom, 200.dp)
                            centerHorizontallyTo(parent)
                        }
                        .size(100.dp)
                ) {
                    drawCircle(
                        color = circleColor,
                        style = Stroke(width = 20f),
                        center = Offset(x = center.x, y = center.y - 25.dp.toPx()),
                        radius = animatedFloat.value
                    )
                }
            }

            PROCESSING -> {
                thinkingLottieComposition?.let {
                    LottieAnimation(
                        modifier = Modifier.constrainAs(centerRef) {
                            centerTo(parent)
                        },
                        composition = it,
                        iterations = LottieConstants.IterateForever,
                    )
                } ?: CircularProgressIndicator(
                    modifier = Modifier.constrainAs(centerRef) {
                        centerTo(parent)
                    }
                )

            }

            ERROR -> {
                Text(
                    text = "Unknown Error",
                    modifier = Modifier.constrainAs(centerRef) {
                        centerTo(parent)
                    })
            }
        }

        when (speakingState) {
            RECORDING -> {
                IconButton(
                    onClick = closeScreen,
                    modifier = constrainButtonModifier
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        tint = MaterialTheme.colorScheme.background,
                        contentDescription = "Close"
                    )
                }
            }

            SPEAKING -> {
                IconButton(
                    onClick = closeScreen,
                    modifier = constrainButtonModifier
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        tint = MaterialTheme.colorScheme.background,
                        contentDescription = "Stop"
                    )
                }
            }

            else -> Unit
        }
    }
}