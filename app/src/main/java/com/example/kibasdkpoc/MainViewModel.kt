package com.example.kibasdkpoc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal data class MainState(
    val pendingDeepLinks: List<String> = emptyList(),
)

internal sealed interface MainAppSideEffect {
    data class HandleDeeplink(val deeplink: String) : MainAppSideEffect
}

internal class MainViewModel(
    private val mutex: Mutex = Mutex()
) : ViewModel() {
    
    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()

    val sideEffects = SideEffectFlow<MainAppSideEffect>()

    fun onDeeplinkReceived(deeplink: String) {
        _state.update {
            it.copy(pendingDeepLinks = it.pendingDeepLinks + deeplink)
        }
    }

    fun onReadyToRedirect() {
        viewModelScope.launch {
            mutex.withLock {
                val deeplink = _state
                    .getAndUpdate { it.copy(pendingDeepLinks = it.pendingDeepLinks.drop(1)) }
                    .pendingDeepLinks.firstOrNull() ?: return@launch

                sideEffects.emit(MainAppSideEffect.HandleDeeplink(deeplink))
            }
        }
    }
}
