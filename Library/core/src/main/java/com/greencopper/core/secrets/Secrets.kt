package com.greencopper.core.secrets

public class SecretService(private val secrets: Map<String, String>) {
    public operator fun get(key: String): String? = secrets[key]
}

public val SecretService.otaZip: String
    get() = this["otaZip"] ?: throw SecretMissingException("otaZip secret missing")

public val SecretService.notificationRegistrationApi: String
    get() = this["notificationRegistrationApi"] ?: throw SecretMissingException("notificationRegistrationApi secret missing")

public val SecretService.notificationInboxApi: String
    get() = this["notificationInboxApi"] ?: throw SecretMissingException("notificationInboxApi secret missing")

public val SecretService.remoteStateApi: String
    get() = this["remoteStateApi"] ?: throw SecretMissingException("remoteStateApi secret missing")

public val SecretService.mixhaloPassword: String
    get() = this["mixhaloPassword"] ?: throw SecretMissingException("mixhaloPassword secret missing")

public class SecretMissingException(message: String, cause: Throwable? = null): Throwable(message, cause)
