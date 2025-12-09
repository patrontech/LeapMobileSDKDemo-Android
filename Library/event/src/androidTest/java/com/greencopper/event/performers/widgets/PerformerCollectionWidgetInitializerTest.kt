package com.greencopper.event.performers.widgets

import android.view.ContextThemeWrapper
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.event.R
import com.greencopper.event.performers.Performer
import com.greencopper.eventmocks.MockPerformerRepository
import com.greencopper.interfacekit.color.repository.ColorRepository
import com.greencopper.interfacekit.textstyle.subsystem.TextStyleRepository
import com.greencopper.interfacekit.widgets.initializer.ImageCollectionWidgetParameters
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.imagecollectionwidget.ImageCollectionWidgetLayout
import com.greencopper.testmocks.*
import com.greencopper.testmocks.interfacekit.MockColorRepository
import com.greencopper.testmocks.interfacekit.MockTextStyleRepository
import com.greencopper.testmocks.interfacekit.MockWidgetParams
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.testKiboSerializable
import com.greencopper.toolkit.Toolkit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class PerformerCollectionWidgetInitializerTest {

    private lateinit var initializer: PerformerCollectionWidgetInitializer
    private val performerRepository = MockPerformerRepository()

    @BeforeEach
    fun beforeEach() {
        Toolkit.setupTest()

        initializer = PerformerCollectionWidgetInitializer(performerRepository)
    }

    @Test
    fun resolveLayout_shouldGetLayout() {
        //given
        bindSingleton<ColorRepository>(MockColorRepository())
        bindSingleton<TextStyleRepository>(MockTextStyleRepository())
        val context = ContextThemeWrapper(
            InstrumentationRegistry.getInstrumentation().targetContext,
            R.style.Base_Theme_MaterialComponents
        )

        //when
        val result = initializer.resolveLayout(context)

        //then
        assertThat(result).isInstanceOf(ImageCollectionWidgetLayout::class.java)
    }

    @Test
    fun resolveParams_withExistingPerformers_shouldGetDeserializedParams() {
        //given
        performerRepository.performers = listOf(
            Performer(
                "p1",
                name = "name1",
                photos = listOf("photo11", "photo12"),
                order = 1,
            ),
            Performer(
                "p2",
                name = "name2",
                photos = listOf("photo21", "photo22"),
                order = 2,
            ),
            Performer(
                "p3",
                name = "name3",
                photos = listOf(),
                order = 3,
            )
        )

        val data = PerformerCollectionWidgetParameters(
            title = "title",
            performers = listOf("p2", "p3", "p1"),
            onPerformerTap = "routeLink",
        )

        //when
        val result = initializer.resolveParams(data.encodeToJsonElement()) as ImageCollectionWidgetParameters

        //then
        assertThat(result.title).isEqualTo(data.title)
        assertThat(result.items).hasSameSizeAs(data.performers)
        assertThat(result.items[0]).satisfies({
            it.label == "name1"
                    && it.accessibilityName == "name1"
                    && it.imageName == "photo11"
                    && it.onTap.routeLink == "${data.onPerformerTap}?performerId=\"p1\""
        })
        assertThat(result.items[1]).satisfies({
            it.label == "name3"
                    && it.accessibilityName == "name3"
                    && it.imageName == ""
                    && it.onTap.routeLink == "${data.onPerformerTap}?performerId=\"p3\""
        })
        assertThat(result.items[2]).satisfies({
            it.label == "name2"
                    && it.accessibilityName == "name2"
                    && it.imageName == "photo21"
                    && it.onTap.routeLink == "${data.onPerformerTap}?performerId=\"p2\""
        })
    }

    @Test
    fun resolveParams_withoutItems_shouldThrow() {
        assertThrows<WidgetException.ParametersDecodeFailed> {
            val data = PerformerCollectionWidgetParameters(
                title = "title",
                performers = listOf(),
                onPerformerTap = "routeLink",
            )

            initializer.resolveParams(data.encodeToJsonElement())
        }
    }

    @Test
    fun resolveParams_withNoPerformers_shouldGetDeserializedParams() {
        //given
        val data = PerformerCollectionWidgetParameters(
            title = "title",
            performers = listOf("p1", "p3"),
            onPerformerTap = "routeLink",
        )

        //when
        val result = initializer.resolveParams(data.encodeToJsonElement()) as ImageCollectionWidgetParameters

        //then
        assertThat(result.title).isEqualTo(data.title)
        assertThat(result.items).hasSize(0)
    }

    @Test
    fun resolveParams_withoutParams_shouldThrow() {
        assertThrows<WidgetException.NoParametersProvided> {
            initializer.resolveParams(null)
        }
    }

    @Test
    fun resolveParams_withBadParams_shouldThrow() {
        assertThrows<WidgetException.ParametersDecodeFailed> {
            initializer.resolveParams(MockWidgetParams("name").encodeToJsonElement())
        }
    }

    @Test
    fun serialize_deserialize_test() {
        val data = PerformerCollectionWidgetParameters(
            title = "title",
            performers = listOf("p1", "p3"),
            onPerformerTap = "routeLink",
        )
        testKiboSerializable(data)
    }

}

