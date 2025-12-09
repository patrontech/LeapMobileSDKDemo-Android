package com.greencopper.interfacekit.webview.data

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class WebViewData(
    val url: String,
    val newWindowMode: WebViewNewWindowMode? = null,
    val analytics: ScreenNameAnalytics,
) : KiboSerializable<WebViewData> {

    override fun getSerializer(): KSerializer<WebViewData> = serializer()
}

@Serializable
public enum class WebViewNewWindowMode {
    @SerialName("push") Push,
    @SerialName("present") Present,
    @SerialName("external") External,
}
