package com.kaii.lavender.snackbars

import androidx.annotation.DrawableRes
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface LavenderSnackbarEvent {
    val message: String
    val duration: SnackbarDuration
    val id: Uuid
}

interface LavenderSnackbarData {
    val event: LavenderSnackbarEvent

    fun performAction() {}
    fun dismiss() {}
}

@OptIn(ExperimentalUuidApi::class)
object LavenderSnackbarEvents {
    /** Shows a [SnackbarWithLoadingIndicator] */
    data class LoadingEvent(
        override val message: String,
        @DrawableRes val icon: Int,
        val isLoading: MutableState<Boolean>,
        override val id: Uuid = Uuid.random()
    ) : LavenderSnackbarEvent {
        override val duration: SnackbarDuration = SnackbarDuration.Indefinite
    }

    /** Shows a [SnackBarWithMessage] */
    data class MessageEvent(
        override val message: String,
        override val duration: SnackbarDuration,
        @DrawableRes val icon: Int,
        override val id: Uuid = Uuid.random()
    ) : LavenderSnackbarEvent

    /** Shows a [SnackBarWithAction] */
    data class ActionEvent(
        override val message: String,
        override val duration: SnackbarDuration = SnackbarDuration.Indefinite,
        @DrawableRes val icon: Int,
        @DrawableRes val actionIcon: Int,
        val action: () -> Unit,
        override val id: Uuid = Uuid.random()
    ) : LavenderSnackbarEvent

    /** Shows a [SnackbarWithLoadingIndicatorAndBody] */
    data class ProgressEvent(
        override val message: String,
        val body: MutableState<String>,
        override val duration: SnackbarDuration = SnackbarDuration.Indefinite,
        @DrawableRes val icon: Int,
        val percentage: MutableFloatState,
        override val id: Uuid = Uuid.random()
    ) : LavenderSnackbarEvent
}
