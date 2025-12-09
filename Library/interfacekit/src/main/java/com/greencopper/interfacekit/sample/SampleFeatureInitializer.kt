package com.greencopper.interfacekit.sample

import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.navigation.feature.ParameterizedFeatureInitializer
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.sample.ui.SampleFragment
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

public class SampleFeatureInitializer : ParameterizedFeatureInitializer<SampleData>() {

    public companion object {
        public val key: FeatureKey = FeatureKey("InterfaceKit.Sample", 1)
    }

    override val featureKey: FeatureKey = key

    override fun decodeParams(params: FeatureParams): SampleData =
        KiboSerializable.decodeFromJsonElement(params)

    override fun layoutForParams(params: SampleData): Layout = SampleFragment(
        SampleLayoutData(
            text = params.text,
            imageName = params.imageName,
            redirectionHash = redirectionHashForParams(params)
        )
    )

    override fun redirectionHashForParams(params: SampleData): RedirectionHash =
        RedirectionHash(key, "${params.imageName} ${params.text}")
}

@Serializable
public data class SampleData(val text: String, val imageName: String)
    : KiboSerializable<SampleData> {
    override fun getSerializer(): KSerializer<SampleData> = serializer()
}

@Serializable
internal data class SampleLayoutData(
    val text: String,
    val imageName: String,
    val redirectionHash: RedirectionHash
) : KiboSerializable<SampleLayoutData> {
    override fun getSerializer(): KSerializer<SampleLayoutData> = serializer()
}
