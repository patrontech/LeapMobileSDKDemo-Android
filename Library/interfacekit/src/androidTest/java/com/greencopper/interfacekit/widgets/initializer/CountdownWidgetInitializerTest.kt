package com.greencopper.interfacekit.widgets.initializer

import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.timezone.TimezoneProvider
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.repository.ColorRepository
import com.greencopper.interfacekit.textstyle.subsystem.TextStyleRepository
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.countdownwidget.CountdownWidgetLayout
import com.greencopper.testmocks.*
import com.greencopper.testmocks.core.MockLocalizationService
import com.greencopper.testmocks.core.MockTimezoneProvider
import com.greencopper.testmocks.interfacekit.*
import com.greencopper.toolkit.Toolkit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.time.ZoneId
import java.time.ZonedDateTime

internal class CountdownWidgetInitializerTest {

    private lateinit var initializer: CountdownWidgetInitializer
    private val mockZoneId = ZoneId.of("America/New_York")
    private val timezoneProvider = MockTimezoneProvider(mockZoneId)

    @BeforeEach
    fun beforeEach() {
        Toolkit.setupTest()

        initializer = CountdownWidgetInitializer()
        bindSingleton<TimezoneProvider>(timezoneProvider)
    }

    @Test
    fun resolveLayout_shouldGetLayout() {
        //given
        val context = ContextThemeWrapper(
            InstrumentationRegistry.getInstrumentation().targetContext,
            R.style.Base_Theme_MaterialComponents
        )
        bindSingleton<ColorRepository>(MockColorRepository())
        bindSingleton<TextStyleRepository>(MockTextStyleRepository())
        bindSingleton<LocalizationService>(MockLocalizationService())

        //when
        val result = initializer.resolveLayout(context)

        //then
        assertThat(result).isInstanceOf(CountdownWidgetLayout::class.java)
    }

    @Test
    fun resolveParams_withCorrectParams_shouldGetDeserializedParams() {
        //given
        val data = CountdownWidgetParameters(
            endDateString = dateTimeFormatter.format(ZonedDateTime.now().withZoneSameInstant(mockZoneId)),
            showDate = true,
            title = "CREAMFIELD NORTH",
            CountdownWidgetParameters.AfterCountdown(
                title = "THE EVENT HAS ENDED",
                subtitle = "THANK YOU FOR WATCHING THE COUNTDOWN GO TO ZERO",
            ),
            backgroundColor = "#DE3586",
            backgroundImage = "mediaimage_20240731045543_f0044350.jpeg",
            textColor = "#FFFFFF",
            digitBackgroundColor = "#803A3A3A",
        )

        //when
        val result = initializer.resolveParams(data.encodeToJsonElement())

        //then
        assertThat(result).isInstanceOf(CountdownWidgetParameters::class.java)
        assertThat(result).isEqualTo(data)
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
        testKiboSerializable(
            CountdownWidgetParameters(
                endDateString = dateTimeFormatter.format(ZonedDateTime.now().withZoneSameInstant(mockZoneId)),
                showDate = true,
                title = "CREAMFIELD NORTH",
                CountdownWidgetParameters.AfterCountdown(
                    title = "THE EVENT HAS ENDED",
                    subtitle = "THANK YOU FOR WATCHING THE COUNTDOWN GO TO ZERO",
                ),
                backgroundColor = "#DE3586",
                backgroundImage = "mediaimage_20240731045543_f0044350.jpeg",
                textColor = "#FFFFFF",
                digitBackgroundColor = "#803A3A3A",
            )
        )

    }
}
