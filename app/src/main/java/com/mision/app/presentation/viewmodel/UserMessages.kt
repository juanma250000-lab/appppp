package com.mision.app.presentation.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Short feedback for the user (shown as a snackbar). The screen calls
 * [consumed] once it has been displayed, so it survives configuration
 * changes but is never shown twice.
 */
class UserMessages {
    private val _current = MutableStateFlow<String?>(null)
    val current: StateFlow<String?> = _current.asStateFlow()

    fun show(text: String) {
        _current.value = text
    }

    fun consumed() {
        _current.value = null
    }
}

/** Generic copy for unexpected failures (storage errors and the like). */
internal const val GENERIC_ERROR = "Algo ha fallado. Inténtalo de nuevo."
