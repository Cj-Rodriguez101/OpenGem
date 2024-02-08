package com.cjrodriguez.cjchatgpt.presentation.screens.chatScreen

import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.cjrodriguez.cjchatgpt.R
import com.cjrodriguez.cjchatgpt.data.datasource.network.internet_check.ConnectivityObserver
import com.cjrodriguez.cjchatgpt.data.util.revertCodeOrPlainHtmlBackToHtml
import com.cjrodriguez.cjchatgpt.data.util.revertHtmlToPlainText
import com.cjrodriguez.cjchatgpt.domain.events.ChatListEvents
import com.cjrodriguez.cjchatgpt.domain.model.Chat
import com.cjrodriguez.cjchatgpt.presentation.components.UiText
import com.cjrodriguez.cjchatgpt.presentation.screens.chatScreen.components.AiTextSwitch
import com.cjrodriguez.cjchatgpt.presentation.screens.chatScreen.components.AnimateTypewriterText
import com.cjrodriguez.cjchatgpt.presentation.screens.chatScreen.components.ChatCard
import com.cjrodriguez.cjchatgpt.presentation.screens.chatScreen.components.QuestionTextField
import com.cjrodriguez.cjchatgpt.presentation.ui.theme.CjChatGPTTheme
import com.cjrodriguez.cjchatgpt.presentation.util.AiType
import com.cjrodriguez.cjchatgpt.presentation.util.GenericMessageInfo
import com.cjrodriguez.cjchatgpt.presentation.util.rememberImeState
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ChatScreen(
    selectedAiType: AiType,
    isLoading: Boolean,
    allChats: LazyPagingItems<Chat>,
    messageSet: ImmutableSet<GenericMessageInfo>,
    status: ConnectivityObserver.Status,
    message: String,
    topicTitle: String,
    topicId: String,
    wordCount: Int,
    upperLimit: Int,
    errorMessage: UiText,
    navigateToHistoryScreen: (String) -> Unit,
    onTriggerEvent: (ChatListEvents) -> Unit
) {

    val snackBarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val imeState = rememberImeState()
    val scrollState = rememberScrollState()

    LaunchedEffect(key1 = imeState.value) {
        if (imeState.value) {
            scrollState.animateScrollTo(scrollState.maxValue, tween(300))
        }
    }

    CjChatGPTTheme(
        messageSet = messageSet,
        snackBarHostState = snackBarHostState,
        onRemoveHeadMessageFromQueue = {
            onTriggerEvent(ChatListEvents.RemoveHeadMessage)
        }
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val context = LocalContext.current
            ModalNavigationDrawer(
                drawerContent =
                {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(
                                color = MaterialTheme.colorScheme.background,
                                shape = RoundedCornerShape(0.dp, 10.dp, 10.dp, 0.dp)
                            )
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ai_icon),
                            contentDescription = "ai", modifier = Modifier
                                .padding(16.dp)
                                .size(40.dp),
                            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onSurface)
                        )

                        OutlinedButton(
                            onClick = {
                                scope.launch { drawerState.close() }
                                onTriggerEvent(ChatListEvents.NewChat)
                            },
                            modifier = Modifier
                                .width(200.dp)
                                .padding(16.dp),
                            shape = CardDefaults.shape
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = context.getString(
                                        R.string.new_chat
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = context.getString(
                                        R.string.new_chat
                                    )
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                scope.launch { drawerState.close() }
                                navigateToHistoryScreen(topicId)
                            },
                            modifier = Modifier
                                .width(200.dp)
                                .padding(16.dp),
                            shape = CardDefaults.shape
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = context.getString(R.string.history))
                                Icon(
                                    imageVector = Icons.Outlined.History,
                                    contentDescription = context.getString(
                                        R.string.history
                                    )
                                )
                            }
                        }
                    }
                }, drawerState = drawerState
            ) {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(title = {
                            Text(
                                text = "GPT",
                                textAlign = TextAlign.Center,
                            )
                        }, navigationIcon = {
                            IconButton(onClick = {
                                if (drawerState.isClosed) {
                                    scope.launch {
                                        drawerState.open()
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    context.getString(R.string.open_drawer)
                                )
                            }
                        }, actions = {
                            if (topicTitle.isNotEmpty()) {
                                IconButton(
                                    onClick = { onTriggerEvent(ChatListEvents.NewChat) },
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = stringResource(
                                            R.string.add_new_chat
                                        )
                                    )
                                }
                            }
                        })
                    },
                ) { paddingValues ->
                    ConstraintLayout(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .verticalScroll(scrollState)
                    ) {
                        val (switch, chatSpace, textField, title) = createRefs()

                        AiTextSwitch(selectedAi = selectedAiType,
                            changeSelectedItem = { onTriggerEvent(ChatListEvents.SetGptVersion(it)) },
                            modifier = Modifier.constrainAs(switch) {
                                top.linkTo(parent.top)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            })

                        Text(text = topicTitle,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .height(if (topicTitle.isEmpty()) 0.dp else 20.dp)
                                .constrainAs(title) {
                                    top.linkTo(switch.bottom, margin = 16.dp)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                }
                                .padding(start = 16.dp, end = 16.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (allChats.itemCount == 0) {
                            Column(
                                modifier = Modifier
                                    .constrainAs(chatSpace) {
                                        top.linkTo(title.bottom, 16.dp)
                                        start.linkTo(parent.start, 16.dp)
                                        end.linkTo(parent.end, 16.dp)
                                        bottom.linkTo(textField.top)
                                        this.width = Dimension.fillToConstraints
                                        this.height = Dimension.fillToConstraints
                                    }, verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AnimateTypewriterText(
                                    baseText = "●",
                                    highlightText = "here",
                                    parts = listOf(
                                        stringResource(R.string.wait),
                                        stringResource(R.string.hold_up),
                                        stringResource(R.string.let_him_cook)
                                    )
                                )
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .constrainAs(chatSpace) {
                                        top.linkTo(title.bottom, 16.dp)
                                        start.linkTo(parent.start, 16.dp)
                                        end.linkTo(parent.end, 16.dp)
                                        bottom.linkTo(textField.top)
                                        this.width = Dimension.fillToConstraints
                                        this.height = Dimension.fillToConstraints
                                    }, verticalArrangement = Arrangement.Top,
                                reverseLayout = true,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                items(
                                    count = allChats.itemCount,
                                    key = allChats.itemKey(Chat::messageId)
                                ) { index ->

                                    allChats[index]?.let {
                                        ChatCard(
                                            it,
                                            onCopyClick = {
                                                onTriggerEvent(
                                                    ChatListEvents.CopyTextToClipBoard(
                                                        it.content.revertCodeOrPlainHtmlBackToHtml()
                                                            .revertHtmlToPlainText()
                                                    )
                                                )
                                            })
                                    }
                                }
                            }
                            if (isLoading) {
                                LaunchedEffect(allChats.itemCount) { listState.scrollToItem(0) }
                            }
                        }

                        QuestionTextField(message = message,
                            wordCount = wordCount,
                            upperLimit = upperLimit,
                            errorMessage = errorMessage.asString(),
                            isLoading = isLoading,
                            updateMessage = { onTriggerEvent(ChatListEvents.SetMessage(it)) },
                            sendMessage = {
                                onTriggerEvent(ChatListEvents.SendMessage(status))
                                keyboardController?.hide()
                            },
                            cancelMessageGeneration = {
                                onTriggerEvent(ChatListEvents.CancelChatGeneration)
                            },
                            modifier = Modifier
                                .constrainAs(textField) {
                                    bottom.linkTo(parent.bottom)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                }
                        )
                    }
                }
            }
        }
    }
}