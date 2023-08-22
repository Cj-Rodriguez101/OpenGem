package com.cjrodriguez.cjchatgpt.presentation.screens.topicScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun DeleteDialog(
    topicId: String = "",
    onDismiss: () -> Unit = {},
    onPositiveAction: () -> Unit = {}
) {

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.background(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = CardDefaults.shape
        )
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                text = "Are You Sure You Want To Delete This Chat?",
                textAlign = TextAlign.Justify,
                fontSize = 14.sp
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

                OutlinedButton(
                    onClick = {
                        onPositiveAction()
                        onDismiss()
                    },
                    colors = ButtonDefaults
                        .textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(text = "Delete", fontSize = 16.sp)
                }
            }
        }
    }
}