package com.example.imagelib.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.imagelib.domain.usecase.GetImagesUseCase
import com.example.imagelib.domain.usecase.InvalidateImageCacheUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getImages: GetImagesUseCase,
    private val invalidateImageCache: InvalidateImageCacheUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        loadImages()
    }

    fun loadImages() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            _state.value = try {
                UiState.Success(getImages())
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Throwable) {
                UiState.Error(error.message)
            }
        }
    }

    fun invalidateCache()  = invalidateImageCache()
}