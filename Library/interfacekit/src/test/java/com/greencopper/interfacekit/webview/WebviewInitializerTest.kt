package com.greencopper.interfacekit.webview

import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.navigation.feature.FeatureInitializerException
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.webview.data.WebViewData
import com.greencopper.interfacekit.webview.data.WebViewNewWindowMode
import com.greencopper.testmocks.*
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.toolkit.Toolkit
import io.mockk.every
import io.mockk.mockkClass
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class WebviewInitializerTest {

    private lateinit var webInitializer: WebviewInitializer
    private val baseUrl = "https://greencopper.com"
    private val notAnUrl = "greencoppercom"
    private val localStorageMockked = mockkClass(LocalStorage::class)
    private val localizationServiceMockked = mockkClass(LocalizationService::class)

    init {
        Toolkit.setupTest()
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())
    }

    @BeforeEach
    fun setupEach() {
        every { localStorageMockked.replaceUrlParameters(baseUrl) } returns baseUrl
        every { localizationServiceMockked.getString(baseUrl) } returns baseUrl
        every { localStorageMockked.replaceUrlParameters(notAnUrl) } returns notAnUrl
        every { localizationServiceMockked.getString(notAnUrl) } returns notAnUrl
        webInitializer = WebviewInitializer(localStorageMockked, localizationServiceMockked)
    }

    @Test
    fun getLayout_withoutParams_shouldThrow() {
        assertThrows<FeatureInitializerException.NoParametersProvidedException> {
            webInitializer.getLayout(null)
        }
    }

    @Test
    fun getLayout_withWrongParams_shouldThrow() {
        val params = buildJsonObject { put("testKey", "testValue") }
        assertThrows<FeatureInitializerException.ParametersDecodeFailed> {
            webInitializer.getLayout(params)
        }
    }

    @Test
    fun getLayout_withCorrectParams_shouldSucceed() {
        val webViewData = WebViewData(
            baseUrl,
            WebViewNewWindowMode.External,
            ScreenNameAnalytics("TestWebview"),
        )
        val params = webViewData.encodeToJsonElement()
        val layout = webInitializer.redirectionHashFor(params)
        assertThat(layout).isNotNull
    }

    @Test
    fun getLayout_withWronglyFormattedUrlAfterReplace_shouldSucceed() {
        mockBundleConstructor()
        val webViewData = WebViewData(
            notAnUrl,
            WebViewNewWindowMode.Present,
            ScreenNameAnalytics("TestWebview")
        )
        val params = webViewData.encodeToJsonElement()
        val layout = webInitializer.getLayout(params)
        assertThat(layout).isNotNull
    }

    @Test
    fun getRedirectionHash_withoutParams_shouldGetDefault() {
        val hash = webInitializer.redirectionHashFor(null)
        assertThat(hash).isEqualTo(RedirectionHash(WebviewInitializer.key))
    }

    @Test
    fun getRedirectionHash_withWrongParams_shouldGetDefault() {
        val params = buildJsonObject { put("testKey", "testValue") }
        val hash = webInitializer.redirectionHashFor(params)
        assertThat(hash).isEqualTo(RedirectionHash(WebviewInitializer.key))
    }

    @Test
    fun getRedirectionHash_withCorrectParams_shouldSucceed() {
        val webViewData = WebViewData(
            baseUrl,
            WebViewNewWindowMode.Push,
            ScreenNameAnalytics("TestWebview")
        )
        val params = webViewData.encodeToJsonElement()
        val hash = webInitializer.redirectionHashFor(params)
        assertThat(hash).isNotNull
    }
}
