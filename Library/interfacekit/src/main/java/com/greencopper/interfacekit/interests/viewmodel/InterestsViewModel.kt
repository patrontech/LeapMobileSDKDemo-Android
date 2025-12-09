package com.greencopper.interfacekit.interests.viewmodel

import androidx.lifecycle.ViewModel
import com.greencopper.interfacekit.interests.InterestsLayoutData
import com.greencopper.interfacekit.ui.compose.IKViewBuilder
import com.toggl.komposable.architecture.Store

internal class InterestsViewModel(
    val viewBuilder: IKViewBuilder,
    private val store: Store<InterestsState, InterestsAction>,
) : ViewModel() {

    val viewState = store.state

    fun setupView(data: InterestsLayoutData) {
        store.send(InterestsAction.LoadInitialState(data))
    }

    fun onInterestClick(id: String, isSelected: Boolean) {
        store.send(InterestsAction.InterestTapped(id, isSelected))
    }

    fun onInterestsClosed() {
        store.send(InterestsAction.InterestsClosed)
    }
}
