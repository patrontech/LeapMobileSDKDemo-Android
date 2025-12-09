package com.greencopper.interfacekit.session

import com.greencopper.core.content.archive.ContentArchive
import com.greencopper.core.content.manager.Content
import com.greencopper.core.content.ota.OTAContent
import com.greencopper.core.content.ota.OTAManager
import com.greencopper.core.recipe.CoreConfiguration
import com.greencopper.core.recipe.CoreConfigurationHolder
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.core.MockContentInitializer
import com.greencopper.testmocks.core.MockContentManager
import com.greencopper.testmocks.core.MockDraftContentManager
import com.greencopper.testmocks.core.MockOTAManager
import com.greencopper.testmocks.interfacekit.MockRootLayoutManager
import com.greencopper.testmocks.interfacekit.MockRouteController
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.toolkit.MockLogging
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.LazyResolver
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File
import java.time.ZonedDateTime

internal class ConcreteSessionManagerTest : CoroutineTest(UnconfinedTestDispatcher()) {

    init {
        Toolkit.setupTest()
    }

    private val configHolder = CoreConfigurationHolder().apply {
        val coreConfiguration = CoreConfiguration(
            remoteState = CoreConfiguration.RemoteState("apiUrl", 20),
            notification = null,
            CoreConfiguration.OTA("apiUrl"),
            timezone = null,
            CoreConfiguration.ContentConfig(60, emptyList()),
            null
        )
        currentConfiguration.tryEmit(coreConfiguration)
    }
    private val content = Content(
        ContentArchive(File(""), "secret"), 1, 1, "test_project", OTAContent.Type.Release,
    )
    private val otaManager = MockOTAManager(
        otaContentToProcessValue = { null },
        processValue = { mockk() },
    )
    private val draftContentManager = MockDraftContentManager(
        passcodeReturnValue = { null },
        passcodeFlowReturnValue = { flowOf(null) },
    )
    private val contentManager = MockContentManager(
        contentToApplyValue = { null },
    )

    private val contentInitializer = MockContentInitializer(
        initializeResult = { content }
    )
    private val lazyOTAManager: LazyResolver<OTAManager> = LazyResolver.adhoc(otaManager)
    private val routeController = MockRouteController()
    private val rootLayoutManager = MockRootLayoutManager()
    private val logger = MockLogging()

    private val sessionManager = ConcreteSessionManager(
        configHolder,
        contentInitializer,
        contentManager,
        draftContentManager,
        lazyOTAManager,
        routeController,
        rootLayoutManager,
        testScope,
        logger
    )

    override fun afterEach() {}

    @Nested
    @DisplayName("Given a valid initial content and no OTA found")
    inner class InitialContentIsValid {

        @Test
        @DisplayName("When resuming session, Then it should initialize content")
        fun resumeInitializingShouldSucceed() {
            runTest {
                sessionManager.resume()
                assertThat(contentInitializer.initializeCount).isEqualTo(1)
            }
        }

        @Test
        @DisplayName("When resuming session, Then process shouldn't be called")
        fun resumeWithoutOTAShouldSucceed() {
            runTest {
                //Call to resume to initialize content
                sessionManager.resume()

                //Second call to check for OTA
                sessionManager.resume()
                assertThat(otaManager.otaContentToProcessCount).isEqualTo(1)
                assertThat(otaManager.processCount).isEqualTo(0)
            }
        }

        @Test
        @DisplayName("When pausing session, Then process shouldn't be called")
        fun pauseWithoutOTAShouldSucceed() {
            runTest {
                //Call to resume to initialize content
                sessionManager.resume()

                sessionManager.pause()
                assertThat(otaManager.otaContentToProcessCount).isEqualTo(2)
                assertThat(otaManager.processCount).isEqualTo(0)
            }
        }

        @Test
        @DisplayName("When redirecting to route link, Then redirection should happen without processing content")
        fun redirectToRouteLinkShouldSucceed() {
            runTest {
                //Call to resume to initialize content
                sessionManager.resume()

                sessionManager.redirectTo("")
                assertThat(otaManager.otaContentToProcessCount).isEqualTo(1)
                assertThat(otaManager.processCount).isEqualTo(0)
                assertThat(routeController.lastRedirectRouteLink).isNotNull
            }
        }

        @Test
        @DisplayName("When redirecting to route, Then redirection should happen without processing content")
        fun redirectToRouteShouldSucceed() {
            runTest {
                //Call to resume to initialize content
                sessionManager.resume()

                sessionManager.redirectTo(mockk<Route>())
                assertThat(otaManager.otaContentToProcessCount).isEqualTo(1)
                assertThat(otaManager.processCount).isEqualTo(0)
                assertThat(routeController.lastRedirectRoute).isNotNull
            }
        }

        @Test
        @DisplayName("When silent update is called, Then process shouldn't be called")
        fun silentUpdateShouldSucceed() {
            runTest {
                //Call to resume to initialize content
                sessionManager.resume()

                sessionManager.silentUpdate()
                assertThat(otaManager.otaContentToProcessCount).isEqualTo(2)
                assertThat(otaManager.processCount).isEqualTo(0)
            }
        }
    }

