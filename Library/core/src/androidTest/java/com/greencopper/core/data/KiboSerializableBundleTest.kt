package com.greencopper.core.data

import androidx.core.os.bundleOf
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class KiboSerializableBundleTest {
    private val bundleKey1 = "key1"
    private val bundleKey2 = "key2"

    init {
        Toolkit.setupTest()
    }

    @Serializable
    private data class SampleTitleJsonKiboSerializable(val title: String): KiboSerializable<SampleTitleJsonKiboSerializable> {
        override fun getSerializer(): KSerializer<SampleTitleJsonKiboSerializable> = serializer()
    }

    @Test
    fun bundleSerialization_shouldAddContentUnderKey() {
        val instance = SampleTitleJsonKiboSerializable("test instance")
        val bundle = bundleOf()
        bundle.putKiboSerializable(bundleKey1, instance)
        bundle.putKiboSerializable(bundleKey2, null)
        assertThat(bundle.size()).isEqualTo(2)
        assertThat(bundle.get(bundleKey1)).isEqualTo(instance.encodeToString())
        assertThat(bundle.get(bundleKey2)).isNull()
    }

    @Test
    fun bundleDeserialization_shouldReturnContentUnderKey() {
        val instance = SampleTitleJsonKiboSerializable("test instance")
        val bundle = bundleOf()
        bundle.putKiboSerializable(bundleKey1, instance)
        bundle.putKiboSerializable(bundleKey2, null)
        assertThat(bundle.getKiboSerializable<SampleTitleJsonKiboSerializable>(bundleKey1)).isEqualTo(instance)
        assertThat(bundle.getKiboSerializable<SampleTitleJsonKiboSerializable>(bundleKey2)).isEqualTo(null)
    }
}
