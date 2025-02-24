package com.kaii.lavender_snackbars

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FloatExponentialDecaySpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.generateDecayAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LavenderSnackbarBox(
    snackbarHostState: LavenderSnackbarHostState,
    modifier: Modifier = Modifier,
    snackbarComposable: @Composable () -> Unit = { LavenderSnackbarDefaults.GetSnackbarType(snackbarHostState) },
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
        LavenderSnackbarHost(snackbarHostState = snackbarHostState)

        val localDensity = LocalDensity.current
        val anchors = DraggableAnchors {
            with(localDensity) {
                DragAnchors.Top at 0f
                DragAnchors.Bottom at (maxHeight - 64.dp - 24.dp).toPx()
                DragAnchors.DismissingBottom at (maxHeight + 64.dp + 75.dp).toPx()
                DragAnchors.DismissingTop at ((-64).dp - 175.dp).toPx()
            }
        }

        val currentEvent = snackbarHostState.currentSnackbarEvent
        val decayAnimationSpec = remember {
            FloatExponentialDecaySpec(3f).generateDecayAnimationSpec<Float>()
        }

        val anchoredDraggableState = remember {
            AnchoredDraggableState(
                initialValue = DragAnchors.Top,
                anchors = anchors,
                positionalThreshold = { total: Float ->
                    total * 0.8f
                },
                velocityThreshold = {
                    with(localDensity) { 200.dp.toPx() }
                },
                snapAnimationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                decayAnimationSpec = decayAnimationSpec,
                confirmValueChange = { state: DragAnchors ->
                    // do not dismiss if the current event is a loading event
                    !((state == DragAnchors.DismissingTop || state == DragAnchors.DismissingBottom) && currentEvent?.event is LavenderSnackbarEvents.LoadingEvent)
                }
            )
        }

        LaunchedEffect(currentEvent) {
            if (currentEvent == null) {
                delay(400)
                anchoredDraggableState.snapTo(anchoredDraggableState.currentValue.getLastPosition())
            }
        }

        LaunchedEffect(anchoredDraggableState.currentValue) {
            if (anchoredDraggableState.currentValue == DragAnchors.DismissingTop || anchoredDraggableState.currentValue == DragAnchors.DismissingBottom) {
                currentEvent?.dismiss()
                delay(400)
                anchoredDraggableState.snapTo(anchoredDraggableState.currentValue.getLastPosition())
            }
        }

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
                    orientation = Orientation.Vertical
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
