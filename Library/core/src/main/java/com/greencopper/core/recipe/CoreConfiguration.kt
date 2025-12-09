package com.greencopper.core.recipe

import com.greencopper.core.data.KiboSerializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class CoreConfiguration(
    val remoteState: RemoteState,
    val notification: Notifications? = null,
    /*
     Marking this public is a temporary hack for the NFL custom,
     which needs to synthesize the API url for implicit account deletion.

     This comment and the public access modifier should be removed once
     https://patrontech.atlassian.net/browse/KIBO-7390 is done.
     */
    public val ota: OTA,
    val timezone: String? = null,
    @SerialName("content")
    val contentConfig: ContentConfig,
    val custom: Map<String, String>? = null,
): KiboSerializable<CoreConfiguration> {
    @Serializable
    public data class RemoteState(val apiUrl: String, val threshold: Int)

    @Serializable
    public data class Notifications internal constructor(val apiUrl: String)

    @Serializable
    public data class Project internal constructor(val project: String, val eventName: String, val secret: String, val otaApiUrl: String)

    @Serializable
    public data class OTA(
        /*
         Marking this public is a temporary hack for the NFL custom,
         which needs to synthesize the API url for implicit account deletion.

         This comment and the public access modifier should be removed once
         https://patrontech.atlassian.net/browse/KIBO-7390 is done.
         */
        public val apiUrl: String
    )

    @Serializable
    public data class ContentConfig(val expiration: Long, val deprecatedProjects: List<String>)

    override fun getSerializer(): KSerializer<CoreConfiguration> = serializer()
}
