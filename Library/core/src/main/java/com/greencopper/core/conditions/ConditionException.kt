package com.greencopper.core.conditions

import kotlinx.serialization.SerializationException
import java.lang.IllegalArgumentException

public sealed class ConditionException: Throwable()

public sealed class ParameterizedConditionException: ConditionException() {
    public object ParamsRequired: IllegalArgumentException("ParameterizedCondition was missing actual Params")
    public class ParseErrorException(message: String): SerializationException(message)
}