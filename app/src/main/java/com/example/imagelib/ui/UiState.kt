package com.example.imagelib.ui

import com.example.imagelib.domain.model.ImageResource

sealed interface UiState {
    data object Loading : UiState
    data class Success(val items: List<ImageResource>) : UiState
    data class Error(val message: String?) : UiState
}