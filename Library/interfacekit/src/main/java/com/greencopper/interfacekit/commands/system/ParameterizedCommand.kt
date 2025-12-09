package com.greencopper.interfacekit.commands.system

import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.navigation.layout.Layout
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerializationException

public abstract class ParameterizedCommand<T : KiboSerializable<T>> : Command {

    public abstract fun executeWith(params: T, origin: Layout?): Flow<Boolean>

    @Throws
    override fun execute(params: CommandParameters?, origin: Layout?): Flow<Boolean> {
        if (params == null) {
            throw (ParameterizedCommandException.ParamsRequired)
        } else {
            try {
                return executeWith(deserialize(params), origin)
            } catch (serializationException: SerializationException) {
                throw ParameterizedCommandException.ParseErrorException(
                    serializationException.message
                        ?: "While parsing $params something went wrong"
                )
            } catch (classCastException: ClassCastException) {
                throw ParameterizedCommandException.ParseErrorException(
                    classCastException.message
                        ?: "While parsing $params and then casting it, something went wrong"
                )
            }
        }
    }

    @Throws(SerializationException::class, ClassCastException::class)
    public abstract fun deserialize(commandParameters: CommandParameters): T
}
