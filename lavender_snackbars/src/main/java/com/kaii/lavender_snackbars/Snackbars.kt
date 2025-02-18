package com.kaii.lavender_snackbars

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/** Base snackbar with an icon and a message */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BoxScope.LavenderSnackbar(
    message: String,
    @DrawableRes iconResId: Int,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    content: @Composable RowScope.() -> Unit
) {
    val localDensity = LocalDensity.current
    val anchors = DraggableAnchors {
        with (localDensity) {
            DragAnchors.Start at -100.dp.toPx()
            DragAnchors.Center at 0.dp.toPx()
            DragAnchors.End at 100.dp.toPx()
        }
    }

    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val anchoredDraggableState = remember {
        AnchoredDraggableState(
            initialValue = DragAnchors.Start,
            anchors = anchors,
            positionalThreshold = { total: Float ->
                total * 0.7f
            },
            velocityThreshold = {
                with (localDensity) { 100.dp.toPx() }
            },
            snapAnimationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            decayAnimationSpec = decayAnimationSpec
        )
    }

    var snackbarAlignment by remember { mutableStateOf(Alignment.BottomCenter)}
    LaunchedEffect(anchoredDraggableState.currentValue) {
        snackbarAlignment = if (anchoredDraggableState.currentValue == DragAnchors.Start) {
            Alignment.TopCenter
        } else {
            Alignment.BottomCenter
        }
    }

    Surface(
        contentColor = contentColor,
        color = containerColor,
        shape = CircleShape,
        shadowElevation = 8.dp,
        modifier = Modifier
            .height(64.dp)
            .fillMaxWidth(1f)
            .align(snackbarAlignment)
            .anchoredDraggable(
                state = anchoredDraggableState,
                orientation = Orientation.Vertical
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(1f)
                .fillMaxHeight(1f)
                .padding(16.dp, 8.dp, 12.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = "Snackbar icon",
                modifier = Modifier
                    .size(32.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = message,
                fontSize = TextUnit(16f, TextUnitType.Sp),
                modifier = Modifier
                    .weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            content()
        }
    }
}

/** A snackbar displaying an icon, a message and its dismiss button*/
@Composable
internal fun BoxScope.SnackBarWithMessage(
    message: String,
    @DrawableRes iconResId: Int,
    onDismiss: () -> Unit
) {
    LavenderSnackbar(
        message = message,
        iconResId = iconResId
    ) {
        IconButton(
            onClick = {
                onDismiss()
            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.close),
                contentDescription = "Dismiss this snackbar",
                modifier = Modifier
                    .size(32.dp)
            )
        }
    }
}

/** A snackbar displaying an icon, a message and an action */
@Composable
internal fun BoxScope.SnackBarWithAction(
    message: String,
    @DrawableRes iconResId: Int,
    @DrawableRes actionIconResId: Int,
    action: () -> Unit
) {
    LavenderSnackbar(
        message = message,
        iconResId = iconResId
    ) {
        IconButton(
            onClick = {
                action()
            }
        ) {
            Icon(
                painter = painterResource(id = actionIconResId),
                contentDescription = "Run this snackbar's action",
                modifier = Modifier
                    .size(32.dp)
            )
        }
    }
}

/** A snackbar showing an infinite loading indicator along with a message and an icon */
@Composable
internal fun BoxScope.SnackbarWithLoadingIndicator(
    message: String,
    @DrawableRes iconResId: Int,
    isLoading: Boolean,
    dismiss: () -> Unit
) {
    LaunchedEffect(isLoading) {
        if (!isLoading) {
            delay(2000)
            dismiss()
        }
    }

    LavenderSnackbar(
        message = message,
        iconResId = iconResId
    ) {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = {
                (scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy
                    )
                ) + fadeIn()
                        ).togetherWith(
                        scaleOut(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy
                            )
                        ) + fadeOut()
                    )
            },
            label = "Animate between loading and loaded states in snackbar",
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(4.dp, 0.dp)
        ) { loading ->
            if (loading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeCap = StrokeCap.Round,
                    strokeWidth = 4.dp,
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.CenterVertically)
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.checkmark),
                    contentDescription = "Loading done",
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}