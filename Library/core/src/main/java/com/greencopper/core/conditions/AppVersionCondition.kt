package com.greencopper.core.conditions

import com.greencopper.core.conditions.conditionchecker.ParameterizedCondition
import com.greencopper.core.data.KiboSerializable
import com.greencopper.toolkit.versionprovider.BuildConfigProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

public class AppVersionCondition(
    buildConfigProvider: BuildConfigProvider,
) : ParameterizedCondition<AppVersionCondition.AppVersionData>() {

    private val appVersionCode: Int = buildConfigProvider.versionCode
    private val appVersionName: String = buildConfigProvider.versionName

    public companion object {
        public val key: ConditionInfo.Key = ConditionInfo.Key("Core.AppVersion", 1)
    }

    override fun checkWith(parameter: AppVersionData): Boolean {
        return when (parameter) {
            is AppVersionData.Exact -> {
                val (versionNames, versionCodes) = parameter.versions.partition {
                    it.toIntOrNull() == null
                }
                versionNames.contains(appVersionName) || versionCodes.contains(appVersionCode.toString()) == parameter.isInVersions
            }

            is AppVersionData.OlderThan -> {

                parameter.version.toIntOrNull()?.let { checkedVersion ->
                    return appVersionCode < checkedVersion
                } ?: run {
                    val version = appVersionName.split(".")
                    val checkedVersion = parameter.version.split(".")

                    val maxLength = maxOf(version.size, checkedVersion.size)

                    for (i in 0 until maxLength) {
                        val v = version.getOrNull(i)?.toIntOrNull() ?: 0
                        val c = checkedVersion.getOrNull(i)?.toIntOrNull() ?: 0

                        if (v < c) return true // version is older
                        if (v > c) return false // version is newer
                    }
                }

                return false // version is the same
            }
        }
    }

    override fun checkWithFlow(parameter: AppVersionData): Flow<Boolean> = flowOf(checkWith(parameter))

    override fun deserialize(conditionParameters: ConditionParameters): AppVersionData =
        KiboSerializable.decodeFromJsonElement(conditionParameters)

    @Serializable(with = AppVersionDataSerializer::class)
    public sealed class AppVersionData : KiboSerializable<AppVersionData> {

        override fun getSerializer(): KSerializer<AppVersionData> = serializer()

        @Serializable
        public data class Exact(
            val versions: List<String>,
            val isInVersions: Boolean,
        ) : AppVersionData()

        @Serializable
        public data class OlderThan(
            @SerialName("olderThan")
            val version: String,
        ) : AppVersionData()
    }

    private object AppVersionDataSerializer : KSerializer<AppVersionData> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("AppVersionDataSerializer", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: AppVersionData) {
            when (value) {
                is AppVersionData.Exact -> encoder.encodeSerializableValue(
                    AppVersionData.Exact.serializer(),
                    value
                )

                is AppVersionData.OlderThan -> encoder.encodeSerializableValue(
                    AppVersionData.OlderThan.serializer(),
                    value
                )
            }
        }

        override fun deserialize(decoder: Decoder): AppVersionData {
            val input = decoder as? JsonDecoder
                ?: throw SerializationException("This class can be loaded only by Json")
            val tree = input.decodeJsonElement() as? JsonObject
                ?: throw SerializationException("Expected JsonObject")

            val versions = tree["versions"]?.jsonArray
            val isInVersions = tree["isInVersions"]?.jsonPrimitive
            val olderThan = tree["olderThan"]?.jsonPrimitive

            val serializer = when {
                versions != null
                        && isInVersions != null -> AppVersionData.Exact.serializer()

                olderThan != null -> AppVersionData.OlderThan.serializer()
                else -> throw SerializationException("Couldn't decode correct sub-class of AppVersionData")
            }
            return input.json.decodeFromJsonElement(serializer, tree)
        }
    }
}
