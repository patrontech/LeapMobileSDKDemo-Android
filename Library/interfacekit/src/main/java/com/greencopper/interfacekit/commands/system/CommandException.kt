package com.greencopper.interfacekit.commands.system

import kotlinx.serialization.SerializationException

public sealed class CommandException : Throwable()

public sealed class ParameterizedCommandException : CommandException() {
    public object ParamsRequired : IllegalArgumentException("ParameterizedCommand was missing actual Params")
    public class ParseErrorException(message: String) : SerializationException(message)
}
