package com.greencopper.thuzi.account.registration.commands

import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.commands.system.CommandInfo
import com.greencopper.interfacekit.commands.system.CommandParameters
import com.greencopper.interfacekit.commands.system.ParameterizedCommand
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.ui.fragment.findVisibleFragment
import com.greencopper.thuzi.account.registration.initializer.RegistrationData
import com.greencopper.thuzi.account.registration.initializer.RegistrationInitializer
import com.greencopper.thuzi.account.registration.ui.RegistrationFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class DeviceLinkingCommand(
    private val routeController: RouteController,
    private val scope: CoroutineScope
) : ParameterizedCommand<DeviceLinkingCommand.DeviceLinkingCommandData>() {

    override fun executeWith(params: DeviceLinkingCommandData, origin: Layout?): Flow<Boolean> {
        scope.launch {
            origin?.findVisibleFragment()?.let {
                if (it is RegistrationFragment) {
                    it.updateUrl(params.url)
                } else {
                    routeController.redirect(
                        Route.Present(
                            FeatureInfo(
                                RegistrationInitializer.key,
                                RegistrationData(onSuccessFeatureInfo = null, registrationUrl = params.url)
                                    .encodeToJsonElement(),
                                null
                            )
                        )
                    , origin)
                }
            }
        }

        return flowOf(true)
    }

    override fun deserialize(commandParameters: CommandParameters): DeviceLinkingCommandData =
        KiboSerializable.decodeFromJsonElement(commandParameters)

    @Serializable
    internal class DeviceLinkingCommandData(val url: String) :
        KiboSerializable<DeviceLinkingCommandData> {
        override fun getSerializer(): KSerializer<DeviceLinkingCommandData> = serializer()
    }

    companion object {
        val key: CommandInfo.Key = CommandInfo.Key("Thuzi.DeviceLinking", 1)
    }
}
