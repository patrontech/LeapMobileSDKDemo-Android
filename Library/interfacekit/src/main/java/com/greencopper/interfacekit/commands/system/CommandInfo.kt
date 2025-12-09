package com.greencopper.interfacekit.commands.system

import com.greencopper.core.data.KiboSerializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import com.greencopper.core.content.Key as CoreContentKey

@Serializable
public data class CommandInfo(
    val key: Key,
    val params: CommandParameters? = null,
) : KiboSerializable<CommandInfo> {
    @Serializable
    public data class Key(override val name: String, override val version: Int) : CoreContentKey()

    override fun getSerializer(): KSerializer<CommandInfo> = serializer()
}
