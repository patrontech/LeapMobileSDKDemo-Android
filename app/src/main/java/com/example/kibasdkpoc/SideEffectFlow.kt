package com.example.kibasdkpoc

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A Flow wrapper designed for one-time side effects (navigation, toasts, dialogs, etc.).
 * 
 * Key features:
 * - Side effects are automatically consumed after collection
 * - Thread-safe emission and consumption via Mutex
 * - Prevents duplicate consumption of the same side effect
 * 
 * Usage in ViewModel:
 * ```
 * class MyViewModel : ViewModel() {
 *     val sideEffects = SideEffectFlow<MySideEffect>()
 *     
 *     fun doSomething() {
 *         sideEffects.emit(MySideEffect.ShowToast("Hello"))
 *     }
 * }
 * ```
 * 
 * Usage in Activity/Fragment:
 * ```
 * lifecycleScope.launch {
 *     viewModel.sideEffects.collectAndConsume { sideEffect ->
 *         when (sideEffect) {
 *             is MySideEffect.ShowToast -> showToast(sideEffect.message)
 *         }
 *     }
 * }
 * ```
 */
public class SideEffectFlow<T : Any> {
    
    private val _flow = MutableStateFlow<T?>(null)
    private val mutex = Mutex()

    /**
     * Emits a side effect to be consumed by collectors.
     * Thread-safe: uses mutex to prevent race conditions.
     */
    public fun emit(sideEffect: T) {
        _flow.value = sideEffect
    }

    /**
     * Collects side effects and automatically consumes them after the block executes.
     * 
     * This is a suspending function that will collect indefinitely until cancelled.
     * Each side effect is guaranteed to be consumed exactly once.
     * 
     * @param collector The block to execute for each side effect
     */
    public suspend fun collectAndConsume(collector: FlowCollector<T>) {
        _flow.filterNotNull().collect { value ->
            mutex.withLock {
                // Double-check the value hasn't been consumed by another collector
                if (_flow.value == value) {
                    collector.emit(value)
                    _flow.value = null
                }
            }
        }
    }

    /**
     * Collects side effects and automatically consumes them after the block executes.
     * Convenience overload that takes a simple lambda instead of FlowCollector.
     * 
     * @param block The block to execute for each side effect
     */
    public suspend fun collectAndConsume(block: suspend (T) -> Unit) {
        collectAndConsume(FlowCollector { block(it) })
    }
}
