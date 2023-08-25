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

@Preview
@Composable
fun TextSwitch(
    modifier: Modifier = Modifier,
    options: List<String> = listOf("GPT 3.5", "GPT 4"),
    selectedState: Boolean = false,
    changeSelectedItem: () -> Unit = {},
) {
    val isDarkMode = isSystemInDarkTheme()
    Row(
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        modifier = modifier.then(
            Modifier
                .height(40.dp)
                .width(250.dp)
                .clickable(onClick = changeSelectedItem)
                .background(Color.Transparent, MaterialTheme.shapes.large)
                .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.large)
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(onClick = changeSelectedItem)
                .background(
                    color = if (!selectedState) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = MaterialTheme.shapes.large
                ), verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = options[0], fontSize = 16.sp, textAlign = TextAlign.Center,
                fontWeight = if (!selectedState) FontWeight.Bold else FontWeight.Normal,
                color = if (!selectedState) MaterialTheme.colorScheme.background else {
                    if (isDarkMode) Color.White else Color.Black
                }
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(onClick = changeSelectedItem)
                .background(
                    color = if (selectedState) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = MaterialTheme.shapes.large
                ), verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = options[1], fontSize = 16.sp, textAlign = TextAlign.Center,
                fontWeight = if (selectedState) FontWeight.Bold else FontWeight.Normal,
                color = if (selectedState) MaterialTheme.colorScheme.background else {
                    if (isDarkMode) Color.White else Color.Black
                }
            )
        }
        //}
    }
}