package com.cjrodriguez.cjchatgpt.presentation.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.cjrodriguez.cjchatgpt.presentation.util.GenericMessageInfo
import com.cjrodriguez.cjchatgpt.presentation.util.UIComponentType
import com.cjrodriguez.cjchatgpt.presentation.components.DefaultSnackBar
import com.cjrodriguez.cjchatgpt.presentation.components.GenericDialog
import com.cjrodriguez.cjchatgpt.presentation.components.SnackbarController
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun CjChatGPTTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    messageSet: Set<GenericMessageInfo>,
    snackBarHostState: SnackbarHostState,
    onRemoveHeadMessageFromQueue: () -> Unit,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
//        SideEffect {
//            val window = (view.context as Activity).window
//            window.statusBarColor = colorScheme.primary.toArgb()
//            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
//        }

        val systemUiController = rememberSystemUiController()
        val useDarkIcons = !isSystemInDarkTheme()

        DisposableEffect(systemUiController, useDarkIcons) {
            systemUiController.setStatusBarColor(
                color = if (useDarkIcons) Color.Transparent else colorScheme.background,
                darkIcons = useDarkIcons
            )

            systemUiController.setNavigationBarColor(
                color = colorScheme.background,
                darkIcons = useDarkIcons
            )
            onDispose {
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
            ProcessDialogQueue(
                messageSet = messageSet,
                snackBarHostState = snackBarHostState,
                onRemoveHeadMessageFromQueue = onRemoveHeadMessageFromQueue
            )

            DefaultSnackBar(
                snackBarHostState = snackBarHostState,
                onDismiss = { onRemoveHeadMessageFromQueue() },
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
            )
        }
    }
}

@Composable
fun ProcessDialogQueue(
    messageSet: Set<GenericMessageInfo>,
    onRemoveHeadMessageFromQueue: () -> Unit,
    snackBarHostState: SnackbarHostState,
) {

    val scope = rememberCoroutineScope()
    val snackBarController = SnackbarController(scope, onRemoveHeadMessageFromQueue)
    messageSet.isNotEmpty().let {
        messageSet.lastOrNull().let { info ->
            info?.let {
                if (info.uiComponentType == UIComponentType.Dialog) {
                    GenericDialog(
                        onDismiss = info.onDismiss,
                        title = info.title,
                        description = info.description,
                        positiveAction = info.positiveAction,
                        negativeAction = info.negativeAction,
                        onRemoveHeadFromQueue = onRemoveHeadMessageFromQueue,
                        info = info
                    )
                } else if (info.uiComponentType == UIComponentType.SnackBar) {
                    snackBarHostState.currentSnackbarData?.dismiss()
                    snackBarController.showSnackBar(snackBarHostState, info.title ?: "")
                } else {
                    onRemoveHeadMessageFromQueue()
                }
            }
        }
    }
}