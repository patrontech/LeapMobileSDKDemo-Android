package com.greencopper.testmocks

import com.toggl.komposable.architecture.Store
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

public class MockStore<State : Any, Action : Any>(
    initialState: State,
) : Store<State, Action> {

    public val mutableState: MutableStateFlow<State> = MutableStateFlow(initialState)
    public val actionSent: MutableList<Action> = mutableListOf()

    override val state: Flow<State> = mutableState

    @Deprecated("Use send(List<Action>)", replaceWith = ReplaceWith("this.send(actions)"))
    override fun dispatch(actions: List<Action>) {
        actionSent.addAll(actions)
    }

    override fun <ViewState : Any, ViewAction : Any> optionalView(
        mapToLocalState: (State) -> ViewState?,
        mapToGlobalAction: (ViewAction) -> Action?,
    ): Store<ViewState, ViewAction> {
        TODO("Not yet implemented")
    }

    override fun send(actions: List<Action>) {
        actionSent.addAll(actions)
    }

    override fun <ViewState, ViewAction : Any> view(
        mapToLocalState: (State) -> ViewState,
        mapToGlobalAction: (ViewAction) -> Action?,
    ): Store<ViewState, ViewAction> {
        TODO("Not yet implemented")
    }
}
