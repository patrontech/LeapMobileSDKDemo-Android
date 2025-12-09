package com.greencopper.interfacekit.draftcontent

import com.greencopper.core.draftcontent.DraftContentManager
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.interfacekit.commands.system.CommandInfo
import com.greencopper.interfacekit.commands.system.UnparameterizedCommand
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.toolkit.logging.Logging
import com.greencopper.toolkit.logging.e
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import retrofit2.HttpException

internal class ToggleDraftContentCommand(
    private val draftContentManager: DraftContentManager,
    private val routeController: RouteController,
    private val localizationService: LocalizationService,
    private val logging: Logging,
    private val backgroundScope: CoroutineScope,
) : UnparameterizedCommand() {

    companion object {
        val key: CommandInfo.Key = CommandInfo.Key("InterfaceKit.ToggleDraftContentCommand", 1)
    }

    override fun execute(origin: Layout?): Flow<Boolean> {
        backgroundScope.launch {
            if (draftContentManager.passcode == null) {
                loadDraftContent()
            } else {
                removeDraftContent()
            }
        }

        return flowOf(true)
    }

    private fun loadDraftContent() {
        routeController.showInputAlert(
            title = localizationService.getString("interfaceKit.toggle_draft_content.sign_in.request.title"),
            message = localizationService.getString("interfaceKit.toggle_draft_content.sign_in.request.message"),
            hint = localizationService.getString("interfaceKit.toggle_draft_content.sign_in.request.text_field.placeholder"),
            isPassword = true,
            positiveText = localizationService.getString("common.confirm"),
            onPositiveClicked = { passcode ->
                backgroundScope.launch { validate(passcode) }
            },
            negativeText = localizationService.getString("common.cancel"),
        )
    }

    private suspend fun validate(passcode: String) {
        try {
            draftContentManager.setPasscode(passcode)

            routeController.showAlert(
                title = localizationService.getString("interfaceKit.toggle_draft_content.sign_in.succeeded.title"),
                message = localizationService.getString("interfaceKit.toggle_draft_content.sign_in.succeeded.message"),
                positiveText = localizationService.getString("common.ok"),
            )

        } catch (e: HttpException) {
            logging.e("Failed to get draft content", throwable = e)

            routeController.showAlert(
                title = localizationService.getString("interfaceKit.toggle_draft_content.sign_in.failed.title"),
                message = localizationService.getString("interfaceKit.toggle_draft_content.sign_in.failed.message"),
                positiveText = localizationService.getString("common.try_again"),
                onPositiveClicked = { loadDraftContent() },
                negativeText = localizationService.getString("common.cancel"),
            )
        }
    }

    private fun removeDraftContent() {
        routeController.showAlert(
            title = localizationService.getString("interfaceKit.toggle_draft_content.sign_out.title"),
            message = localizationService.getString("interfaceKit.toggle_draft_content.sign_out.message"),
            positiveText = localizationService.getString("common.disable"),
            onPositiveClicked = {
                draftContentManager.deletePasscode()
            },
            negativeText = localizationService.getString("common.cancel"),
        )
    }
}
