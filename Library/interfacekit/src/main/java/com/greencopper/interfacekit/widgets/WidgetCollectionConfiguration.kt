package com.greencopper.interfacekit.widgets

import com.greencopper.core.conditions.ConditionSet
import com.greencopper.core.conditions.Conditioned
import com.greencopper.core.data.KiboSerializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

public typealias JsonWidgetParameters = JsonElement
@Serializable
public data class WidgetCollectionConfiguration(val instances: Map<String, Instance>): KiboSerializable<WidgetCollectionConfiguration> {

    override fun getSerializer(): KSerializer<WidgetCollectionConfiguration> = serializer()

    @Serializable
    public data class Instance(val header: HeaderInfo? = null, val widgets: List<WidgetInfo>) {

        @Serializable
        public data class WidgetInfo(
            val key: WidgetKey,
            val params: JsonWidgetParameters,
            override val conditionSet: ConditionSet? = null
        ) : Conditioned

        @Serializable
        public data class HeaderInfo(
            val imageName: String,
            val ratio: Float,
            val cornerRadius: Int? = null,
            val shadow: Boolean? = null,
            val accessibilityLabel: String? = null
        )

        @Serializable
        public data class WidgetKey(val name: String, val version: Int)
    }
}
