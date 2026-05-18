package io.github.kaii_lb.lavender.snackbars

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kaii_lb.lavender.snackbars.generated.resources.Res
import io.github.kaii_lb.lavender.snackbars.generated.resources.checkmark
import io.github.kaii_lb.lavender.snackbars.generated.resources.close
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import kotlin.math.PI

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
        ) { height -> height } + scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            initialScale = 0.2f
        )

    val bottomExitTransition =
        slideOutVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) { height -> height } + scaleOut(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )

    val topEnterTransition =
        slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) { height -> -height } + scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            initialScale = 0.2f
        )

    val topExitTransition =
        slideOutVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) { height -> -height } + scaleOut(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )

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
            is LavenderSnackbarEvent.LoadingEvent -> {
                val event = currentEvent.event as LavenderSnackbarEvent.LoadingEvent

                SnackbarWithLoadingIndicator(
                    message = event.message,
                    icon = event.icon,
                    isLoading = event.isLoading.value
                ) {
                    snackbarHostState.currentSnackbarEvent?.dismiss()
                }
            }

            is LavenderSnackbarEvent.MessageEvent -> {
                val event = currentEvent.event as LavenderSnackbarEvent.MessageEvent

                SnackBarWithMessage(
                    message = event.message,
                    icon = event.icon
                ) {
                    snackbarHostState.currentSnackbarEvent?.dismiss()
                }
            }

            is LavenderSnackbarEvent.ActionEvent -> {
                val event = currentEvent.event as LavenderSnackbarEvent.ActionEvent

                SnackBarWithAction(
                    message = event.message,
                    icon = event.icon,
                    actionIcon = event.actionIcon,
                    action = event.action
                )
            }

            is LavenderSnackbarEvent.ProgressEvent -> {
                val event = currentEvent.event as LavenderSnackbarEvent.ProgressEvent

                SnackbarWithLoadingIndicatorAndBody(
                    message = event.message,
                    body = event.body,
                    icon = event.icon,
                    percentage = event.percentage
                ) {
                    snackbarHostState.currentSnackbarEvent?.dismiss()
                }
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
            .heightIn(min = 64.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, end = 12.dp, bottom = 8.dp),
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
                fontSize = 16.sp,
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
                painter = painterResource(resource = Res.drawable.close),
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
                .padding(horizontal = 4.dp)
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
                    painter = painterResource(resource = Res.drawable.checkmark),
                    contentDescription = "Loading done",
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}

/** A snackbar showing a loading indicator along with a message, body and an icon */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
            .heightIn(min = 64.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, end = 12.dp, bottom = 8.dp),
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
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = message,
                    fontSize = 16.sp,
                    maxLines = 1
                )

                Text(
                    text = body.value,
                    fontSize = 13.sp,
                    maxLines = 1
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
                    .padding(horizontal = 4.dp)
            ) { loading ->
                if (loading) {
                    val animated by animateFloatAsState(
                        targetValue = percentage.floatValue,
                        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterVertically),
                        contentAlignment = Alignment.Center
                    ) {
                        val density = LocalDensity.current

                        val circumference = remember {
                            with(density) {
                                2f * PI.toFloat() * 15.dp.toPx()
                            }
                        }

                        val period = remember(circumference) {
                            circumference / 8f
                        }

                        val infiniteTransition = rememberInfiniteTransition()
                        val infinitePhase by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = -period * 2,
                            animationSpec = infiniteRepeatable(
                                animation = tween(
                                    durationMillis = 800
                                ),
                                repeatMode = RepeatMode.Restart
                            )
                        )

                        CircularWavyProgressIndicator(
                            progress = { animated },
                            color = MaterialTheme.colorScheme.onPrimary,
                            trackColor = LavenderSnackbarDefaults.contentColorLight,
                            trackStroke = Stroke(
                                width = with(density) { 4.dp.toPx() },
                                cap = StrokeCap.Round,
                                pathEffect = PathEffect.dashPathEffect(
                                    intervals = floatArrayOf(
                                        period * 0.5f,
                                        period * 0.5f
                                    ),
                                    phase = infinitePhase
                                )
                            ),
                            stroke = Stroke(
                                width = with(density) { 4.dp.toPx() },
                                cap = StrokeCap.Round
                            ),
                            wavelength = 8.dp,
                            amplitude = { 0f },
                            gapSize = 2.dp,
                            modifier = Modifier
                                .size(30.dp)
                        )
                    }
                } else {
                    Icon(
                        painter = painterResource(resource = Res.drawable.checkmark),
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
