package com.greencopper.interfacekit.editorial

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.editorial.repository.ConcreteEditorialPageRepository
import com.greencopper.interfacekit.navigation.feature.FeatureInitializerException
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.testmocks.bindProvider
import com.greencopper.testmocks.core.MockLocalizationService
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.encodeToJsonElement
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.*
import java.io.File

internal class EditorialPageInitializerTest {

    init {
        Toolkit.setupTest()
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())
    }

    private val json: Json = App.resolve()
    private val context = InstrumentationRegistry.getInstrumentation().context
    private val localization = MockLocalizationService(
        { "Hello" }, { _, _ -> "Hello"}, { "Hello" }
    )
    private val repository = ConcreteEditorialPageRepository(localization)
    private val parentDirectory = context.getDir("directory", Context.MODE_PRIVATE)

    private val initializer: EditorialPageInitializer
        get() = EditorialPageInitializer(repository)

    private val params = EditorialPageData(
        fileName = "file_name_to_localize",
        analytics = ScreenNameAnalytics("analytics_screen_name")
    )

    @BeforeEach
    fun beforeEach() {
        parentDirectory.mkdirs()
        repository.setContentDirectoryPath(parentDirectory.path)
    }

    @AfterEach
    fun afterEach() {
        parentDirectory.delete()
    }

    @Test
    fun whenGettingLayout_withoutParams_shouldThrow() {
        assertThrows<FeatureInitializerException.NoParametersProvidedException> {
            initializer.getLayout(null)
        }
    }

    @Test
    fun whenGettingLayout_withEmptyParams_shouldThrow() {
        assertThrows<FeatureInitializerException.ParametersDecodeFailed> {
            initializer.getLayout(JsonNull)
        }
    }

    @Test
    fun whenGettingLayout_withProperParams_shouldGetLayout() {
        val file = File(parentDirectory, localization.getStringFromRepository("")!!).apply { createNewFile() }

        val layout = initializer.getLayout(json.encodeToJsonElement(params))
        Assertions.assertThat(layout).isNotNull

        file.delete()
    }

    @Test
    fun whenGettingRedirectionHash_withoutParams_shouldDefault() {
        val redirectionHash = initializer.redirectionHashFor(null)
        Assertions.assertThat(redirectionHash).isEqualTo(RedirectionHash(EditorialPageInitializer.key))
    }

    @Test
    fun whenGettingRedirectionHash_withWrongParams_shouldGetDefault() {
        val redirectionHash = initializer.redirectionHashFor(JsonNull)
        Assertions.assertThat(redirectionHash).isEqualTo(RedirectionHash(EditorialPageInitializer.key))
    }

    @Test
    fun whenGettingRedirectionHash_withProperParams_shouldGetHash() {
        val redirectionHash = initializer.redirectionHashFor(json.encodeToJsonElement(params))
        Assertions.assertThat(redirectionHash).isNotNull
    }
}
