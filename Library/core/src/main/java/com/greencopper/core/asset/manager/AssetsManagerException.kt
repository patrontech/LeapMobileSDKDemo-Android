package com.greencopper.core.asset.manager

public sealed class AssetsManagerException(cause: Throwable? = null) : Throwable(cause) {
    public class NoConfigurationException : AssetsManagerException() {
        override val message: String
            get() = "[AssetsManager] Can't access assets without configuration."
    }

    public class NoPlaceHolderException : AssetsManagerException() {
        override val message: String
            get() = "[AssetsManager] Can't find any placeholder."
    }
}
