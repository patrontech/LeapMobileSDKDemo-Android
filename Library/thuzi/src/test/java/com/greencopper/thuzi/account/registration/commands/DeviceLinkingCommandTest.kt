package com.greencopper.thuzi.account.registration.commands

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.commands.system.CommandInfo
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.interfacekit.ui.fragment.findVisibleFragment
import com.greencopper.interfacekit.ui.fragment.waitToBeAttached
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.interfacekit.MockRouteController
import com.greencopper.testmocks.setupTest
import com.greencopper.thuzi.account.registration.ui.RegistrationFragment
import com.greencopper.toolkit.Toolkit
import io.mockk.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class DeviceLinkingCommandTest : CoroutineTest(UnconfinedTestDispatcher()) {

    private val routeController: MockRouteController
    private val deviceLinkingCommand: DeviceLinkingCommand

    private val params = DeviceLinkingCommand.DeviceLinkingCommandData("https://www.test.com")

    init {
        Toolkit.setupTest()
        routeController = MockRouteController()
        mockkStatic("com.greencopper.interfacekit.ui.fragment.BaseFragmentKt")

        deviceLinkingCommand = spyk(
            DeviceLinkingCommand(
                routeController,
                testScope
            ),
            recordPrivateCalls = true
        )
    }

    override fun afterEach() {
        unmockkAll()
    }

    @Test
    fun verifyKey() {
        Assertions.assertThat(DeviceLinkingCommand.key).isEqualTo(
            CommandInfo.Key(
                "Thuzi.DeviceLinking",
                1
            )
        )
    }

    @Test
    fun verifyDeserializeFunction() {
        val deserializedParams = deviceLinkingCommand.deserialize(params.encodeToJsonElement())

        Assertions.assertThat(deserializedParams.url).isEqualTo(params.url)
    }

    @Test
    fun serializeAndDeserializeData() {
        org.junit.jupiter.api.assertDoesNotThrow {
            val data =
                KiboSerializable.decodeFromString<DeviceLinkingCommand.DeviceLinkingCommandData>(
                    params.encodeToString()
                )
            Assertions.assertThat(data.url).isEqualTo(params.url)
        }
    }

    @Test
    fun whenPassingNullOrigin_shouldDoNothing() {
        mockkStatic(Fragment::findVisibleFragment)
        deviceLinkingCommand.executeWith(params, null)
        verify(exactly = 0) {
            runTest {
                any<Fragment>().findVisibleFragment()
            }
        }
    }

    @Test
    fun whenPassingOrigin_asRegistrationFragment_shouldUpdateRegistrationUrl() {
        val origin = getMockedRegistrationFragment()
        deviceLinkingCommand.executeWith(params, origin)

        verify(exactly = 1) { origin.updateUrl(any()) }
    }

    @Test
    fun whenPassingOrigin_asOtherFragment_withRouteFound_shouldRedirect() {
        val origin = getMockedFragment(false)
        deviceLinkingCommand.executeWith(params, origin)

        Assertions.assertThat(routeController.lastRedirectRoute).isNotNull
        Assertions.assertThat(routeController.lastRedirectRoute)
            .isInstanceOf(Route.Present::class.java)
    }

    private fun getMockedFragment(withChild: Boolean): Layout {
        val fragment = spyk(mockk<DialogFragment>())
        every { any<Fragment>().waitToBeAttached() } returns CompletableDeferred(true)
        every { fragment.childFragmentManager.fragments } returns if (withChild) {
            listOf(getMockedFragment(false))
        } else {
            emptyList()
        }
        return fragment
    }

    private fun getMockedRegistrationFragment(): RegistrationFragment {
        val fragment = spyk(mockk<RegistrationFragment>(), recordPrivateCalls = true)
        every { any<Fragment>().waitToBeAttached() } returns CompletableDeferred(true)
        every { fragment.updateUrl(any()) } just runs
        every { fragment.childFragmentManager.fragments } returns emptyList()
        return fragment
    }

}
