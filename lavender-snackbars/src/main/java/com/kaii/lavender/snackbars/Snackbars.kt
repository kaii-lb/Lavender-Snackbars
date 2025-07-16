package com.kaii.lavender.snackbars

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

object LavenderSnackbarDefaults {
    val containerColor: Color
        @Composable
        get() = MaterialTheme.colorScheme.primary

    val contentColor: Color
        @Composable
        get() = MaterialTheme.colorScheme.onPrimary

    val contentColorLight: Color
        @Composable
        get() = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)

    val bottomEnterTransition =
        slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) { height -> height } + expandHorizontally(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) { width -> (width * 0.2f).toInt() }

    val bottomExitTransition =
        slideOutVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) { height -> height } + shrinkHorizontally(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) { width -> (width * 0.2f).toInt() }

    val topEnterTransition =
        slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) { height -> -height } + expandHorizontally(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) { width -> (width * 0.2f).toInt() }

    val topExitTransition =
        slideOutVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) { height -> -height } + shrinkHorizontally(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) { width -> (width * 0.2f).toInt() }

    val switchAnimation =
        (scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) + fadeIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            )
        )).togetherWith(
            scaleOut(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeOut(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        )

    const val DISMISS_TIMEOUT = 2000L

    @Composable
    fun GetSnackbarType(snackbarHostState: LavenderSnackbarHostState) {
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
                    icon = event.icon,
                    isLoading = event.isLoading.value
                ) {
                    snackbarHostState.currentSnackbarEvent?.dismiss()
                }
            }

            is LavenderSnackbarEvents.MessageEvent -> {
                val event = currentEvent.event as LavenderSnackbarEvents.MessageEvent

                SnackBarWithMessage(
                    message = event.message,
                    icon = event.icon
                ) {
                    snackbarHostState.currentSnackbarEvent?.dismiss()
                }
            }

            is LavenderSnackbarEvents.ActionEvent -> {
                val event = currentEvent.event as LavenderSnackbarEvents.ActionEvent

                SnackBarWithAction(
                    message = event.message,
                    icon = event.icon,
                    actionIcon = event.actionIcon,
                    action = event.action
                )
            }

            is LavenderSnackbarEvents.ProgressEvent -> {
                val event = currentEvent.event as LavenderSnackbarEvents.ProgressEvent

                SnackbarWithLoadingIndicatorAndBody(
                    message = event.message,
                    body = event.body,
                    icon = event.icon,
                    percentage = event.percentage
                ) {
                    snackbarHostState.currentSnackbarEvent?.dismiss()
                }
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
}

/** Base snackbar with an icon and a message */
@Composable
private fun LavenderSnackbar(
    message: String,
    @DrawableRes icon: Int,
    containerColor: Color = LavenderSnackbarDefaults.containerColor,
    contentColor: Color = LavenderSnackbarDefaults.contentColor,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        contentColor = contentColor,
        color = containerColor,
        shape = CircleShape,
        shadowElevation = 8.dp,
        modifier = Modifier
            .height(64.dp)
            .fillMaxWidth(1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(1f)
                .height(64.dp)
                .padding(16.dp, 8.dp, 12.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                painter = painterResource(id = icon),
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
internal fun SnackBarWithMessage(
    message: String,
    @DrawableRes icon: Int,
    onDismiss: () -> Unit
) {
    LavenderSnackbar(
        message = message,
        icon = icon
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
internal fun SnackBarWithAction(
    message: String,
    @DrawableRes icon: Int,
    @DrawableRes actionIcon: Int,
    action: () -> Unit
) {
    LavenderSnackbar(
        message = message,
        icon = icon
    ) {
        IconButton(
            onClick = {
                action()
            }
        ) {
            Icon(
                painter = painterResource(id = actionIcon),
                contentDescription = "Run this snackbar's action",
                modifier = Modifier
                    .size(32.dp)
            )
        }
    }
}

/** A snackbar showing an infinite loading indicator along with a message and an icon */
@Composable
internal fun SnackbarWithLoadingIndicator(
    message: String,
    @DrawableRes icon: Int,
    isLoading: Boolean,
    dismiss: () -> Unit
) {
    LaunchedEffect(isLoading) {
        if (!isLoading) {
            delay(LavenderSnackbarDefaults.DISMISS_TIMEOUT)
            dismiss()
        }
    }

    LavenderSnackbar(
        message = message,
        icon = icon
    ) {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = {
                LavenderSnackbarDefaults.switchAnimation
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

/** A snackbar showing an loading indicator along with a message, body and an icon */
@Composable
internal fun SnackbarWithLoadingIndicatorAndBody(
    message: String,
    body: MutableState<String>,
    @DrawableRes icon: Int,
    percentage: MutableFloatState,
    dismiss: () -> Unit
) {
    val isLoading by remember {
        derivedStateOf {
            percentage.floatValue < 1f
        }
    }

    LaunchedEffect(isLoading) {
        if (!isLoading) {
            delay(LavenderSnackbarDefaults.DISMISS_TIMEOUT)
            dismiss()
        }
    }

    Surface(
        contentColor = LavenderSnackbarDefaults.contentColor,
        color = LavenderSnackbarDefaults.containerColor,
        shape = CircleShape,
        shadowElevation = 8.dp,
        modifier = Modifier
            .height(64.dp)
            .fillMaxWidth(1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(1f)
                .height(64.dp)
                .padding(16.dp, 8.dp, 12.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = "Snackbar icon",
                modifier = Modifier
                    .size(32.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = message,
                    fontSize = TextUnit(16f, TextUnitType.Sp),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = body.value,
                    fontSize = TextUnit(13f, TextUnitType.Sp),
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            AnimatedContent(
                targetState = isLoading,
                transitionSpec = {
                    LavenderSnackbarDefaults.switchAnimation
                },
                label = "Animate between loading and loaded states in snackbar",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(4.dp, 0.dp)
            ) { loading ->
                if (loading) {
                    val animated by animateFloatAsState(
                        targetValue = percentage.floatValue,
                        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
                    )

                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeCap = StrokeCap.Round,
                        strokeWidth = 4.dp,
                        progress = { animated },
                        trackColor = LavenderSnackbarDefaults.contentColorLight,
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
}
