package com.cjrodriguez.cjchatgpt.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cjrodriguez.cjchatgpt.presentation.util.GenericMessageInfo
import com.cjrodriguez.cjchatgpt.presentation.util.NegativeAction
import com.cjrodriguez.cjchatgpt.presentation.util.PositiveAction
import com.cjrodriguez.cjchatgpt.presentation.util.UIComponentType

@Preview
@Composable
fun GenericDialog(
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    title: String = "Success",
    description: String? = null,
    positiveAction: PositiveAction? = PositiveAction("Yes"),
    negativeAction: NegativeAction? = NegativeAction("No"),
    onRemoveHeadFromQueue: () -> Unit = {},
    info: GenericMessageInfo = GenericMessageInfo.Builder().id("")
        .uiComponentType(UIComponentType.None)
        .title("").build()
) {

    AlertDialog(
        modifier = modifier,
        onDismissRequest = {
            onDismiss?.invoke()
            onRemoveHeadFromQueue()
        },
        title = {
            Text(
                title,
                fontSize = 14.sp
            )
        },
        text = {
            if (description != null) {
                Text(text = description)
            }
        },
        dismissButton = {
            if (negativeAction != null) {
                Button(
                    modifier = Modifier.padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    onClick = {
                        negativeAction.onNegativeAction()
                        onRemoveHeadFromQueue()
                    }
                ) {
                    Text(
                        text = negativeAction.negativeBtnTxt,
                        color = MaterialTheme.colorScheme.background
                    )
                }
            }
        },
        confirmButton = {
            if (positiveAction != null) {
                Button(
                    modifier = Modifier.padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                    onClick = {
                        positiveAction.onPositiveAction()
                        onRemoveHeadFromQueue()
                    },
                ) {
                    Text(
                        text = positiveAction.positiveBtnTxt,
                        color = MaterialTheme.colorScheme.background
                    )
                }
            }
        }
    )
}