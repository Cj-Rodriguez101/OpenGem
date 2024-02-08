package com.cjrodriguez.cjchatgpt.presentation.screens.topicScreen

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.cjrodriguez.cjchatgpt.R
import com.cjrodriguez.cjchatgpt.domain.events.TopicListEvents
import com.cjrodriguez.cjchatgpt.domain.model.Topic
import com.cjrodriguez.cjchatgpt.presentation.screens.topicScreen.components.DeleteDialog
import com.cjrodriguez.cjchatgpt.presentation.screens.topicScreen.components.RenameDialog
import com.cjrodriguez.cjchatgpt.presentation.screens.topicScreen.components.TopicCard
import com.cjrodriguez.cjchatgpt.presentation.ui.theme.CjChatGPTTheme
import com.cjrodriguez.cjchatgpt.presentation.util.GenericMessageInfo
import kotlinx.collections.immutable.ImmutableSet

@Composable
fun TopicScreen(
    query: String,
    allTopics: LazyPagingItems<Topic>,
    messageSet: ImmutableSet<GenericMessageInfo>,
    onTriggerEvents: (TopicListEvents) -> Unit,
    setIdToNavigateToAndOnBackPressed: () -> Unit
) {

    val snackBarHostState = remember { SnackbarHostState() }
    var shouldShowDeleteDialog by remember { mutableStateOf(false) }
    var shouldShowRenameDialog by remember { mutableStateOf(false) }
    var selectedTopic by remember { mutableStateOf(Topic("", "")) }

    BackHandler(onBack = setIdToNavigateToAndOnBackPressed)

    CjChatGPTTheme(
        messageSet = messageSet,
        snackBarHostState = snackBarHostState,
        onRemoveHeadMessageFromQueue = {
            onTriggerEvents(
                TopicListEvents
                    .OnRemoveHeadMessageFromQueue
            )
        }
    ) {

        if (shouldShowDeleteDialog) {
            DeleteDialog(
                onDismiss = { shouldShowDeleteDialog = false },
                onPositiveAction = {
                    onTriggerEvents(
                        TopicListEvents
                            .DeleteTopic(selectedTopic.id)
                    )
                })
        }

        if (shouldShowRenameDialog) {
            RenameDialog(
                topic = selectedTopic,
                onDismiss = { shouldShowRenameDialog = false },
                onPositiveAction = {
                    onTriggerEvents(
                        TopicListEvents
                            .RenameTopic(selectedTopic)
                    )
                },
                onTextChanged = { selectedTopic = selectedTopic.copy(title = it) })
        }
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(topBar = {
                Row(
                    modifier = Modifier
                        .height(56.dp)
                        .padding(start = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = CardDefaults.shape
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { setIdToNavigateToAndOnBackPressed() },
                        modifier = Modifier
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }

                    TextField(value = query, maxLines = 1, singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        onValueChange = { onTriggerEvents(TopicListEvents.SetQuery(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = "History") })
                }
            }

            ) { paddingValues ->

                when (allTopics.loadState.refresh) {
                    LoadState.Loading -> {
                        Log.e("state", "loading")
                    }

                    is LoadState.Error -> {
                        Log.e("state", "error")
                    }

                    else -> {
                        if (allTopics.itemCount == 0) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.no_chats_present),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues),
                                verticalArrangement = Arrangement.Top,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                items(
                                    count = allTopics.itemCount,
                                    key = allTopics.itemKey(Topic::id)
                                ) { index ->
                                    allTopics[index]?.let { topic ->
                                        TopicCard(topic = topic, onSelectTopic = {
                                            //onBackPressed(it)
                                            onTriggerEvents(TopicListEvents.SetTopic(it))
                                            setIdToNavigateToAndOnBackPressed()
                                        }, deleteTopic = {
                                            selectedTopic = topic
                                            shouldShowDeleteDialog = true
                                        }, renameTopic = {
                                            selectedTopic = topic
                                            shouldShowRenameDialog = true
                                        })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}