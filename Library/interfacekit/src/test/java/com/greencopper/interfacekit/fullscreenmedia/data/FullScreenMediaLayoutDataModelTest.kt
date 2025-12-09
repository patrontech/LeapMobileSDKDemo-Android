package com.greencopper.interfacekit.fullscreenmedia.data

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.fullscreenmedia.FullScreenMediaData
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class FullScreenMediaLayoutDataModelTest {

    init {
        Toolkit.setupTest()
    }

    @Test
    fun createFullScreenMediaDataModel_shouldNotThrow() {
        assertDoesNotThrow {
            FullScreenMediaData("name", ScreenNameAnalytics("screenName"))
        }
    }

    @Test
    fun serializeFullScreenMediaDataModel_shouldNotThrow() {
        val fullScreenMediaDataModel =
            FullScreenMediaData("name", ScreenNameAnalytics("screenName"))
        assertDoesNotThrow {
            fullScreenMediaDataModel.encodeToString()
        }
    }

    @Test
    fun deserializeFullScreenMediaDataModel_shouldNotThrow() {
        val fullScreenMediaDataModel =
            FullScreenMediaData("name", ScreenNameAnalytics("screenName"))
        val encodedFullScreenMediaDataModel = fullScreenMediaDataModel.encodeToString()
        assertEquals(
            fullScreenMediaDataModel,
            KiboSerializable.decodeFromString<FullScreenMediaData>(
                encodedFullScreenMediaDataModel
            )
        )
    }

    @Test
    fun serializationFullScreenMediaDataModel_equals() {
        val fullScreenMediaDataModel =
            FullScreenMediaData("name", ScreenNameAnalytics("screenName"))
        val encodedFullScreenMediaDataModel = fullScreenMediaDataModel.encodeToString()
        val fullScreenMediaDataModel2 = KiboSerializable.decodeFromString<FullScreenMediaData>(
            encodedFullScreenMediaDataModel
        )
        assert(fullScreenMediaDataModel == fullScreenMediaDataModel2)
    }
}
