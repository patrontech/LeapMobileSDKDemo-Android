package com.example.kibasdkpoc.designsystem.lifecycle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Observes [Lifecycle.Event.ON_RESUME] and [Lifecycle.Event.ON_PAUSE]
 * from the current [LocalLifecycleOwner] and triggers the provided callbacks.
 *
 * The observer is automatically added and removed with the composition.
 *
 * @param onResume Called on resume.
 * @param onPause Called on pause.
 */
@Composable
public fun ObserveLifecycleEvents(
    onResume: () -> Unit = {},
    onPause: () -> Unit = {},
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onResume()
            }
            if (event == Lifecycle.Event.ON_PAUSE) {
                onPause()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
