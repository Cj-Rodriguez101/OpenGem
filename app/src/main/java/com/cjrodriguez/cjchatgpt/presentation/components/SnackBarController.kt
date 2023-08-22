package com.cjrodriguez.cjchatgpt.presentation.components

import android.util.Log
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
constructor(
    private val scope: CoroutineScope,
    private val onRemoveHeadMessageFromQueue: ()-> Unit = {}
){

    private var snackbarJob: Job? = null

    init {
        cancelActiveJob()
    }

    fun getScope() = scope

    fun showSnackBar(
        snackBarHostState: SnackbarHostState,
        message: String,
        actionLabel: String? = null
    ){
        if(snackbarJob == null){
            snackbarJob = scope.launch {
                snackBarHostState.showSnackbar(
                    message = message,
                    actionLabel = actionLabel
                )
                Log.e("index here", "state changed here dismissed")
                onRemoveHeadMessageFromQueue()
                cancelActiveJob()
                //onRemoveHeadMessageFromQueue()
            }
        }
        else{
            cancelActiveJob()
            //onRemoveHeadMessageFromQueue()
            snackbarJob = scope.launch {
                snackBarHostState.showSnackbar(
                    message = message,
                    actionLabel = actionLabel
                )
                Log.e("index here", "state changed here dismissed")
                onRemoveHeadMessageFromQueue()
                //onRemoveHeadMessageFromQueue()
                cancelActiveJob()
            }
        }
    }

    private fun cancelActiveJob(){
        snackbarJob?.let { job ->
            job.cancel()
            //onRemoveHeadMessageFromQueue()
            Log.e("index here", "state changed here dismissed hereeee")
            snackbarJob = Job()
        }
    }
}