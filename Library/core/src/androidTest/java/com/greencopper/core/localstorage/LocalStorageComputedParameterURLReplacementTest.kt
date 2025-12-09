package com.greencopper.core.localstorage

import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.core.content.archive.ContentArchive
import com.greencopper.core.content.initialcontent.RunConfiguration
import com.greencopper.core.content.manager.Content
import com.greencopper.core.content.ota.OTAContent
import com.greencopper.core.notification.repository.NotificationRepository
import com.greencopper.core.notification.repository.RegisterResult
import com.greencopper.testmocks.core.MockContentManager
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.toolkit.MockBuildConfigProvider
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.LazyResolver
import com.greencopper.toolkit.di.resolver.resolve
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.io.File

internal class LocalStorageComputedParameterURLReplacementTest {

    private val context = InstrumentationRegistry.getInstrumentation().context

    init {
        Toolkit.setupTest(applicationContext = context)
    }

    private val initialContent = RunConfiguration.Content(
        "abc.zip",
        "abc123",
        1,
        53,
        "awesome"
    )

    private val mockContentManager = MockContentManager(
        currentContentValue = {
            Content(
                ContentArchive(File("archive"), "123abc"),
                93,
                1,
                "amazing",
                OTAContent.Type.Draft,
            )
        },
        contentToApplyValue = {
            Content(
                ContentArchive(File("archive"), "abc123"),
                22,
                1,
                "cool",
                OTAContent.Type.Draft,
            )
        }
    )

    private val computedContainer = ComputedPropertiesLocalStorageContainer(
        TestLocalStorageContainer(),
        context,
        initialContent,
        LazyResolver.adhoc(mockContentManager),
        LazyResolver.adhoc(TestNotificationManager()),
        MockBuildConfigProvider(),
        App.resolve(),
    )
    private val localStorage = LocalStorage("awesome", computedContainer)

    @Test
    fun testComputedKeyExists() {
        val key = LocalStorageKey("%/version")
        assertThat(computedContainer.keyExists(key)).isTrue
    }

    @Test
    fun testComputedKeyDoesNotExist() {
        val key = LocalStorageKey("%/doesNotExist")
        assertThat(computedContainer.keyExists(key)).isFalse
    }

    @Test
    fun testStandardKeyDoesNotExist() {
        val key = LocalStorageKey("foo/bar")
        assertThat(computedContainer.keyExists(key)).isFalse
    }

    @Test
    fun testGetJSON_withMissingKey() {
        val key = LocalStorageKey("%/doesNotExist")
        assertThrows<IllegalArgumentException> {
            computedContainer.getJSON(key)
        }
    }

    @Test
    fun testSetJSON_withComputedKey() {
        val key = LocalStorageKey("%/version")
        assertThrows<IllegalArgumentException> {
            computedContainer.setJSON(key, "99")
        }
    }

    @Test
    fun testVersion() {
        val url = "u?v={%/version}"
        assertThat(localStorage.replaceUrlParameters(url)).isNotBlank
    }

    @Test
    fun testPlatform() {
        val url = "u?p={%/platform}"
        assertThat(localStorage.replaceUrlParameters(url)).isEqualTo("u?p=Android")
    }

    @Test
    fun testDevice() {
        val url = "u?d={%/device}"
        assertThat(localStorage.replaceUrlParameters(url)).isNotBlank
    }

    @Test
    fun testLocationPermissions() {
        val url = "u?p={%/locationPermission}"
        assertThat(localStorage.replaceUrlParameters(url)).isNotNull
    }

    @Test
    fun testRegisteredForPush() {
        val url = "u?p={%/registeredForPush}"
        assertThat(localStorage.replaceUrlParameters(url)).isNotNull
    }

    @Test
    fun testAttPermission() {
        val url = "u?p={%/attPermission}"
        assertThat(localStorage.replaceUrlParameters(url)).isEqualTo("u?p=inapplicable")
    }

    @Test
    fun testCurrentContent() {
        val url = "u?v={%/currentContent}"
        assertThat(localStorage.replaceUrlParameters(url)).isEqualTo("u?v=s1v93_amazing")
    }

    @Test
    fun testCurrentContentVersion() {
        val url = "u?v={%/currentContentVersion}"
        assertThat(localStorage.replaceUrlParameters(url)).isEqualTo("u?v=93")
    }

    @Test
    fun testContentToApply() {
        val url = "u?v={%/contentToApply}"
        assertThat(localStorage.replaceUrlParameters(url)).isEqualTo("u?v=s1v22_cool")
    }

    @Test
    fun testInitialContentConfig() {
        val url = "u?v={%/initialContentConfig}"
        assertThat(localStorage.replaceUrlParameters(url)).isEqualTo("u?v=s1v53_awesome")
    }

    @Test
    fun testCurrentContentVersionWithType() {
        val url = "u?v={%/currentContentVersionWithType}"
        assertThat(localStorage.replaceUrlParameters(url)).isEqualTo("u?v=93+%28draft%29")
    }
}

private class TestNotificationManager : NotificationRepository {
    override var token: String? = null

    override fun onNewToken(newToken: String) {
        throw NotImplementedError()
    }

    override suspend fun register(token: String?): RegisterResult {
        throw NotImplementedError()
    }

    override fun isRegistered(): Boolean = true
}
