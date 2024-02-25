package com.cjrodriguez.cjchatgpt.presentation.screens.chatScreen.components

import android.util.Log
import android.widget.TextView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.AsyncImagePainter.State.Loading
import coil.compose.AsyncImagePainter.State.Success
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import com.cjrodriguez.cjchatgpt.R
import com.cjrodriguez.cjchatgpt.data.util.ERROR
import com.cjrodriguez.cjchatgpt.data.util.LOADING
import com.cjrodriguez.cjchatgpt.domain.model.Chat
import com.cjrodriguez.cjchatgpt.presentation.util.AiType.GPT3
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview
fun ChatCard(
    chat: Chat = Chat(
        topicId = "",
        isUserGenerated = false,
        content = "",
        imageUrl = "",
        aiType = GPT3
    ),
    onLongClick: () -> Unit = {},
    onCopyClick: () -> Unit = {},
    regenerateResponse: () -> Unit = {},
) {

    var isExpanded by remember { mutableStateOf(false) }
    val color =
        if (chat.isUserGenerated) MaterialTheme.colorScheme.surfaceVariant
        else MaterialTheme.colorScheme.secondaryContainer
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .combinedClickable(onLongClick = { isExpanded = true }, onClick = {})
    ) {

        Row(
            modifier = Modifier
                .background(color = color)
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (chat.isUserGenerated) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "",
                    Modifier
                        .align(Alignment.Top)
                        .size(36.dp)
                        .padding(end = 16.dp)
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ai),
                    contentDescription = "",
                    Modifier
                        .align(Alignment.Top)
                        .size(36.dp)
                        .padding(end = 16.dp)
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Log.e("expanded", chat.content)
                MarkdownText(
                    markdown = chat.content,
                    modifier = Modifier.fillMaxWidth(),
                    style = TextStyle(color = MaterialTheme.colorScheme.onBackground)
                )

                Spacer(modifier = Modifier.height(8.dp))

                when{
                    chat.imageUrl == LOADING -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .width(300.dp)
                                .height(300.dp)
                        )
                    }

                    chat.imageUrl == ERROR -> {
                        Image(painter = rememberVectorPainter(image = Icons.Default.Error),
                            contentDescription = "generated image")
                    }

                    chat.imageUrl != ""-> {
                        SubcomposeAsyncImage(
                            modifier = Modifier
                                .width(300.dp)
                                .height(300.dp)
                                .padding(bottom = 8.dp)
                                .clip(RoundedCornerShape(percent = 20)),
                            model = chat.imageUrl,
                            loading = {
                                CircularProgressIndicator()
                            },
                            error = {
                                Image(painter = rememberVectorPainter(image = Icons.Default.Error),
                                    contentDescription = "generated image")
                            },
                            contentDescription = "generated image")
                    }

                    else -> Unit
                }

//                if(chat.imageUrl != "") {
//
//                    SubcomposeAsyncImage(
//                        modifier = Modifier
//                            .width(300.dp)
//                            .height(300.dp)
//                            .padding(bottom = 8.dp)
//                            .clip(RoundedCornerShape(percent = 20)),
//                        model = chat.imageUrl,
//                        loading = {
//                            CircularProgressIndicator()
//                        },
//                        error = {
//                            Image(painter = rememberVectorPainter(image = Icons.Default.Error),
//                                contentDescription = "generated image")
//                        },
//                        contentDescription = "generated image")
//                }

                if (!chat.isUserGenerated) {
                    Text(
                        text = chat.aiType.displayName,
                        modifier = Modifier
                            .align(Alignment.End)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface,
                                shape = RoundedCornerShape(percent = 50)
                            )
                            .padding(8.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = isExpanded, onDismissRequest = { isExpanded = false },
                offset = DpOffset(x = 100.dp, y = 0.dp)
            ) {
                DropdownMenuItem(onClick = {
                    isExpanded = false
                    onCopyClick()
                }, text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .width(150.dp)
                            .padding(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CopyAll, contentDescription = "Copy")
                        Text(
                            text = "Copy",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                })
            }
        }
    }
}