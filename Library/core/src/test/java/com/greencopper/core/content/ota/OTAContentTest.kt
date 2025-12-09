package com.greencopper.core.content.ota

import com.greencopper.core.data.KiboSerializable
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime

internal class OTAContentTest {

    init {
        Toolkit.setupTest()
    }

    @Test
    fun whenSerialized_canDeserialize() {
        val otaContentBeta =
            OTAContent("https://someAddress", "default", "2020-03-20T10:20:24'Z'", 1, "beta", 1)

        val restoredOta =
            KiboSerializable.decodeFromString<OTAContent>(otaContentBeta.encodeToString())

        assertThat(otaContentBeta).isEqualTo(restoredOta)
    }

    @Test
    fun whenGettingVersionType_inBeta() {
        val otaContentBeta =
            OTAContent("https://someAddress", "default", "2020-03-20T10:20:24'Z'", 1, "draft", 1)

        assertThat(otaContentBeta.versionType).isEqualTo(OTAContent.Type.Draft)
    }

    @Test
    fun whenGettingVersionType_inRelease() {
        val otaContentRelease =
            OTAContent("https://someAddress", "default", "2020-03-20T10:20:24'Z'", 1, "release", 1)

        assertThat(otaContentRelease.versionType).isEqualTo(OTAContent.Type.Release)
    }

    @Test
    fun whenGettingVersionType_inProgress() {
        val otaContentInProgress =
            OTAContent(
                "https://someAddress",
                "default",
                "2020-03-20T10:20:24'Z'",
                1,
                "in_progress",
                1
            )

        assertThat(otaContentInProgress.versionType).isEqualTo(OTAContent.Type.InProgress)
    }

    @Test
    fun whenGettingVersionType_isUnknown() {
        val otaContentNull =
            OTAContent(
                "https://someAddress",
                "default",
                "2020-03-20T10:20:24'Z'",
                1,
                "otherType",
                1
            )

        assertThat(otaContentNull.versionType).isNull()
    }

    @Test
    fun whenGettingCreationDate_properlyFormatted_shouldBeParsed() {
        val otaContentSuccessDate =
            OTAContent(
                "https://someAddress",
                "default",
                "2020-03-20T10:20:24Z[UTC]",
                1,
                "release",
                1
            )
        assertThat(otaContentSuccessDate.creationDate).isNotNull
        val date = ZonedDateTime.of(
            2020,
            3,
            20,
            10,
            20,
            24,
            0,
            ZoneId.of("UTC")
        )
        assertThat(otaContentSuccessDate.creationDate).isEqualTo(date)
    }

    @Test
    fun whenGettingCreationDate_nullDate_shouldBeNull() {
        val otaContentNullDate =
            OTAContent("https://someAddress", "default", null, 1, "release", 1)
        assertThat(otaContentNullDate.creationDate).isNull()
    }

    @Test
    fun whenGettingCreationDate_wrongFormatDate_shouldBeNull() {
        val otaContentWrongFormatDate =
            OTAContent(
                "https://someAddress",
                "default",
                "2020-grjwiogrw-20T10:20:24",
                1,
                "release",
                1
            )
        assertThat(otaContentWrongFormatDate.creationDate).isNull()
    }
}
