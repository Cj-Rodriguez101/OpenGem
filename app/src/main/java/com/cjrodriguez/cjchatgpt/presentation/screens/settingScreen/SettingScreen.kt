package com.cjrodriguez.cjchatgpt.presentation.screens.settingScreen

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import com.cjrodriguez.cjchatgpt.R.drawable
import com.cjrodriguez.cjchatgpt.R.string
import com.cjrodriguez.cjchatgpt.domain.events.SettingsEvents
import com.cjrodriguez.cjchatgpt.presentation.ui.theme.CjChatGPTTheme
import com.cjrodriguez.cjchatgpt.presentation.util.GenericMessageInfo
import kotlinx.collections.immutable.ImmutableSet

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@ExperimentalMaterial3Api
@Composable
fun SettingScreen(
    openAiKey: String,
    geminiKey: String,
    shouldEnableHaptics: Boolean,
    messageSet: ImmutableSet<GenericMessageInfo>,
    onTriggerEvents: (SettingsEvents) -> Unit,
    onBackPressed: () -> Unit
) {
    val snackBarHostState = remember { SnackbarHostState() }
    var shouldApiKeyDialog by remember { mutableStateOf(false) }
    CjChatGPTTheme(
        messageSet = messageSet,
        snackBarHostState = snackBarHostState,
        onRemoveHeadMessageFromQueue = { onTriggerEvents(SettingsEvents.OnRemoveHeadMessage) }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = stringResource(string.settings))
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackPressed) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = stringResource(string.back)
                            )
                        }
                    })
            }
        ) { paddingValues ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                color = MaterialTheme.colorScheme.background
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        SettingsCard(
                            title = stringResource(string.keys),
                            subtitle = stringResource(string.change_api_key),
                            iconImageVector = Icons.Default.Key,
                            contentDescription = stringResource(id = string.keys),
                            onClick = {
                                onTriggerEvents(SettingsEvents.LoadApiKey)
                                shouldApiKeyDialog = true
                            }
                        )

                        SettingsCard(
                            title = "Haptic Feedback",
                            subtitle = stringResource(string.toggle_haptics),
                            iconImageVector = Icons.Default.Vibration,
                            contentDescription = stringResource(string.haptic_feedback),
                            shouldShowSwitch = true,
                            checkedStatus = shouldEnableHaptics,
                            onClick = {
                                onTriggerEvents(SettingsEvents.ToggleHapticState(!shouldEnableHaptics))
                            }
                        )
                    }

                    if (shouldApiKeyDialog) {
                        Dialog(
                            onDismissRequest = {
                                shouldApiKeyDialog = false
                            }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.background,
                                        shape = MaterialTheme.shapes.large
                                    )
                                    .padding(16.dp)
                            ) {
                                Image(
                                    modifier = Modifier.size(48.dp),
                                    painter = painterResource(id = drawable.logo_brain),
                                    contentDescription = stringResource(string.app_logo)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(
                                    label = { Text(text = stringResource(string.open_ai_key)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    value = openAiKey,
                                    onValueChange = {
                                        onTriggerEvents(SettingsEvents.SetOpenAiKey(it))
                                    }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(
                                    label = { Text(text = stringResource(string.gemini_ai_key)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    value = geminiKey,
                                    onValueChange = {
                                        onTriggerEvents(SettingsEvents.SetGeminiKey(it))
                                    }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        onTriggerEvents(
                                            SettingsEvents.SaveApiKeys(
                                                openAiKey = openAiKey,
                                                geminiApiKey = geminiKey
                                            )
                                        )
                                        shouldApiKeyDialog = false
                                    }
                                ) {
                                    Text(text = stringResource(string.save))
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun SettingsCard(
    onClick: () -> Unit,
    iconImageVector: ImageVector,
    contentDescription: String,
    title: String,
    subtitle: String,
    checkedStatus: Boolean = false,
    shouldShowSwitch: Boolean = false
) {
    Card(
        modifier = Modifier
            .padding(bottom = 16.dp)
            .height(60.dp)
            .fillMaxWidth(),
        onClick = onClick,
    ) {
        ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
            val (iconRef, textColumn, switchRef) = createRefs()
            Icon(
                imageVector = iconImageVector,
                contentDescription = contentDescription,
                modifier = Modifier.constrainAs(iconRef) {
                    centerVerticallyTo(parent)
                    start.linkTo(parent.start, margin = 16.dp)
                }
            )

            Column(
                modifier = Modifier
                    .constrainAs(textColumn) {
                        centerVerticallyTo(parent)
                        start.linkTo(iconRef.end, margin = 16.dp)
                    }
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                )
            }

            if (shouldShowSwitch) {
                Switch(
                    modifier = Modifier.constrainAs(switchRef) {
                        end.linkTo(parent.end, margin = 16.dp)
                        centerVerticallyTo(parent)
                    },
                    checked = checkedStatus,
                    onCheckedChange = { onClick() },
                )
            }
        }
    }
}