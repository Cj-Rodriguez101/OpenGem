package com.cjrodriguez.cjchatgpt.presentation.screens.topicScreen.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cjrodriguez.cjchatgpt.R
import com.cjrodriguez.cjchatgpt.domain.model.Topic

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview
fun TopicCard(
    topic: Topic = Topic("", ""),
    onSelectTopic: (String) -> Unit = {},
    deleteTopic: () -> Unit = {},
    renameTopic: () -> Unit = {}
) {

    val color = MaterialTheme.colorScheme.surfaceVariant
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            .combinedClickable(
                onClick = { onSelectTopic(topic.id) },
                onLongClick = { isExpanded = true })
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .drawBehind {
                    this.drawRect(color = color)
                }
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = topic.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Justify, fontSize = 14.sp
            )
        }

        DropdownMenu(
            expanded = isExpanded, onDismissRequest = { isExpanded = false },
            offset = DpOffset(x = 100.dp, y = 0.dp)
        ) {
            DropdownMenuItem(onClick = {
                isExpanded = false
                deleteTopic()
            }, text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .width(150.dp)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete)
                    )
                    Text(
                        text = "Delete",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            })
            DropdownMenuItem(onClick = {
                isExpanded = false
                renameTopic()
            }, text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .width(150.dp)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.rename)
                    )
                    Text(
                        text = stringResource(R.string.rename),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            })

        }
    }
}