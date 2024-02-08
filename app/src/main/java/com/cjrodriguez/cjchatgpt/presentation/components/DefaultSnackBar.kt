package com.cjrodriguez.cjchatgpt.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun DefaultSnackBar(
    snackBarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
) {
    if (snackBarHostState.currentSnackbarData == null) {
        return
    }

    SnackbarHost(
        hostState = snackBarHostState,
        snackbar = { data ->
            Snackbar(
                containerColor = MaterialTheme.colorScheme.primary,
                action = {
                    data.visuals.actionLabel?.let { actionLabel ->
                        TextButton(onClick = {
                            onDismiss()
                        }) {
                            Text(
                                text = actionLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                    }
                }
            ) {
                Text(
                    text = data.visuals.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }
        }, modifier = modifier
    )
}