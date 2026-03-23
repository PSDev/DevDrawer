package de.psdev.devdrawer.ui

sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val cause: Throwable) : UiState<Nothing>()
}
