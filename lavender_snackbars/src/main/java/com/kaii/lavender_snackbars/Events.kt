package com.kaii.lavender_snackbars

import androidx.annotation.DrawableRes
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.MutableState
import kotlin.random.Random

interface LavenderSnackbarEvent {
    val message: String
    val duration: SnackbarDuration
    val id: Int
}

interface LavenderSnackbarData {
    val event: LavenderSnackbarEvent

    fun performAction() {}
    fun dismiss() {}
}

object LavenderSnackbarEvents {
    /** Shows a [SnackbarWithLoadingIndicator] */
    data class LoadingEvent(
        override val message: String,
        @DrawableRes val iconResId: Int,
        val isLoading: MutableState<Boolean>,
        override val id: Int = Random.nextInt()
    ) : LavenderSnackbarEvent {
        override val duration: SnackbarDuration = SnackbarDuration.Indefinite
    }

    /** Shows a [SnackBarWithMessage] */
    data class MessageEvent(
        override val message: String,
        override val duration: SnackbarDuration,
        @DrawableRes val iconResId: Int,
        override val id: Int = Random.nextInt()
    ) : LavenderSnackbarEvent

    /** Shows a [SnackBarWithAction] */
    data class ActionEvent(
        override val message: String,
        override val duration: SnackbarDuration = SnackbarDuration.Indefinite,
        @DrawableRes val iconResId: Int,
        @DrawableRes val actionIconResId: Int,
        val action: () -> Unit,
        override val id: Int = Random.nextInt()
    ) : LavenderSnackbarEvent
}
