package com.greencopper.interfacekit.commands

import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.interfacekit.commands.system.CommandInfo
import com.greencopper.interfacekit.commands.system.CommandParameters
import com.greencopper.interfacekit.commands.system.ParameterizedCommand
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.toolkit.logging.Logging
import com.greencopper.toolkit.logging.e
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class PresentAppStoreCommand(
    private val localizationService: LocalizationService,
    private val logging: Logging,
) : ParameterizedCommand<PresentAppStoreCommand.PresentAppStoreParams>() {

    override fun executeWith(params: PresentAppStoreParams, origin: Layout?): Flow<Boolean> {
        origin?.context?.let { context ->
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = "market://details?id=${params.packageName}".toUri()
                    setPackage("com.android.vending")
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                logging.e("Unable to open Play Store for package ${params.packageName}", throwable = e)
                Toast.makeText(context, localizationService.getString("force_update.error.android"), Toast.LENGTH_SHORT).show()
            }
        }
        return flow {
            awaitCancellation()
        }
    }

    @Serializable
    data class PresentAppStoreParams(
        val packageName: String,
    ) : KiboSerializable<PresentAppStoreParams> {
        override fun getSerializer(): KSerializer<PresentAppStoreParams> = serializer()
    }

    companion object {
        val key: CommandInfo.Key = CommandInfo.Key("InterfaceKit.PresentAppStore", 1)
    }

    override fun deserialize(commandParameters: CommandParameters): PresentAppStoreParams =
        KiboSerializable.decodeFromJsonElement(commandParameters)
}

