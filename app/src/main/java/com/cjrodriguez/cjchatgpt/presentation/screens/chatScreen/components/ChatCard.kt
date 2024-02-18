package com.cjrodriguez.cjchatgpt.presentation.screens.chatScreen.components

import android.widget.TextView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import com.cjrodriguez.cjchatgpt.R
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
                MarkdownText(
                    markdown = chat.content,
                    modifier = Modifier.fillMaxWidth(),
                    style = TextStyle(color = MaterialTheme.colorScheme.onBackground)
                )

                Spacer(modifier = Modifier.height(8.dp))

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

@Composable
fun HtmlTextView(html: String, modifier: Modifier = Modifier, textColor: Int) {
    AndroidView(
        modifier = modifier.then(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ),
        factory = { context ->
            TextView(context).apply {
                this.setTextColor(textColor)
                this.textSize = 14f
            }
        },
        update = { it.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT) }
    )
}

@Composable
fun CodeTextView(
    html: String,
    modifier: Modifier = Modifier,
    textColor: Int,
    backgroundColor: Color
) {
    //val color = MaterialTheme.colorScheme.onBackground
    AndroidView(
        modifier = modifier.then(
            Modifier
                .fillMaxWidth()
                .background(color = backgroundColor, shape = CardDefaults.shape)
                .padding(8.dp)
        ),
        factory = { context ->
            TextView(context).apply {
                this.setTextColor(textColor)
                this.textSize = 14f
            }
        },
        update = { it.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT) }
    )
}