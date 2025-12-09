package com.greencopper.interfacekit.color

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.interfacekit.color.repository.ColorRepository
import com.greencopper.interfacekit.color.repository.ConcreteColorRepository
import com.greencopper.interfacekit.ui.compose.mockColors
import com.greencopper.testmocks.*
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import de.mannodermaus.junit5.compose.createComposeExtension
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import android.graphics.Color as ColorAndroid
import androidx.compose.ui.graphics.Color as ColorCompose

@Disabled("Frequently fails on github. Error message: Instrumentation run failed due to keyDispatchingTimedOut")
@OptIn(ExperimentalTestApi::class)
internal class SelectableColorComposableTest {

    @JvmField
    @RegisterExtension
    @ExperimentalTestApi
    val extension = createComposeExtension()

    private val context = InstrumentationRegistry.getInstrumentation().context
    private val colorJson: String =
        context.assets.open("colors/colorsOverride-config.json").bufferedReader()
            .use { it.readText() }

    private lateinit var pressableColor: TestSelectableColor

    @BeforeEach
    internal fun setUp() {
        Toolkit.setupTest(applicationContext = context)
        val colorRepository = ConcreteColorRepository()
        colorRepository.loadColors(
            App.resolve<Json>().decodeFromString(
                ColorsConfiguration.serializer(),
                colorJson
            )
        )
        bindSingleton<ColorRepository>(colorRepository)

        pressableColor = TestSelectableColor(InterfaceKitColor)
    }

    @Test
    fun accessColors_shouldReturnColors() {
        extension.use {
            setContent {

                CompositionLocalProvider(
                    mockColors(
                        mapOf(
                            listOf("interfaceKit", "test", "normal") to ColorCompose.Red,
                            listOf("interfaceKit", "test", "selected") to ColorCompose.Blue,
                        )
                    )
                ) {
                    pressableColor.normal shouldBe ColorCompose.Red
                    pressableColor.selected shouldBe ColorCompose.Blue
                }
            }
        }
    }

    @Test @Disabled
    fun accessUnknownColors_shouldReturnDefaultColors() {
        extension.use {
            setContent {

                CompositionLocalProvider(
                    mockColors(
                        mapOf(
                            listOf("interfaceKit", "test", "foo") to ColorCompose.Red,
                            listOf("interfaceKit", "test", "bar") to ColorCompose.Blue,
                        )
                    )
                ) {
                    pressableColor.normal shouldBe ColorCompose(ColorAndroid.RED)
                    pressableColor.selected shouldBe ColorCompose(ColorAndroid.BLUE)
                }
            }
        }
    }

    internal object InterfaceKitColor : UIColor() {
        override val level: String = "interfaceKit"
    }

    class TestSelectableColor(parent: UIColor) : SelectableColorComposable(parent) {
        override val level: String = "test"
        override val normalDefault: () -> Color = { Color(ColorAndroid.RED, null) }
        override val selectedDefault: () -> Color = { Color(ColorAndroid.BLUE, null) }
    }
}
