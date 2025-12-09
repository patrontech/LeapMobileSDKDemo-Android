package com.greencopper.interfacekit.inbox

import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.inbox.ui.InboxFragment
import com.greencopper.interfacekit.navigation.feature.FeatureInitializerException
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.interfacekit.sample.SampleData
import com.greencopper.testmocks.*
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.toolkit.Toolkit
import io.mockk.unmockkAll
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class InboxInitializerTest {

    private lateinit var inboxInitializer: InboxInitializer
    private val inboxData = InboxData(
        null,
        "https://www.google.com/",
        "empty_state.png",
        null,
        ScreenNameAnalytics("inbox_screen")
    )
    private val sampleData = SampleData("sample", "image.png")

    @BeforeEach
    internal fun setUp() {
        Toolkit.setupTest()
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())
        mockBundleConstructor()
        inboxInitializer = InboxInitializer()
    }

    @AfterEach
    internal fun tearDown() {
        unmockkAll()
    }

    @Test
    @DisplayName("Given valid inbox data, When getLayout is called, Then a valid layout should be returned")
    fun getLayoutShouldSucceed() {
        val featureParams = inboxData.encodeToJsonElement()
        assertThat(inboxInitializer.getLayout(featureParams)).isInstanceOf(InboxFragment::class.java)
    }

    @Test
    @DisplayName("Given valid inbox data, When redirectionHashFor is called, Then a valid redirection hash should be returned")
    fun redirectionHashForShouldSucceed() {
        val featureParams = inboxData.encodeToJsonElement()
        val redirectionHash = inboxInitializer.redirectionHashFor(featureParams)
        assertThat(redirectionHash.featureKey).isEqualTo(InboxInitializer.key)
        assertThat(redirectionHash.identifier).isEqualTo(inboxData.analytics.screenName)
    }

    @Test
    @DisplayName("Given invalid data, When getLayout is called, Then an exception should be thrown")
    fun getLayoutShouldFail() {
        val featureParams = sampleData.encodeToJsonElement()
        assertThrows<FeatureInitializerException.ParametersDecodeFailed> {
            inboxInitializer.getLayout(featureParams)
        }
    }

    @Test
    @DisplayName("Given invalid data, When redirectionHashFor is called, Then a feature key only hash should be returned")
    fun redirectionHashForShouldFallbackToKey() {
        val featureParams = sampleData.encodeToJsonElement()
        val redirectionHash = inboxInitializer.redirectionHashFor(featureParams)
        assertThat(redirectionHash.featureKey).isEqualTo(InboxInitializer.key)
        assertThat(redirectionHash.identifier).isNull()
    }
}
