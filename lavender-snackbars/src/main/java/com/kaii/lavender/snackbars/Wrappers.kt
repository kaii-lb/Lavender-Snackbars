package com.kaii.lavender.snackbars

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/** Wrapper for easy displaying of [LavenderSnackbarEvent]s.
 * wrap around the top-most component of your UI, usually a NavHost */
@Suppress("unused")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LavenderSnackbarBox(
    snackbarHostState: LavenderSnackbarHostState,
    modifier: Modifier = Modifier,
    snackbarComposable: @Composable () -> Unit = {
        LavenderSnackbarDefaults.GetSnackbarType(
            snackbarHostState
        )
    },
    bottomEnterTransition: EnterTransition = LavenderSnackbarDefaults.bottomEnterTransition,
    bottomExitTransition: ExitTransition = LavenderSnackbarDefaults.bottomExitTransition,
    topEnterTransition: EnterTransition = LavenderSnackbarDefaults.topEnterTransition,
    topExitTransition: ExitTransition = LavenderSnackbarDefaults.topExitTransition,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize(1f),
        contentAlignment = Alignment.Center
    ) {
        val boxScope = this

        LavenderSnackbarHost(snackbarHostState = snackbarHostState)

        val currentEvent by remember {
            derivedStateOf {
                snackbarHostState.currentSnackbarEvent
            }
        }

        val localDensity = LocalDensity.current
        val normalAnchors =
            DraggableAnchors {
                with(localDensity) {
                    DragAnchors.Top at 0f
                    DragAnchors.Bottom at (boxScope.maxHeight - 128.dp - 24.dp).toPx()
                    DragAnchors.DismissingBottom at (boxScope.maxHeight + 64.dp + 75.dp).toPx()
                    DragAnchors.DismissingTop at ((-128).dp - 175.dp).toPx()
                }
            }
        val loadingAnchors =
            DraggableAnchors {
                with(localDensity) {
                    DragAnchors.Top at 0f
                    DragAnchors.Bottom at (boxScope.maxHeight - 128.dp - 24.dp).toPx()
                }
            }

        val anchoredDraggableState = remember {
            AnchoredDraggableState(
                initialValue = DragAnchors.Top,
                anchors = normalAnchors
            )
        }

        val anchoredDraggableFlingBehavior = AnchoredDraggableDefaults.flingBehavior(
            state = anchoredDraggableState,
            positionalThreshold = { total: Float ->
                total * 0.8f
            },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )

        LaunchedEffect(currentEvent) {
            if (currentEvent == null) {
                delay(500)
                anchoredDraggableState.snapTo(anchoredDraggableState.currentValue.getLastPosition())
            }

            if (currentEvent?.event is LavenderSnackbarEvents.LoadingEvent) {
                anchoredDraggableState.updateAnchors(
                    newAnchors = loadingAnchors
                )
            } else {
                anchoredDraggableState.updateAnchors(
                    newAnchors = normalAnchors
                )
            }
        }

        LaunchedEffect(anchoredDraggableState.currentValue) {
            if (anchoredDraggableState.currentValue == DragAnchors.DismissingTop || anchoredDraggableState.currentValue == DragAnchors.DismissingBottom) {
                currentEvent?.dismiss()
                delay(400)
                anchoredDraggableState.snapTo(anchoredDraggableState.currentValue.getLastPosition())
            }
        }

        val overscrollEffect = rememberOverscrollEffect()
        AnimatedVisibility(
            visible = currentEvent != null,
            enter = if (anchoredDraggableState.currentValue.getLastPosition() == DragAnchors.Top) topEnterTransition else bottomEnterTransition,
            exit = if (anchoredDraggableState.currentValue.getLastPosition() == DragAnchors.Top) topExitTransition else bottomExitTransition,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset {
                    IntOffset(
                        x = 0,
                        y = anchoredDraggableState.requireOffset().roundToInt()
                    )
                }
                .anchoredDraggable(
                    state = anchoredDraggableState,
                    orientation = Orientation.Vertical,
                    flingBehavior = anchoredDraggableFlingBehavior,
                    overscrollEffect = overscrollEffect
                )
                .overscroll(
                    overscrollEffect
                )
                .systemBarsPadding()
                .fillMaxWidth(1f)
                .wrapContentHeight()
                .padding(12.dp)
                .zIndex(100f)
        ) {
            snackbarComposable()
        }

        content()
    }
}
