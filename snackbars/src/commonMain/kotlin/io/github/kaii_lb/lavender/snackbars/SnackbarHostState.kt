package io.github.kaii_lb.lavender.snackbars

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine

/** Sets the currently visible snackbar
 *
 * Handles the [LavenderSnackbarData.dismiss] and [LavenderSnackbarData.performAction] calls when displaying a snackbar
 *
 * Should be remembered, use in conjunction with [LavenderSnackbarHost] */
class LavenderSnackbarHostState {
    var currentSnackbarEvent: LavenderSnackbarData? by mutableStateOf(null)
        private set

    suspend fun showSnackbar(
        event: LavenderSnackbarEvent
    ): SnackbarResult = run {
        val result = try {
            suspendCancellableCoroutine { continuation ->
                continuation.invokeOnCancellation {
                    Napier.d("Snackbar cancelled: $event", null, TAG)

                    if (currentSnackbarEvent?.event?.id == event.id) {
                        currentSnackbarEvent!!.dismiss()
                    }

                    currentSnackbarEvent = null
                }

                currentSnackbarEvent = LavenderSnackbarDataImpl(
                    event,
                    continuation
                )
            }
        } finally {
            Napier.d("Snackbar finished: $event", null, TAG)

            if (currentSnackbarEvent?.event?.id == event.id) {
                currentSnackbarEvent!!.dismiss()
            }

            currentSnackbarEvent = null
        }

        return@run result
    }

    // implements the actual dismiss() and performAction()
    private class LavenderSnackbarDataImpl(
        override val event: LavenderSnackbarEvent,
        private val continuation: CancellableContinuation<SnackbarResult>
    ) : LavenderSnackbarData {
        override fun performAction() {
            if (continuation.isActive) continuation.resumeWith(Result.success(SnackbarResult.ActionPerformed))
        }

        override fun dismiss() {
            if (continuation.isActive) {
                continuation.resumeWith(Result.success(SnackbarResult.Dismissed))
            } else {
                Napier.d("Dismiss ignored because continuation is not active", null, TAG)
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as LavenderSnackbarDataImpl

            if (event != other.event) return false
            if (continuation != other.continuation) return false

            return true
        }

        override fun hashCode(): Int {
            var result = event.hashCode()
            result = 31 * result + continuation.hashCode()
            return result
        }
    }
}

/** Takes events from [LavenderSnackbarController.events] and displays them
 * latest added event removes whatever is before it, and shows itself
 * there is a 300ms delay between each event */
@Composable
fun LavenderSnackbarHost(snackbarHostState: LavenderSnackbarHostState) {
    val inChannel by LavenderSnackbarController.events.collectAsStateWithLifecycle(initialValue = null)

    // LaunchedEffect cancels whenever the keys change, meaning the suspendCancellableCoroutine is also canceled
    // this way we don't have to deal with stupid dismissal rules to make sure latest snackbar is always shown
    // even though this might by hacky
    // note: look into DisposableEffect maybe its onDispose method can help cleanup the suspendCancellableCoroutine with a dismiss()
    LaunchedEffect(inChannel, snackbarHostState) {
        if (inChannel == null) return@LaunchedEffect

        snackbarHostState.currentSnackbarEvent?.dismiss()
        delay(300)
        snackbarHostState.showSnackbar(inChannel!!)
    }

    val currentEvent = snackbarHostState.currentSnackbarEvent
    LaunchedEffect(currentEvent) {
        if (currentEvent != null) {
            when (val event = currentEvent.event) {
                is LavenderSnackbarEvent.LoadingEvent -> {
                    if (!event.isLoading.value) {
                        delay(3000)
                        currentEvent.dismiss()
                    }
                }

                is LavenderSnackbarEvent.ProgressEvent -> {
                    if (event.percentage.floatValue >= 1f) {
                        delay(3000)
                        currentEvent.dismiss()
                    }
                }

                else -> {
                    if (event.duration != SnackbarDuration.Indefinite) {
                        val delay = currentEvent.event.duration.toMillis()
                        delay(delay)
                        currentEvent.dismiss()
                    }
                }
            }
        }
    }
}
