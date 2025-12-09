package com.greencopper.core.content.ota

import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.UnknownHostException

internal class OTAManagerExceptionTest {

    init {
        Toolkit.setupTest()
    }

    @Test
    fun missingUrlExceptionMessage() {
        val otaContent = OTAContent(null, "default", "2020-03-20T10:20:24'Z'", 1, "release", 1)
        val exception = OTAManagerException.MissingUrlException(otaContent)
        assertThat(exception).hasMessage(
            "[OTAManagerException] Couldn't retrieve OTAContent with missing URL: " +
                "OTAContent(url=null, project=default, dateString=2020-03-20T10:20:24'Z', version=1, typeString=release, schema=1)"
        )
    }

    @Test
    fun downloadFailedExceptionMessage() {
        val exception = OTAManagerException.DownloadFailedException(UnknownHostException())
        assertThat(exception).hasMessage("[OTAManagerException] Couldn't download OTA content: java.net.UnknownHostException")
    }
}