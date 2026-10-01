package com.mision.app.presentation.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.LifecycleResumeEffect

/** Shows [message] in the snackbar host, then reports it as consumed. */
@Composable
fun SnackbarMessageEffect(
    message: String?,
    hostState: SnackbarHostState,
    onShown: () -> Unit,
) {
    val currentOnShown by rememberUpdatedState(onShown)
    LaunchedEffect(message) {
        if (message != null) {
            // Consumed only after it was seen: clearing it first would change
            // the key and cancel this very effect.
            hostState.showSnackbar(message)
            currentOnShown()
        }
    }
}

/**
 * Runs [onResume] every time the screen comes back to the foreground (and on
 * first display), so data that depends on "today" is never stale.
 */
@Composable
fun OnResumeEffect(onResume: () -> Unit) {
    val currentOnResume by rememberUpdatedState(onResume)
    LifecycleResumeEffect(Unit) {
        currentOnResume()
        onPauseOrDispose { }
    }
}
