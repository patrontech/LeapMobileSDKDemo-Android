package com.greencopper.core.automation

import com.greencopper.core.data.KiboSerializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonElement

public typealias AutomationParams = JsonElement

public sealed class AutomationException: Throwable()

public sealed class ParameterizedAutomationException: AutomationException() {
    public object ParamsRequired: IllegalArgumentException("ParameterizedAutomation was missing actual Params")
    public class ParseErrorException(message: String): SerializationException(message)
}

public interface Automation {
    public fun setup(params: AutomationParams?)
}

public abstract class UnparameterizedAutomation: Automation {
    public abstract fun setup()
    override fun setup(params: AutomationParams?): Unit = setup()
}

public abstract class ParameterizedAutomation<T: KiboSerializable<T>>: Automation {
    @Throws(SerializationException::class, ClassCastException::class)
    public abstract fun deserialize(automationParameters: AutomationParams): T
    public abstract fun setupWith(params: T)

    override fun setup(params: AutomationParams?) {
        if (params == null) {
            throw(ParameterizedAutomationException.ParamsRequired)
        } else {
            try {
                setupWith(deserialize(params))
            } catch (serializationException: SerializationException) {
                throw ParameterizedAutomationException.ParseErrorException(serializationException.message
                    ?: "While parsing $params something went wrong")
            } catch (classCastException: ClassCastException) {
                throw ParameterizedAutomationException.ParseErrorException(classCastException.message
                    ?: "While parsing $params and then casting it, something went wrong")
            }
        }
    }
}
