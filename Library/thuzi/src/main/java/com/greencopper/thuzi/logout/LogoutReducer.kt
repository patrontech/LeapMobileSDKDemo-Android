package com.greencopper.thuzi.logout

import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.toggl.komposable.architecture.ReduceResult
import com.toggl.komposable.architecture.Reducer
import com.toggl.komposable.extensions.withoutEffect

internal class LogoutReducer(
    private val localizationService: LocalizationService,
) : Reducer<LogoutState, LogoutAction> {

    override fun reduce(
        state: LogoutState,
        action: LogoutAction
    ): ReduceResult<LogoutState, LogoutAction> {
        return when (action) {
            is LogoutAction.LoadInitialState ->
                LogoutState(
                    title = localizationService.getString("thuzi.logout.title"),
                    subtitle = localizationService.getString("thuzi.logout.subtitle"),
                    buttonText = localizationService.getString("thuzi.logout.button"),
                ).withoutEffect()
            is LogoutAction.LogoutTapped -> state.withoutEffect()
        }
    }
}

internal sealed class LogoutAction {
    data class LoadInitialState(val data: LogoutLayoutData) : LogoutAction()
    data object LogoutTapped : LogoutAction()
}
