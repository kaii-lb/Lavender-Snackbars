package com.kaii.lavender_snackbars

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/** Wrapper for easy displaying of [LavenderSnackbarEvent]s.
 * wrap around the top-most component of your UI, usually a [NavHost] */
@Composable
fun LavenderSnackbarBox(
    snackbarHostState: LavenderSnackbarHostState,
    modifier: Modifier = Modifier,
    enterTransition: EnterTransition = slideInVertically { height -> height } + expandHorizontally { width -> (width * 0.2f).toInt() },
    exitTransition: ExitTransition = slideOutVertically { height -> height } + shrinkHorizontally { width -> (width * 0.2f).toInt() },
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize(1f),
        contentAlignment = Alignment.Center
    ) {
        LavenderSnackbarHost(snackbarHostState = snackbarHostState)

        AnimatedVisibility(
            visible = snackbarHostState.currentSnackbarEvent != null,
            enter = enterTransition,
            exit = exitTransition,
            modifier = Modifier
                .systemBarsPadding()
                .fillMaxSize(1f)
                .padding(12.dp)
                .zIndex(100f)
        ) {
            // keep last event in memory so the exit animation actually works
            // not proud of this but oh well it works
            var lastEvent by remember { mutableStateOf(snackbarHostState.currentSnackbarEvent!!) }
            val currentEvent = remember(snackbarHostState.currentSnackbarEvent) {
                if (snackbarHostState.currentSnackbarEvent == null) {
                    lastEvent
                } else {
                    lastEvent = snackbarHostState.currentSnackbarEvent!!
                    lastEvent
                }
            }

            when (currentEvent.event) {
                is LavenderSnackbarEvents.LoadingEvent -> {
                    val event = currentEvent.event as LavenderSnackbarEvents.LoadingEvent

                    SnackbarWithLoadingIndicator(
                        message = event.message,
                        iconResId = event.iconResId,
                        isLoading = event.isLoading.value
                    ) {
                        snackbarHostState.currentSnackbarEvent?.dismiss()
                    }
                }

                is LavenderSnackbarEvents.MessageEvent -> {
                    val event = currentEvent.event as LavenderSnackbarEvents.MessageEvent

                    SnackBarWithMessage(
                        message = event.message,
                        iconResId = event.iconResId
                    ) {
                        snackbarHostState.currentSnackbarEvent?.dismiss()
                    }
                }

                is LavenderSnackbarEvents.ActionEvent -> {
                    val event = currentEvent.event as LavenderSnackbarEvents.ActionEvent

                    SnackBarWithAction(
                        message = event.message,
                        iconResId = event.iconResId,
                        actionIconResId = event.actionIconResId,
                        action = event.action
                    )
                }

                else -> {
                    Text(
                        text = "THIS SHOULD NOT BE VISIBLE",
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(8.dp)
                            .clip(CircleShape)
                    )
                }
            }
        }

        content()
    }
}
