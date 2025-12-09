package com.greencopper.interfacekit.widgets.ui

import com.greencopper.interfacekit.widgets.JsonWidgetParameters

public abstract class WidgetException : Throwable() {
    public class NoParametersProvided : WidgetException() {
        override val message: String
            get() = "[WidgetException] Couldn't bind widget, parameters were required but not provided."
    }

    public class InvalidParametersProvided(private val params: JsonWidgetParameters? = null) : WidgetException() {
        override val message: String
            get() = "[WidgetException] Couldn't bind widget, couldn't find valid parameters in $params."
    }

    public class ParametersDecodeFailed(private val params: JsonWidgetParameters? = null) : WidgetException() {
        override val message: String
            get() = "[WidgetException] Couldn't decode parameters $params"
    }

    public class ParametersCastFailed(private val params: WidgetParameters? = null) : WidgetException() {
        override val message: String
            get() = "[WidgetException] Couldn't cast parameters $params"
    }
}
