package com.kaii.lavender_snackbars

import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

const val TAG = "LAVENDER_SNACKBARS"

/** Allows sending event to the snackbar controller from any place, composable or not */
object LavenderSnackbarController {
    private val _events = Channel<LavenderSnackbarEvent>(1)
    val events = _events.receiveAsFlow()

    /** queue a snackbar event to be displayed */
    @Suppress("unused")
    suspend fun pushEvent(event: LavenderSnackbarEvent) {
        _events.send(event)
    }
}

// copy paste from composes default private function
/** converts [SnackbarDuration] to milliseconds */
fun SnackbarDuration.toMillis() = when (this) {
    SnackbarDuration.Indefinite -> Long.MAX_VALUE
    SnackbarDuration.Long -> 10000L
    SnackbarDuration.Short -> 4000L
}

internal enum class DragAnchors {
    Top,
    Bottom,
    DismissingTop,
    DismissingBottom
}

internal fun DragAnchors.getLastPosition() =
	if (this == DragAnchors.DismissingTop) DragAnchors.Top
	else if (this == DragAnchors.DismissingBottom) DragAnchors.Bottom
	else this
