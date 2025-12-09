package com.greencopper.interfacekit.navigation.feature

import com.greencopper.interfacekit.navigation.feature.info.FeatureParams

public sealed class FeatureInitializerException : Throwable() {
    public class NoParametersProvidedException : FeatureInitializerException() {
        override val message: String =
            "[FeatureInitializerException] Couldn't retrieve Layout, parameters were required but not provided."
    }

    public class ParametersDecodeFailed(params: FeatureParams? = null) : FeatureInitializerException() {
        override val message: String = "[FeatureInitializerException] Couldn't decode parameters $params"
    }

    public class ParametersNotValid(params: FeatureParams? = null) : FeatureInitializerException() {
        override val message: String =
            "[FeatureInitializerException] Provided parameters doesn't meet the requirements to show this layout : $params"
    }

    public class FeatureDisabled : FeatureInitializerException() {
        override val message: String = "[FeatureInitializerException] Feature is disabled"
    }
}
