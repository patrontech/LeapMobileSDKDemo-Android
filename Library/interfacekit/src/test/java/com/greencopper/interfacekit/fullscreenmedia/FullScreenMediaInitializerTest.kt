package com.greencopper.interfacekit.fullscreenmedia

import android.webkit.MimeTypeMap
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.navigation.feature.FeatureInitializerException
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.testmocks.*
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.toolkit.Toolkit
import io.mockk.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class FullScreenMediaInitializerTest {

    private lateinit var fullScreenMediaInitializer: FullScreenMediaInitializer
    private val fileName: String = "pouet.png"
    private val analytics = ScreenNameAnalytics("TestFullScreenMedia")

    init {
        Toolkit.setupTest()
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())

        mockkStatic(MimeTypeMap::class)
        every { MimeTypeMap.getSingleton() } returns mockk()
        every { MimeTypeMap.getFileExtensionFromUrl(any()) } returns "png"
        every { MimeTypeMap.getSingleton().getMimeTypeFromExtension(any()) } returns "image/png"
    }

    @BeforeEach
    fun setupEach() {
        fullScreenMediaInitializer = FullScreenMediaInitializer()
    }

    @Test
    fun getLayout_withoutParams_shouldThrow() {
        assertThrows<FeatureInitializerException.NoParametersProvidedException> {
            fullScreenMediaInitializer.getLayout(null)
        }
    }

    @Test
    fun getLayout_withWrongParams_shouldThrow() {
        val params = buildJsonObject { put("testKey", "testValue") }
        assertThrows<FeatureInitializerException.ParametersDecodeFailed> {
            fullScreenMediaInitializer.getLayout(params)
        }
    }

    @Test
    fun getLayout_withCorrectParams_shouldSucceed() {
        mockBundleConstructor()

        val params = FullScreenMediaData(
            fileName,
            analytics
        ).encodeToJsonElement()
        assertThat(fullScreenMediaInitializer.getLayout(params)).isNotNull
    }

    @Test
    fun getRedirectionHash_withoutParams_shouldReturnDefault() {
        val hash = fullScreenMediaInitializer.redirectionHashFor(null)
        assertThat(hash).isEqualTo(RedirectionHash(FullScreenMediaInitializer.key))
    }

    @Test
    fun getRedirectionHash_withWrongParams_shouldReturnDefault() {
        val params = buildJsonObject { put("testKey", "testValue") }
        val hash = fullScreenMediaInitializer.redirectionHashFor(params)
        assertThat(hash).isEqualTo(RedirectionHash(FullScreenMediaInitializer.key))
    }

    @Test
    fun getRedirectionHash_withCorrectParams_shouldSucceed() {
        val params = FullScreenMediaData(
            "name",
            analytics
        ).encodeToJsonElement()
        val hash = fullScreenMediaInitializer.redirectionHashFor(params)
        assertThat(hash).isNotNull
    }
}
