package com.cjrodriguez.cjchatgpt.presentation.screens.chatScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cjrodriguez.cjchatgpt.presentation.util.AiType
import com.cjrodriguez.cjchatgpt.presentation.util.AiType.GPT3

@Preview
@Composable
fun AiTextSwitch(
    modifier: Modifier = Modifier,
    aiList: Array<AiType> = AiType.values().filter { it.shouldBeVisible }.toTypedArray(),
    selectedAi: AiType = GPT3,
    changeSelectedItem: (AiType) -> Unit = {},
) {
    val isDarkMode = isSystemInDarkTheme()
    Row(
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        modifier = modifier.then(
            Modifier
                .height(40.dp)
                .width(250.dp)
                .background(
                    Color.Transparent,
                    MaterialTheme.shapes.large
                )
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.shapes.large
                )
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        aiList.forEach { currentAiType ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .width(300.dp)
                    .clickable(onClick = { changeSelectedItem(currentAiType) })
                    .background(
                        color = if (selectedAi == currentAiType) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = MaterialTheme.shapes.large
                    ), verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentAiType.displayName,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = if (selectedAi == currentAiType) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedAi == currentAiType) MaterialTheme.colorScheme.background else {
                        if (isDarkMode) Color.White else Color.Black
                    }
                )
            }
        }
    }
}
