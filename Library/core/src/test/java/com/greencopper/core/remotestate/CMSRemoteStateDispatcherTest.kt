package com.greencopper.core.remotestate

import com.greencopper.core.content.manager.CurrentProjectTagProvider
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.core
import com.greencopper.core.networking.SignatureGenerator
import com.greencopper.core.recipe.CoreConfiguration
import com.greencopper.core.recipe.CoreConfigurationHolder
import com.greencopper.coremocks.MockCoreAPI
import com.greencopper.coremocks.SignatureGeneratorMock
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.bindSingleton
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.LazyResolver
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

internal class CMSRemoteStateDispatcherTest : CoroutineTest() {

    private val classUnderTest: CMSRemoteStateDispatcher
    private val coreAPI = MockCoreAPI()
    private val signatureGenerator: SignatureGenerator = SignatureGeneratorMock()

    private val remoteStateConfig = CoreConfiguration.RemoteState("https://apiUrl.com", threshold = 10)
    private val coreConfig = CoreConfiguration(
        remoteState = remoteStateConfig,
        notification = null,
        ota = CoreConfiguration.OTA(""),
        timezone = null,
        contentConfig = CoreConfiguration.ContentConfig(1L, listOf()),
        custom = emptyMap(),
    )
    private val coreConfigHolder = CoreConfigurationHolder().apply {
        currentConfiguration.value = coreConfig
    }
    private val localStorage = LocalStorage("project")

    init {
        Toolkit.setupTest()
        bindSingleton(coreConfigHolder)

        val currentProjectTagProvider = object : CurrentProjectTagProvider {
            override val currentProject: String = "project"
            override val currentProjectFlow: StateFlow<String?> = MutableStateFlow(currentProject)
        }

        classUnderTest = CMSRemoteStateDispatcher(
            coreAPI,
            signatureGenerator,
            coreConfigHolder,
            currentProjectTagProvider,
            LazyResolver.adhoc(localStorage),
            App.resolve<Json>(),
            testScope,
        )
    }

    override fun afterEach() {}

    @Test
    @DisplayName("Given remote state and current project are set, When dispatch is called, Then no exception is thrown")
    fun dispatchShouldSucceed() {
        assertDoesNotThrow {
            classUnderTest.dispatch(
                "testKey",
                JsonPrimitive("testValue"),
                isUrgent = true,
                domain = RemoteStateEntry.Domain.PROJECT
            )
        }
    }

    @Test
    @DisplayName("Given remote state and current project are set, When dispatchOnLifecyclePause is called, Then no exception is thrown")
    fun dispatchOnLifecyclePauseShouldSucceed() {
        assertDoesNotThrow {
            classUnderTest.dispatch(
                "testKey",
                JsonPrimitive("testValue"),
                isUrgent = false,
                domain = RemoteStateEntry.Domain.PROJECT
            )
            classUnderTest.dispatchOnLifecyclePause()
        }
    }

    @Test
    fun createProjectRemoteStateDispatcherSucceeds() {
        localStorage["other"].project.core.remoteState.configuration.value = CoreConfiguration.RemoteState("https://foo.bar", 20)
        assertDoesNotThrow {
            classUnderTest.dispatch(
                "testKey",
                JsonPrimitive("testValue"),
                domain = RemoteStateEntry.Domain.PROJECT,
                isUrgent = false,
                project = "other"
            )
        }
    }
}
