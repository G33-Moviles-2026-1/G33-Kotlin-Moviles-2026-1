package com.example.andespace.ui.common

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

object SnackbarManager {
    private val _messages = Channel<String>(capacity = Channel.UNLIMITED)
    val messages = _messages.receiveAsFlow()

    fun showMessage(message: String) {
        _messages.trySend(message)
    }
}