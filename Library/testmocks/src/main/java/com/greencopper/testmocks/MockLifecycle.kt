package com.greencopper.testmocks

import androidx.lifecycle.*

public class MockLifecycle(private val owner: LifecycleOwner) : Lifecycle() {
    private var state = State.INITIALIZED
    private val observers: MutableSet<LifecycleObserver> = mutableSetOf()

    public fun setCurrentState(newState: State) {
        state = newState
        for (observer in observers) {
            if (observer is DefaultLifecycleObserver) observer.observe()
            if (observer is LifecycleEventObserver) observer.observe()
        }
    }

    override val currentState: State get() = state

    override fun addObserver(observer: LifecycleObserver) {
        observers.add(observer)
    }

    override fun removeObserver(observer: LifecycleObserver) {
        observers.remove(observer)
    }

    private fun DefaultLifecycleObserver.observe() =
       when (state) {
           State.CREATED -> onCreate(owner)
           State.STARTED -> onStart(owner)
           State.RESUMED -> onResume(owner)
           State.DESTROYED -> onDestroy(owner)
           else -> {}
       }

    private fun LifecycleEventObserver.observe() =
        when (state) {
            State.CREATED -> onStateChanged(owner, Event.ON_CREATE)
            State.STARTED -> onStateChanged(owner, Event.ON_START)
            State.RESUMED -> onStateChanged(owner, Event.ON_RESUME)
            State.DESTROYED -> onStateChanged(owner, Event.ON_DESTROY)
            else -> {}
        }
}

public class MockLifecycleOwner : LifecycleOwner {
    private val mockLifecycle = MockLifecycle(this)

    public var currentState: Lifecycle.State
        get() = lifecycle.currentState
        set(newState) {
            mockLifecycle.setCurrentState(newState)
        }

    override val lifecycle: Lifecycle get() = mockLifecycle
}