    @Nested
    @DisplayName("Given a valid initial content and valid OTA is found")
    inner class OTAContentIsValid {
        @BeforeEach
        internal fun setUp() {
            otaManager.otaContentToProcessValue = {
                OTAContent(
                    null,
                    "test_project",
                    null,
                    2,
                    "RELEASE",
                    1
                )
            }
        }

        @Test
        @DisplayName("When resuming session, Then it should initialize content")
        fun resumeInitializingShouldSucceed() {
            runTest {
                sessionManager.resume()
                assertThat(contentInitializer.initializeCount).isEqualTo(1)
            }
        }

        @Test
        @DisplayName("When resuming session, Then it should process last OTA")
        fun resumeWithOTAShouldSucceed() {
            runTest {
                //Call to resume to initialize content
                sessionManager.resume()

                //Second call to check for OTA
                sessionManager.resume()
                assertThat(otaManager.otaContentToProcessCount).isEqualTo(1)
                assertThat(otaManager.processCount).isEqualTo(1)
            }
        }

        @Test
        @DisplayName("When pausing session, Then it should process last OTA")
        fun pauseWithOTAShouldSucceed() {
            runTest {
                //Call to resume to initialize content
                sessionManager.resume()

                sessionManager.pause()
                assertThat(otaManager.otaContentToProcessCount).isEqualTo(2)
                assertThat(otaManager.processCount).isEqualTo(2)
            }
        }

        @Test
        @DisplayName("When redirecting to route link, Then redirection should happen after processing content")
        fun redirectToRouteLinkShouldSucceed() {
            runTest {
                //Call to resume to initialize content
                sessionManager.resume()

                sessionManager.redirectTo("")
                assertThat(otaManager.otaContentToProcessCount).isEqualTo(1)
                assertThat(otaManager.processCount).isEqualTo(1)
                assertThat(routeController.lastRedirectRouteLink).isNotNull
            }
        }

        @Test
        @DisplayName("When redirecting to route link, Then redirection should happen after processing content")
        fun redirectToRouteShouldSucceed() {
            runTest {
                //Call to resume to initialize content
                sessionManager.resume()

                sessionManager.redirectTo(mockk<Route>())
                assertThat(otaManager.otaContentToProcessCount).isEqualTo(1)
                assertThat(otaManager.processCount).isEqualTo(1)
                assertThat(routeController.lastRedirectRoute).isNotNull
            }
        }

        @Test
        @DisplayName("When silent update is called, Then last OTA should be processed")
        fun silentUpdateShouldSucceed() {
            runTest {
                //Call to resume to initialize content
                sessionManager.resume()

                sessionManager.silentUpdate()
                assertThat(otaManager.otaContentToProcessCount).isEqualTo(2)
                assertThat(otaManager.processCount).isEqualTo(2)
            }
        }
    }

    @Nested
    @DisplayName("Given initial content is failing and processing OTA is failing")
    inner class InitialContentIsNotValid {

        @BeforeEach
        internal fun setUp() {
            App = mockk()
            every { App.date } returns { ZonedDateTime.now() }
            otaManager.otaContentToProcessValue = { null }
        }

        @Test
        @DisplayName("When resuming session, Then initialization should fail")
        fun resumeInitializingShouldFail() {
            contentInitializer.initializeResult = { throw RuntimeException() }
            try {
                runTest {
                    sessionManager.resume()
                    assertThat(contentInitializer.initializeCount).isEqualTo(0)
                }
            } catch (_: RuntimeException) {
                // expected to fail
            }
        }

        @Test
        @DisplayName("When resuming session, Then processing should fail")
        fun resumeWithOTAShouldFail() {
            runTest {
                //Call to resume to initialize content
                sessionManager.resume()

                //Second call to check for OTA
                sessionManager.resume()
                assertThat(otaManager.otaContentToProcessCount).isEqualTo(1)
                assertThat(otaManager.processCount).isEqualTo(0)
            }
        }

        @Test
        @DisplayName("When pausing session, Then processing should fail")
        fun pauseWithOTAShouldFail() {
            runTest {
                //Call to resume to initialize content
                sessionManager.resume()

                sessionManager.pause()
                assertThat(otaManager.otaContentToProcessCount).isEqualTo(2)
                assertThat(otaManager.processCount).isEqualTo(0)
            }
        }

        @Test
        @DisplayName("When redirecting to route link, Then processing should fail and redirection happens")
        fun redirectToRouteLinkShouldSucceed() {
            runTest {
                //Call to resume to initialize content
                sessionManager.resume()

                sessionManager.redirectTo("")
                assertThat(otaManager.otaContentToProcessCount).isEqualTo(1)
                assertThat(otaManager.processCount).isEqualTo(0)
                assertThat(routeController.lastRedirectRouteLink).isNotNull
            }
        }

        @Test
        @DisplayName("When redirecting to route, Then processing should fail and redirection happens")
        fun redirectToRouteShouldSucceed() {
            runTest {
                //Call to resume to initialize content
                sessionManager.resume()

                sessionManager.redirectTo(mockk<Route>())
                assertThat(otaManager.otaContentToProcessCount).isEqualTo(1)
                assertThat(otaManager.processCount).isEqualTo(0)
                assertThat(routeController.lastRedirectRoute).isNotNull
            }
        }

        @Test
        @DisplayName("When silent update is called, Then processing should fail")
        fun silentUpdateShouldFail() {
            runTest {
                //Call to resume to initialize content
                sessionManager.resume()

                sessionManager.silentUpdate()
                assertThat(otaManager.otaContentToProcessCount).isEqualTo(2)
                assertThat(otaManager.processCount).isEqualTo(0)
            }
        }
    }

    @Test
    fun draftContentNotChanged_updateRootLayout_shouldNotBeCalled() {
        assertThat(rootLayoutManager.updateRootLayoutCalled).isFalse
    }

    @Test
    fun draftContentToggled_shouldUpdatedRootLayout() {
        draftContentManager.passcodeFlowReturnValue = { flowOf(null, "passcode") }

        ConcreteSessionManager(
            configHolder,
            contentInitializer,
            contentManager,
            draftContentManager,
            lazyOTAManager,
            routeController,
            rootLayoutManager,
            testScope,
            logger
        )

        assertThat(rootLayoutManager.updateRootLayoutCalled).isTrue
    }
}
