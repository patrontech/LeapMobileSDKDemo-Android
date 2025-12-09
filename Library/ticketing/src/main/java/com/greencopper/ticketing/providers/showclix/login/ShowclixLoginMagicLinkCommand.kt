package com.greencopper.ticketing.providers.showclix.login

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.interfacekit.commands.system.CommandInfo
import com.greencopper.interfacekit.commands.system.CommandParameters
import com.greencopper.interfacekit.commands.system.ParameterizedCommand
import com.greencopper.interfacekit.links.resolver.LinkResolver
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.ui.fragment.findVisibleFragment
import com.greencopper.ticketing.providers.showclix.login.ui.ShowclixLoginFragment
import com.greencopper.ticketing.providers.showclix.showclix
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class ShowclixLoginMagicLinkCommand(
    private val routeController: RouteController,
    private val linkResolver: LinkResolver,
    private val localStorage: LocalStorage,
    private val scope: CoroutineScope
) : ParameterizedCommand<ShowclixLoginMagicLinkCommand.MagicLinkCommandData>() {

    override fun executeWith(params: MagicLinkCommandData, origin: Layout?): Flow<Boolean> {
        scope.launch {
            origin?.findVisibleFragment()?.let {
                if (it is ShowclixLoginFragment) {
                    it.verifyToken(params.token)
                } else {
                    linkResolver.route(params.routeLink)
                        ?.let { uriRoute ->
                            localStorage.project.showclix.timeToken.value = params.token
                            routeController.redirect(uriRoute, origin)
                        }
                }
            }
        }

        return flowOf(true)
    }

    override fun deserialize(commandParameters: CommandParameters): MagicLinkCommandData =
        KiboSerializable.decodeFromJsonElement(commandParameters)

    @Serializable
    internal class MagicLinkCommandData(val token: String, val routeLink: String) :
        KiboSerializable<MagicLinkCommandData> {
        override fun getSerializer(): KSerializer<MagicLinkCommandData> = serializer()
    }

    companion object {
        val key: CommandInfo.Key = CommandInfo.Key("Ticketing.Showclix.MagicLink", 1)
    }
}
