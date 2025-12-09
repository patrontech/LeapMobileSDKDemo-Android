package com.greencopper.thuzi.logout

import androidx.lifecycle.ViewModel
import com.greencopper.interfacekit.rootview.RootLayoutManager
import com.greencopper.interfacekit.ui.compose.IKViewBuilder
import com.greencopper.thuzi.account.registration.manager.ThuziRegistrationManager
import com.toggl.komposable.architecture.Store

internal class LogoutViewModel(
    val viewBuilder: IKViewBuilder,
    val store: Store<LogoutState, LogoutAction>,
    private val registrationManager: ThuziRegistrationManager,
    private val rootLayoutManager: RootLayoutManager,
) : ViewModel() {

    fun setupView(layoutData: LogoutLayoutData) {
        store.send(LogoutAction.LoadInitialState(layoutData))
    }

    suspend fun logout() {
        store.send(LogoutAction.LogoutTapped)
        registrationManager.logout()
        rootLayoutManager.updateRootLayout()
    }
}
