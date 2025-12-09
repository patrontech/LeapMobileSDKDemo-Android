package com.greencopper.core.data

import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.ResolveException
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class KiboSerializableTest {

    init {
        Toolkit.setupTest()
    }

    @Serializable
    private data class SampleTitleKiboSerializable(val title: String): KiboSerializable<SampleTitleKiboSerializable> {
        override fun getSerializer(): KSerializer<SampleTitleKiboSerializable> = serializer()
    }

    @Serializable
    private data class SampleIntKiboSerializable(val version: Int): KiboSerializable<SampleIntKiboSerializable> {
        override fun getSerializer(): KSerializer<SampleIntKiboSerializable> = serializer()
    }

    private open class WrongCastSerializable(val title: String, val version: Int): KiboSerializable<SampleTitleKiboSerializable> {
        override fun getSerializer(): KSerializer<SampleTitleKiboSerializable> = SampleTitleKiboSerializable.serializer()
    }

    private class WrongSerializableSerializer : KSerializer<WrongSerializable> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("serialName")
        override fun deserialize(decoder: Decoder): WrongSerializable  = throw SerializationException()
        override fun serialize(encoder: Encoder, value: WrongSerializable) = throw SerializationException()
    }

    private open class WrongSerializable(val serialize: String): KiboSerializable<WrongSerializable> {
        override fun getSerializer(): KSerializer<WrongSerializable> = WrongSerializableSerializer()
    }

    @Serializable
    private data class ThrowWithAppResolve(val text: String): KiboSerializable<ThrowWithAppResolve> {
        override fun getSerializer(): KSerializer<ThrowWithAppResolve> = serializer()
        override fun jsonProvider(): Json {
            App.resolve<ThrowWithAppResolve>()
            return super.jsonProvider()
        }
    }

    @Test
    fun kiboSerialization_toJsonElement_whenAppDependenciesIsNotSetup_shouldThrow() {
        val instance = ThrowWithAppResolve("test instance")
        assertThrows<ResolveException> { instance.encodeToJsonElement() }
    }

    @Test
    fun kiboSerialization_toString_whenAppDependenciesIsNotSetup_shouldThrow() {
        val instance = ThrowWithAppResolve("test instance")
        assertThrows<ResolveException> { instance.encodeToString() }
    }

    @Test
    fun kiboSerializationAndDeserialization_shouldBeEqual() {
        val instance = SampleTitleKiboSerializable("test instance")
        val serializedInstance = instance.encodeToJsonElement()
        assertThat(instance).isEqualTo(
            KiboSerializable.decodeFromJsonElement<SampleTitleKiboSerializable>(
                serializedInstance
            )
        )
    }

    @Test
    fun stringSerializationAndDeserialization_shouldBeEqual() {
        val instance = SampleTitleKiboSerializable("test instance")
        val serializedInstance = instance.encodeToString()
        assertThat(instance).isEqualTo(
            KiboSerializable.decodeFromString<SampleTitleKiboSerializable>(
                serializedInstance
            )
        )
    }

    @Test
    fun kiboSerializationAndDeserialization_shouldThrowWhenTargetingDifferentClass() {
        val instance = SampleTitleKiboSerializable("test instance")
        val serializedInstance = instance.encodeToJsonElement()
        assertThrows<SerializationException> {
            KiboSerializable.decodeFromJsonElement<SampleIntKiboSerializable>(serializedInstance)
        }
    }

    @Test
    fun stringSerializationAndDeserialization_shouldThrowWhenTargetingDifferentClass() {
        val instance = SampleTitleKiboSerializable("test instance")
        val serializedInstance = instance.encodeToString()
        assertThrows<SerializationException> {
            KiboSerializable.decodeFromString<SampleIntKiboSerializable>(serializedInstance)
        }
    }

    @Test
    fun kiboSerialization_shouldThrowWhenSerializationClassIsWrong() {
        val instance = WrongSerializable("serialize")
        assertThrows<SerializationException> {
            instance.encodeToJsonElement()
        }
    }

    @Test
    fun stringSerialization_shouldThrowWhenSerializationClassIsWrong() {
        val instance = WrongSerializable("serialize")
        assertThrows<SerializationException> {
            instance.encodeToString()
        }
    }

    @Test
    fun kiboSerialization_shouldThrowWhenDefinitionClassIsWrong() {
        val instance = WrongCastSerializable("test instance", 1)
        assertThrows<ClassCastException> {
            instance.encodeToJsonElement()
        }
    }

    @Test
    fun stringSerialization_shouldThrowWhenDefinitionClassIsWrong() {
        val instance = WrongCastSerializable("test instance", 1)
        assertThrows<ClassCastException> {
            instance.encodeToString()
        }
    }
}