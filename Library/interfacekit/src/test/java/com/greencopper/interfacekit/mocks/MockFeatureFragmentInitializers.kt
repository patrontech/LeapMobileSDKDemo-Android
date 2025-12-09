package com.greencopper.interfacekit.mocks

import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.color.ScreenColor
import com.greencopper.interfacekit.navigation.feature.FeatureInitializer
import com.greencopper.interfacekit.navigation.feature.FeatureInitializerException
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.ui.fragment.ParameterizedFragment
import com.greencopper.interfacekit.ui.fragment.UnparameterizedFragment
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

internal class TestUnparameterizedFeatureFragmentInitializer : FeatureInitializer {

    override val featureKey: FeatureKey = key

    override fun getLayout(params: FeatureParams?): Layout {
        return TestUnparameterizedFragment()
    }

    override fun redirectionHashFor(params: FeatureParams?): RedirectionHash {
        return RedirectionHash(key, params.toString())
    }

    companion object {
        val key: FeatureKey =
            FeatureKey(
                "Test.ResolverUnparameterizedFragment",
                1
            )
    }
}

internal class TestParameterizedFeatureFragmentInitializer : FeatureInitializer {
    override val featureKey: FeatureKey = key

    override fun getLayout(params: FeatureParams?): Layout {
        if (params == null) {
            throw FeatureInitializerException.NoParametersProvidedException()
        }
        val parameter: TestParameter = try {
            App.resolve<Json>().decodeFromJsonElement(params)
        } catch (e: Exception) {
            throw FeatureInitializerException.ParametersDecodeFailed(params)
        }
        return TestParameterizedFragment(parameter)
    }

    override fun redirectionHashFor(params: FeatureParams?): RedirectionHash {
        return RedirectionHash(key, params.toString())
    }

    companion object {
        val key: FeatureKey =
            FeatureKey(
                "Test.ResolverParameterizedFragment",
                1
            )
    }
}

internal class TestParameterizedFragment : ParameterizedFragment<TestParameter> {
    constructor(parameter: TestParameter) : super(parameter)

    @Deprecated("System constructor access by reflection through FragmentFactory")
    constructor() : super(null)

    override val screenColor: ScreenColor?
        get() = null

    override fun restoreData(encodedData: String): TestParameter =
        KiboSerializable.decodeFromString(encodedData)
}

internal class TestUnparameterizedFragment : UnparameterizedFragment() {
    override val screenColor: ScreenColor?
        get() = null
}

@Serializable
internal data class TestParams(val pairs: Map<String, String>) {
    constructor(vararg pairs: Pair<String, String>) : this(pairs.toMap())

    fun toJsonObject(): JsonObject {
        return App.resolve<Json>().encodeToJsonElement(this).jsonObject
    }
}

@Serializable
internal data class TestParameter(val title: String, val version: Int) :
    KiboSerializable<TestParameter> {
    override fun getSerializer(): KSerializer<TestParameter> = serializer()
}
