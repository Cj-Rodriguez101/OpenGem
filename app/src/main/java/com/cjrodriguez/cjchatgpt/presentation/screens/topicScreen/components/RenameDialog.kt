package com.cjrodriguez.cjchatgpt.presentation.screens.topicScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cjrodriguez.cjchatgpt.R
import com.cjrodriguez.cjchatgpt.domain.model.Topic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun RenameDialog(
    topic: Topic = Topic("", ""),
    onDismiss: () -> Unit = {},
    onTextChanged: (String)-> Unit = {},
    onPositiveAction: () -> Unit = {}
) {

    val currentText by remember{ mutableStateOf(topic.title) }
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.background(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = CardDefaults.shape
        )
    ) {
        Column {
            OutlinedTextField(
                value = topic.title,
                onValueChange = {
                    onTextChanged(it)
                },
                label = { Text(text = stringResource(R.string.new_name))},
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.padding(end = 16.dp)) {
                    Text(text = "Cancel", fontSize = 16.sp)
                }

                OutlinedButton( enabled = topic.title.isNotEmpty() && (topic.title != currentText),
                    onClick = {
                        onPositiveAction()
                        onDismiss()
                    }
                ) {
                    Text(text = stringResource(id = R.string.rename), fontSize = 16.sp)
                }
            }
        }
    }
}