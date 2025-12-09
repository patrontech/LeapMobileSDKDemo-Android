package com.greencopper.interfacekit.utils

import com.toggl.komposable.architecture.Effect
import com.toggl.komposable.architecture.ReduceResult
import com.toggl.komposable.extensions.merge
import kotlinx.coroutines.flow.Flow

public fun <State, Action> State.withMultipleFlowsEffect(vararg flow: Flow<Action>): ReduceResult<State, Action> =
    ReduceResult(
        this, Effect.merge(
            *flow.map { Effect.fromFlow(it) }.toTypedArray()
        )
    )
