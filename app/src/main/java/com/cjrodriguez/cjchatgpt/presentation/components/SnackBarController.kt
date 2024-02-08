package com.cjrodriguez.cjchatgpt.presentation.components

import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * If a snackbar is visible and the user triggers a second snackbar to show, it will remove
 * the first one and show the second. Likewise with a third, fourth, ect...
 *
 * If a mechanism like this is not used, snackbar get added to the Scaffolds "queue", and will
 * show one after another. I don't like that.
 *
 */
class SnackbarController
    (
    private val scope: CoroutineScope,
    private val onRemoveHeadMessageFromQueue: () -> Unit = {}
) {

    private var snackbarJob: Job? = null

    init {
        cancelActiveJob()
    }

    fun showSnackBar(
        snackBarHostState: SnackbarHostState,
        message: String,
        actionLabel: String? = null
    ) {
        if (snackbarJob == null) {
            snackbarJob = scope.launch {
                snackBarHostState.showSnackbar(
                    message = message,
                    actionLabel = actionLabel
                )
                onRemoveHeadMessageFromQueue()
                cancelActiveJob()
            }
        } else {
            cancelActiveJob()
            snackbarJob = scope.launch {
                snackBarHostState.showSnackbar(
                    message = message,
                    actionLabel = actionLabel
                )
                onRemoveHeadMessageFromQueue()
                cancelActiveJob()
            }
        }
    }

    private fun cancelActiveJob() {
        snackbarJob?.let { job ->
            job.cancel()
            snackbarJob = Job()
        }
    }
}