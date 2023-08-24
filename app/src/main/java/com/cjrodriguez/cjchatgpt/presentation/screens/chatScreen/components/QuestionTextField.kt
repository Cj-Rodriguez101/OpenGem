package com.cjrodriguez.cjchatgpt.presentation.screens.chatScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.cjrodriguez.cjchatgpt.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun QuestionTextField(
    modifier: Modifier = Modifier,
    message: String = "",
    wordCount: Int = 0,
    upperLimit: Int = 500,
    errorMessage: String = "",
    isLoading: Boolean = false,
    sendMessage: () -> Unit = {},
    cancelMessageGeneration: ()-> Unit = {},
    updateMessage: (String) -> Unit = {}
) {

    Column(
        modifier = modifier.then(
            Modifier
                .imePadding()
                .fillMaxWidth()
                .padding(16.dp)
        )

    ) {
        OutlinedTextField(
            value = message, onValueChange = updateMessage, isError = errorMessage.isNotEmpty(),
            trailingIcon = {
                if (errorMessage.isNotEmpty())
                    Icon(
                        Icons.Filled.Error,
                        stringResource(R.string.error), tint = MaterialTheme.colorScheme.error
                    )
            },
            modifier = Modifier
                .heightIn(max = 200.dp)
                .fillMaxWidth()
        )

        if(!isLoading){
            CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                if (errorMessage.isNotEmpty()) Text(
                    text = errorMessage,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.error
                )
                ConstraintLayout(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    val (closeButton, noCount, sendButton) = createRefs()
                    IconButton(onClick = { updateMessage("") }, modifier = Modifier
                        .size(24.dp)
                        .constrainAs(closeButton) {
                            start.linkTo(parent.start)
                            top.linkTo(parent.top)
                            //bottom.linkTo(parent.bottom)
                        }) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.clear_text)
                        )
                    }

                    IconButton(onClick = sendMessage,
                        enabled = errorMessage.isEmpty() && message.trim().isNotEmpty() && !isLoading,
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

                    Text(text = "$wordCount / $upperLimit", modifier = Modifier.constrainAs(noCount) {
                        end.linkTo(sendButton.start, margin = 16.dp)
                        top.linkTo(parent.top)
                        //bottom.linkTo(parent.bottom)
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
                    Icon(imageVector = Icons.Default.Stop, tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "Stop Message Generation",
                        modifier = Modifier.size(56.dp))
                }
            }
        }
    }

}