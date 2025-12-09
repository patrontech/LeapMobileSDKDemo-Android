package com.greencopper.ticketing.providers.showclix.login

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.interfacekit.commands.system.CommandInfo
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.interfacekit.ui.fragment.findVisibleFragment
import com.greencopper.interfacekit.ui.fragment.waitToBeAttached
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.interfacekit.MockLinkResolver
import com.greencopper.testmocks.interfacekit.MockRouteController
import com.greencopper.testmocks.setupTest
import com.greencopper.ticketing.providers.showclix.login.ui.ShowclixLoginFragment
import com.greencopper.ticketing.providers.showclix.showclix
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import io.mockk.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

internal class ShowclixLoginMagicLinkCommandTest : CoroutineTest(UnconfinedTestDispatcher()) {

    init {
        Toolkit.setupTest()
        mockkStatic("com.greencopper.interfacekit.ui.fragment.BaseFragmentKt")
    }

    private val localStorage: LocalStorage = App.resolve()
    private val routeController = MockRouteController()
    private val linkResolver = MockLinkResolver()
    private val showclixCommand: ShowclixLoginMagicLinkCommand = spyk(
        ShowclixLoginMagicLinkCommand(
            routeController,
            linkResolver,
            localStorage,
            testScope
        ),
        recordPrivateCalls = true
    )

    private val params = ShowclixLoginMagicLinkCommand.MagicLinkCommandData("token", "route")

    override fun afterEach() {
        unmockkAll()
    }

    @Test
    fun verifyKey() {
        assertThat(ShowclixLoginMagicLinkCommand.key).isEqualTo(
            CommandInfo.Key(
                "Ticketing.Showclix.MagicLink",
                1
            )
        )
    }

    @Test
    fun verifyDeserializeFunction() {
        val deserializedParams = showclixCommand.deserialize(params.encodeToJsonElement())

        assertThat(deserializedParams.token).isEqualTo(params.token)
        assertThat(deserializedParams.routeLink).isEqualTo(params.routeLink)
    }

    @Test
    fun serializeAndDeserializeData() {
        assertDoesNotThrow {
            val data =
                KiboSerializable.decodeFromString<ShowclixLoginMagicLinkCommand.MagicLinkCommandData>(
                    params.encodeToString()
                )
            assertThat(data.token).isEqualTo(params.token)
            assertThat(data.routeLink).isEqualTo(params.routeLink)
        }
    }

    @Test
    fun whenPassingNullOrigin_shouldDoNothing() {
        mockkStatic(Fragment::findVisibleFragment)
        showclixCommand.executeWith(params, null)
        verify(exactly = 0) {
            runTest {
                any<Fragment>().findVisibleFragment()
            }
        }
    }

    @Test
    fun whenPassingOrigin_asShowclixLoginFragment_shouldCallShowclixLoginFunction() {
        val origin = getMockedShowclixFragment()
        showclixCommand.executeWith(params, origin)

        verify(exactly = 1) { origin.verifyToken(any()) }
    }

    @Nested
    @DisplayName("when top Fragment isn't ShowclixLoginFragment")
    inner class WithNormalFragment {

        private val json: Json = App.resolve()
        private val deeplinkScheme = "kibaapp"

        private val route = Route.Present(
            FeatureInfo(
                FeatureKey("", 1),
                json.parseToJsonElement("{ \"myParamString\": \"pouet\"}")
            )
        )

        @Test
        fun whenPassingOrigin_asOtherFragment_withRouteFound_shouldRedirect() {
            val origin = getMockedFragment(false)
            linkResolver.mockRoutes = mapOf("route" to route)
            showclixCommand.executeWith(params, origin)

            assertThat(localStorage.project.showclix.timeToken.value).isEqualTo("token")
            assertThat(routeController.lastRedirectRoute).isEqualTo(route)
        }

        @Test
        fun whenPassingOrigin_asOtherFragment_withChildren_withRouteFound_shouldRedirect() {
            val origin = getMockedFragment(true)
            linkResolver.mockRoutes = mapOf("route" to route)
            showclixCommand.executeWith(params, origin)

            assertThat(localStorage.project.showclix.timeToken.value).isEqualTo("token")
            assertThat(routeController.lastRedirectRoute).isEqualTo(route)
        }

        @Test
        fun whenPassingOrigin_asOtherFragment_withRouteNotFound_shouldNotRedirect() {
            val origin = getMockedFragment(false)
            showclixCommand.executeWith(params, origin)

            assertThat(routeController.lastRedirectRoute).isEqualTo(null)
        }

        private fun getMockedFragment(withChild: Boolean): Layout {
            val fragment = spyk(mockk<DialogFragment>())
            every { any<Fragment>().waitToBeAttached() } returns CompletableDeferred(true)
            every { fragment.childFragmentManager.fragments } returns if (withChild) {
                listOf(getMockedFragment(false))
            } else {
                emptyList()
            }
            every { fragment.getString(any()) } returns deeplinkScheme
            return fragment
        }
    }

    private fun getMockedShowclixFragment(): ShowclixLoginFragment {
        val fragment = spyk(mockk<ShowclixLoginFragment>(), recordPrivateCalls = true)
        every { any<Fragment>().waitToBeAttached() } returns CompletableDeferred(true)
        every { fragment.verifyToken(any()) } just runs
        every { fragment.childFragmentManager.fragments } returns emptyList()
        return fragment
    }

}
