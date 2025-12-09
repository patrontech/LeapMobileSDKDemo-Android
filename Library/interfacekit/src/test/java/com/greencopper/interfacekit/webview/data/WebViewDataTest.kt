package com.greencopper.interfacekit.webview.data

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class WebViewDataTest {

    init {
        Toolkit.setupTest()
    }

    @Test
    fun createWebViewData_shouldNotThrow() {
        assertDoesNotThrow {
            WebViewData("url", WebViewNewWindowMode.Present, ScreenNameAnalytics("screenName"))
        }
    }

    @Test
    fun serializeWebViewData_shouldNotThrow() {
        val webViewData = WebViewData("url", null, ScreenNameAnalytics("screenName"))
        assertDoesNotThrow {
            webViewData.encodeToString()
        }
    }

    @Test
    fun deserializeWebViewData_shouldNotThrow() {
        val webViewData = WebViewData("url", WebViewNewWindowMode.Push,  ScreenNameAnalytics("screenName"))
        val encodedWebViewData = webViewData.encodeToString()
        assertEquals(
            webViewData,
            KiboSerializable.decodeFromString<WebViewData>(encodedWebViewData)
        )
    }
}
