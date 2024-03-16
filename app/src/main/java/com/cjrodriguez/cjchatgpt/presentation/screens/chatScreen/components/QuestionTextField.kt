package com.cjrodriguez.cjchatgpt.presentation.screens.chatScreen.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupPositionProvider
import androidx.constraintlayout.compose.ConstraintLayout
import com.cjrodriguez.cjchatgpt.R
import com.cjrodriguez.cjchatgpt.R.string
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
@Preview
fun QuestionTextField(
    modifier: Modifier = Modifier,
    message: String = "",
    files: List<Uri> = listOf(),
    wordCount: Int = 0,
    upperLimit: Int = 500,
    errorMessage: String = "",
    isLoading: Boolean = false,
    shouldEnableTextField: Boolean = true,
    sendMessage: (List<String>) -> Unit = {},
    cancelMessageGeneration: () -> Unit = {},
    openVoiceRecordingSegment: () -> Unit = {},
    updateMessage: (String) -> Unit = {},
    clearTextAndRemoveFiles: () -> Unit = {},
    uploadFile: () -> Unit = {},
    removeFile: (Uri) -> Unit = {},
) {
    val toolTipState = rememberBasicTooltipState(isPersistent = false)
    val scope = rememberCoroutineScope()

    BasicTooltipBox(
        focusable = true,
        positionProvider = object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset {
                return IntOffset(
                    x = (windowSize.width - popupContentSize.width) / 2,
                    y = (windowSize.height - popupContentSize.height) / 2
                )
            }
        }, tooltip = {
            RichTooltip(
                title = { Text(text = "Create Image") },
            ) {
                Text(buildAnnotatedString {
                    append("To create an image type ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Black)) {
                        append("\"/Imagine\"")
                    }
                })
            }
        }, state = toolTipState
    ) {}
    Column(
        modifier = modifier
            .then(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

    ) {
        Row {
            files.forEach {
                FileContainer(uri = it, removeFile = { removeFile(it) })
            }
        }
        Row(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.extraLarge
                )
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = message,
                onValueChange = updateMessage,
                enabled = shouldEnableTextField,
                isError = errorMessage.isNotEmpty(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    errorBorderColor = Color.Transparent
                ),
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AnimatedVisibility(errorMessage.isNotEmpty()) {
                            Icon(
                                Icons.Filled.Error,
                                stringResource(R.string.error),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        AnimatedVisibility(visible = shouldEnableTextField) {
                            IconButton(
                                onClick = openVoiceRecordingSegment,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = stringResource(string.microphone)
                                )
                            }
                        }
                    }
                },
                leadingIcon = {
                    IconButton(
                        onClick = uploadFile
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = stringResource(string.upload_file)
                        )
                    }
                },
                modifier = Modifier
                    .heightIn(max = 200.dp)
                    .fillMaxWidth()
            )
        }

        if (!isLoading) {
            CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                if (errorMessage.isNotEmpty()) Text(
                    text = errorMessage,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.error
                )
                ConstraintLayout(
                    modifier = Modifier
                        .imePadding()
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    val (closeButton, noCount, sendButton, imageTipButton) = createRefs()
                    IconButton(
                        onClick = clearTextAndRemoveFiles, modifier = Modifier
                            .size(24.dp)
                            .constrainAs(closeButton) {
                                start.linkTo(parent.start)
                                top.linkTo(parent.top)
                            }) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.clear_text)
                        )
                    }

                    IconButton(onClick = {
                        sendMessage(
                            files.map { it.toString() }
                        )
                    },
                        enabled = errorMessage.isEmpty() && message.trim()
                            .isNotEmpty() && !isLoading,
                        modifier = Modifier
                            .size(24.dp)
                            .constrainAs(sendButton) {
                                end.linkTo(parent.end)
                                top.linkTo(parent.top)
                            }) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = stringResource(R.string.send)
                        )
                    }

                    IconButton(onClick = {
                        scope.launch {
                            toolTipState.show()
                        }
                    },
                        modifier = Modifier
                            .size(24.dp)
                            .constrainAs(imageTipButton) {
                                start.linkTo(closeButton.end, 16.dp)
                                top.linkTo(parent.top)
                            }) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = stringResource(R.string.send)
                        )
                    }

                    Text(
                        text = "$wordCount / $upperLimit",
                        modifier = Modifier.constrainAs(noCount) {
                            end.linkTo(sendButton.start, margin = 16.dp)
                            top.linkTo(parent.top)
                        })
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator()

                IconButton(onClick = cancelMessageGeneration) {
                    Icon(
                        imageVector = Icons.Default.Stop, tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "Stop Message Generation",
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
        }
    }

}