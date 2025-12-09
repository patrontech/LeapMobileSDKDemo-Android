package com.greencopper.interfacekit.widgets.viewmodel.widgetcollection

import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import com.greencopper.interfacekit.widgets.ui.widgetcollection.integration.WidgetCollectionView
import com.greencopper.testmocks.interfacekit.MockWidgetParameters
import com.greencopper.testmocks.interfacekit.MockWidgetResolver
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.serialization.json.JsonArray
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class WidgetCollectionViewModelTest {
    private val widgetResolver = MockWidgetResolver()
    private val classUnderTest = WidgetCollectionViewModel(widgetResolver)

    private val widgetInfos: List<WidgetCollectionConfiguration.Instance.WidgetInfo> = listOf(
        WidgetCollectionConfiguration.Instance.WidgetInfo(
            WidgetCollectionConfiguration.Instance.WidgetKey(name = "testKey", version = 1),
            JsonArray(emptyList()),
        )
    )

    init {
        Toolkit.setupTest()
    }

    @Test
    fun getWidgetItems_whenParametersIncorrect() {
        widgetResolver.widgetParameters = null
        assertThat(classUnderTest.getWidgetItems(widgetInfos).isEmpty()).isTrue
    }

    @Test
    fun getWidgetItems_whenParametersCorrect() {
        widgetResolver.widgetParameters = MockWidgetParameters()
        val expected = listOf(
            WidgetCollectionView.WidgetItem(
                key = WidgetCollectionConfiguration.Instance.WidgetKey(
                    name = "testKey",
                    version = 1
                ),
                params = MockWidgetParameters()
            )
        )
        assertThat(classUnderTest.getWidgetItems(widgetInfos))
            .usingRecursiveComparison()
            .isEqualTo(expected)

        // check second time
        assertThat(classUnderTest.getWidgetItems(widgetInfos))
            .usingRecursiveComparison()
            .isEqualTo(expected)
    }
}