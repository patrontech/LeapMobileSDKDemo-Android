package com.greencopper.interfacekit.navigation.route

import androidx.fragment.app.*
import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.TestLocalStorageContainer
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.ScreenColor
import com.greencopper.interfacekit.navigation.*
import com.greencopper.interfacekit.navigation.feature.*
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.layout.*
import com.greencopper.interfacekit.presentBottomSheet
import com.greencopper.interfacekit.rootview.RootLayoutHolder
import com.greencopper.interfacekit.ui.fragment.*
import com.greencopper.testmocks.*
import com.greencopper.testmocks.core.MockLocalizationService
import com.greencopper.testmocks.interfacekit.*
import com.greencopper.testmocks.toolkit.MockLogging
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.binding.auto
import com.greencopper.toolkit.di.resolver.LazyResolver
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

internal class ConcreteRouteControllerTest : CoroutineTest() {

    private val redirectionHash = RedirectionHash(FeatureKey("random_name", 99))

    private inner class BaseFragmentTest : BaseFragment(R.layout.sample_fragment),
        RedirectingLayout {
        override val availableRedirections: List<RedirectionHash> = listOf(redirectionHash)
        override fun redirectTo(hash: RedirectionHash) {}
        override val screenColor: ScreenColor? get() = null
    }

    private val baseFragment: BaseFragment = spyk(BaseFragmentTest())

    private val featureResolver = mockkClass(FeatureResolver::class)

    private val commandExecutor = MockCommandExecutor()
    private val context = InstrumentationRegistry.getInstrumentation().context
    private val rootLayout = MutableStateFlow<Layout?>(null)
    private val linkResolver = MockLinkResolver()
    private val logger = MockLogging()

    private val concreteRouteController = spyk(
        ConcreteRouteController(
            featureResolver = featureResolver,
            commandExecutor = commandExecutor,
            context = context,
            rootLayout = rootLayout,
            localizationService = MockLocalizationService(),
            lazyLocalStorage = LazyResolver.adhoc(
                LocalStorage(
                    "UT_test",
                    TestLocalStorageContainer()
                )
            ),
            linkResolver = linkResolver,
            mainThreadScope = testScope,
            logger = logger,
        ),
        recordPrivateCalls = true
    )

    private val featureInfo = FeatureInfo(FeatureKey("name", 98))
    private val routePush = Route.Push(featureInfo)
    private val routePresent = Route.Present(featureInfo)
    private val featureInitializer: FeatureInitializer = mockk()
    private val origin: Layout = DialogFragment()

    init {
        Toolkit.setupTest()
        rootLayout.value = null
    }

    override fun afterEach() {
        unmockkAll()
    }

    @Test
    fun removeOriginAndAdd_withUnresolvedFeature_shouldNotThrow() {
        every {
            featureResolver.resolve(any())
        } answers { throw FeatureResolverException.FeatureNotRegisteredException(featureInfo.key) }
        assertDoesNotThrow {
            concreteRouteController.replaceBackStackAware(origin, featureInfo)
        }
        verify(exactly = 1) { featureResolver.resolve(featureInfo) }
    }

    @Test
    fun resolvingRoutePush_withUnresolvedFeature_shouldNotThrow() {
        every {
            featureResolver.resolve(any())
        } answers { throw FeatureResolverException.FeatureNotRegisteredException(featureInfo.key) }
        assertDoesNotThrow {
            concreteRouteController.resolve(routePush, origin)
        }
        verify(exactly = 1) { featureResolver.resolve(featureInfo) }
    }

    @Test
    fun resolvingRoutePush_withResolvedFeatureThrowingAny_shouldNotThrow() {
        every { featureResolver.resolve(any()) } answers { throw Exception() }
        assertDoesNotThrow {
            concreteRouteController.resolve(routePush, origin)
        }
        verify(exactly = 1) { featureResolver.resolve(featureInfo) }
    }

    @Test
    fun resolvingRoutePush_withResolvedFeatureBeingFragment_shouldNotThrow() {
        every { featureResolver.resolve(any()) } answers { baseFragment }
        assertDoesNotThrow {
            concreteRouteController.resolve(routePush, origin)
        }
        verify(exactly = 1) { featureResolver.resolve(featureInfo) }
    }

    @Test
    fun resolvingRoutePresent_withNullRootLayout_shouldNotThrow() {
        assertDoesNotThrow {
            concreteRouteController.resolve(routePresent, origin)
        }
    }

    @Test
    fun resolvingRoutePresent_withUnresolvedFeature_shouldNotThrow() {
        every {
            featureResolver.resolve(any())
        } answers { throw FeatureResolverException.FeatureNotRegisteredException(featureInfo.key) }
        assertDoesNotThrow {
            concreteRouteController.resolve(routePresent, origin)
        }
        verify(exactly = 1) { featureResolver.resolve(featureInfo) }
    }

    @Test
    fun resolvingRoutePresent_withResolvedFeatureBeingFragment_shouldNotThrow() {
        every { featureResolver.resolve(any()) } answers { baseFragment }
        assertDoesNotThrow {
            concreteRouteController.resolve(routePresent, origin)
        }
    }

    @Test
    fun resolvingRouteExternal_withMailTo_shouldNotThrow() {
        assertDoesNotThrow {
            concreteRouteController.resolve(
                Route.External("mailto:test@google.com?cc=test&subject=test&body=test"),
                origin
            )
        }
    }

    @Test
    fun resolvingRouteExternal_withUrl_shouldNotThrow() {
        assertDoesNotThrow {
            concreteRouteController.resolve(
                Route.External("www.google.com"),
                origin
            )
        }
    }

    @Test
    fun resolvingRouteExternal_withParameterizedUrl_shouldNotThrow() {
        assertDoesNotThrow {
            concreteRouteController.resolve(
                Route.External("www.google.com/?installationId={@/installationId}&localized="),
                origin
            )
        }
    }

    @Test
    fun showAlert_withoutRootLayoutNotSet_shouldNotThrow() {
        assertDoesNotThrow {
            concreteRouteController.showAlert("", "", null, null, null, null, null)
        }
    }

    @Test
    fun redirect_withRoutePushAndNoRedirection_shouldShowAlert() {
        every { featureResolver.resolveInitializer(any()) } answers { featureInitializer }
        every { featureInitializer.redirectionHashFor(any()) } returns mockk()

        concreteRouteController.redirect(routePush, origin)
        verify(exactly = 1) { concreteRouteController.showAlert(title = any(), message = any()) }
    }

    @Test
    fun redirect_withRoutePushAndRedirection_shouldNotThrow() {
        every { featureResolver.resolveInitializer(any()) } answers { featureInitializer }
        every { featureInitializer.redirectionHashFor(any()) } returns RedirectionHash(
            FeatureKey(
                "random_name",
                99
            )
        )
        assertDoesNotThrow {
            concreteRouteController.redirect(routePush, origin)
        }
    }

    @Test
    fun redirect_withRoutePushAndRedirectionAndNoOrigin_shouldNotThrow() {
        every { featureResolver.resolveInitializer(any()) } answers { featureInitializer }
        every { featureInitializer.redirectionHashFor(any()) } returns RedirectionHash(
            FeatureKey(
                "random_name",
                99
            )
        )
        assertDoesNotThrow {
            concreteRouteController.redirect(routePush, null)
        }
    }

    @Test
    fun redirect_withRoutePresent_shouldThrow() {
        every { featureResolver.resolveInitializer(any()) } answers { featureInitializer }
        every { featureInitializer.redirectionHashFor(any()) } returns mockk()

        concreteRouteController.redirect(routePresent, origin)
        verify(exactly = 1) { concreteRouteController.showAlert(title = any(), message = any()) }
    }

    @Test
    fun replace_shouldNotThrow() {
        assertDoesNotThrow {
            concreteRouteController.replace(baseFragment, featureInfo)
        }
    }

    @Test
    fun commandExecutionResolve_shouldNotThrow() {
        bindCommand(MockCommand.key, auto(::MockCommand))
        assertDoesNotThrow {
            val routeExecute = Route.Execute(MockCommand.commandInfo)
            concreteRouteController.resolve(routeExecute, origin)
            assertThat(commandExecutor.executedCommandInfo)
                .isEqualTo(MockCommand.commandInfo)
        }
    }
    
    @Test
    fun commandDoesNotResolve_shouldNotThrow() {
        commandExecutor.shouldThrow = true
        assertDoesNotThrow {
            val routeExecute = Route.Execute(MockCommand.commandInfo)
            concreteRouteController.resolve(routeExecute, origin)
            assertThat(commandExecutor.executedCommandInfo).isNull()
        }
    }

    @Test
    fun commandExecutionRedirect_shouldNotThrow() {
        bindCommand(MockCommand.key, auto(::MockCommand))
        assertDoesNotThrow {
            val routeExecute = Route.Execute(MockCommand.commandInfo)
            concreteRouteController.redirect(routeExecute, origin)
            assertThat(commandExecutor.executedCommandInfo)
                .isEqualTo(MockCommand.commandInfo)
        }
    }

    @Test
    fun commandExecutionRedirect_withNullOrigin_shouldNotThrow() {
        bindCommand(MockCommand.key, auto(::MockCommand))
        assertDoesNotThrow {
            val routeExecute = Route.Execute(MockCommand.commandInfo)
            concreteRouteController.redirect(routeExecute, null)
            assertThat(commandExecutor.executedCommandInfo)
                .isEqualTo(MockCommand.commandInfo)
        }
    }

    @Test
    fun whenRouteResolves_resolveRouteLink_callsResolve() {
        val route = Route.Present(FeatureInfo(FeatureKey("name", 0)))
        linkResolver.mockRoutes = mapOf("route" to route)
        concreteRouteController.resolveRouteLink("route", mockk())
        verify { concreteRouteController.resolve(route, any()) }
    }

    @Test
    fun whenNoRoute_resolveRouteLink_doesNotCallResolve() {
        concreteRouteController.resolveRouteLink("route", mockk())
        verify(exactly = 0) { concreteRouteController.resolve(any(), any()) }
    }

    @Test
    fun whenRouteResolves_redirectRouteLink_callsRedirect() {
        val route = Route.Present(FeatureInfo(FeatureKey("name", 0)))
        linkResolver.mockRoutes = mapOf("route" to route)
        concreteRouteController.redirectRouteLink("route", mockk())
        verify { concreteRouteController.redirect(route, any()) }
    }

    @Test
    fun whenNoRoute_redirectRouteLink_doesNotCallRedirect() {
        concreteRouteController.redirectRouteLink("route", mockk())
        verify(exactly = 0) { concreteRouteController.redirect(any(), any()) }
    }

    @Test
    fun whenOpenBottomSheet_givenNullLayoutAndUnresolvedFeature_shouldShowAlertFeatureUnavailable() {
        val featureInfo = mockk<FeatureInfo>()
        concreteRouteController.openBottomSheet(null, featureInfo, 0)
        verify(exactly = 1) { concreteRouteController["showAlertFeatureUnavailable"](featureInfo, any<Throwable>()) }
    }

    @Test
    fun whenOpenBottomSheet_givenNullLayoutAndResolvedFeature_shouldPresentBottomSheet() {
        every { featureResolver.resolve(any()) } answers { baseFragment }
        val layout = spyk(NavigationFragment())
        RootLayoutHolder().setRootLayout(layout)

        val mockFragmentManager: FragmentManager = spyk(recordPrivateCalls = true)
        every { layout.ncParentFragmentManager } returns mockFragmentManager
        every { mockFragmentManager.addFragmentOnAttachListener(any()) } just Runs

        concreteRouteController.openBottomSheet(null, featureInfo, 0)

        verify(exactly = 1) { layout.bottomSheetPresent(any(), any()) }
        verify(exactly = 1) { mockFragmentManager.addFragmentOnAttachListener(any()) }
    }

    @Test
    fun whenOpenBottomSheet_givenValidLayoutAndResolvedFeature_shouldPresentBottomSheet() {
        every { featureResolver.resolve(any()) } answers { baseFragment }
        val layout = mockk<NavigationFragment>()
        every { layout.bottomSheetPresent(any(), any()) } just Runs
        mockkStatic(Layout::getNavigationController)
        every { layout.getNavigationController() } returns null
        RootLayoutHolder().setRootLayout(layout)

        val mockFragmentManager: FragmentManager = mockk()
        every { layout.ncParentFragmentManager } returns mockFragmentManager
        every { mockFragmentManager.addFragmentOnAttachListener(any()) } just Runs

        concreteRouteController.openBottomSheet(layout, featureInfo, 0)

        verify(exactly = 1) { layout.bottomSheetPresent(any(), any()) }
    }

    @Test
    fun whenOpenBottomSheet_givenBottomSheetDialogFragmentContainerLayoutAndResolvedFeature_shouldPresentBottomSheetAndDismiss() {
        every { featureResolver.resolve(any()) } answers { baseFragment }
        val layout = spyk(BottomSheetDialogFragmentContainer())
        mockkStatic(Layout::getNavigationController)
        every { layout.getNavigationController() } returns null
        RootLayoutHolder().setRootLayout(layout)

        val mockFragmentManager: FragmentManager = mockk()
        every { layout.ncParentFragmentManager } returns mockFragmentManager
        every { layout.dismiss() } just Runs
        every { mockFragmentManager.addFragmentOnAttachListener(any()) } just Runs
        mockkStatic(FragmentManager::presentBottomSheet)
        mockkStatic(Fragment::getStackTag)
        every { mockFragmentManager.presentBottomSheet(any(), any(), any()) } just Runs


        concreteRouteController.openBottomSheet(layout, featureInfo, 0)

        verify(exactly = 1) { layout.bottomSheetPresent(any(), any()) }
        verify(exactly = 1) { layout.dismiss() }
    }

    @Test
    fun whenOpenBottomSheet_givenNullNavigationControllerAndResolvedFeature_shouldNotPresentBottomSheet() {
        every { featureResolver.resolve(any()) } answers { baseFragment }
        val layout = mockk<NavigationFragment>()
        mockkStatic(Layout::getNavigationController)
        every { layout.getNavigationController() } returns null
        val layoutHolder = RootLayoutHolder()
        layoutHolder.setRootLayout(layout)
        layoutHolder.clearRootLayout()

        val mockFragmentManager: FragmentManager = mockk(relaxed = true)
        every { layout.ncParentFragmentManager } returns mockFragmentManager
        every { mockFragmentManager.addFragmentOnAttachListener(any()) } just Runs

        concreteRouteController.openBottomSheet(layout, featureInfo, 0)

        verify(exactly = 0) { (layout as? NavigationController<*>)?.bottomSheetPresent(any(), any()) }
        verify(exactly = 0) { mockFragmentManager.addFragmentOnAttachListener(any()) }
    }

    @Test
    fun givenNullParentFragment_whenGetNavigationController_shouldReturnNull() {
        val layout = mockk<NavigationFragment>()
        every { layout.parentFragment } returns null
        val result = layout.getNavigationController()
        assertThat(result).isNull()
    }

    @Test
    fun givenNullParentFragment_whenGetParentPresentedFragment_shouldReturnNull() {
        val layout = mockk<NavigationFragment>()
        every { layout.parentFragment } returns null
        val result = layout.getParentPresentedFragment()
        assertThat(result).isNull()
    }

    @Test
    fun givenParentFragment_whenGetParentPresentedFragment_shouldReturnParent() {
        val layout = mockk<NavigationFragment>()
        val expected = mockk<PresentedFragment>()
        every { layout.parentFragment } returns expected
        val result = layout.getParentPresentedFragment()
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun givenParentFragmentIsNotPresentedFragment_whenGetParentPresentedFragment_shouldReturnParent() {
        val layout = mockk<NavigationFragment>()
        val expected = mockk<Fragment>()
        val expectedParent = mockk<PresentedFragment>()
        every { layout.parentFragment } returns expected
        every { expected.parentFragment } returns expectedParent
        val result = layout.getParentPresentedFragment()
        assertThat(result).isEqualTo(expectedParent)
    }

    @Test
    fun givenNullParentFragment_whenGetParentNavigationController_shouldReturnNull() {
        val layout = mockk<NavigationFragment>()
        every { layout.parentFragment } returns null
        val result = layout.getParentNavigationController()
        assertThat(result).isNull()
    }

    @Test
    fun givenParentFragmentWithNavigationController_whenGetParentNavigationController_shouldReturnParentNavigationController() {
        val layout = mockk<NavigationFragment>()
        val parent = mockk<PresentedFragment>()
        val expected = mockk<NavigationFragment>()
        every { layout.parentFragment } returns parent
        every { parent.ncParentFragment } returns expected
        val result = layout.getParentNavigationController()
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun givenParentFragmentWithoutNavigationController_whenGetParentNavigationController_shouldReturnNull() {
        val layout = mockk<NavigationFragment>()
        val parent = mockk<PresentedFragment>()
        every { layout.parentFragment } returns parent
        every { parent.ncParentFragment } returns null
        val result = layout.getParentNavigationController()
        assertThat(result).isNull()
    }

    @Test
    fun givenParentFragmentWithoutNavigationControllerAndGrandParentWithNavigationController_whenGetParentNavigationController_shouldReturnGrandParent() {
        val layout = mockk<DialogFragment>()
        val parent = mockk<Fragment>()
        val grandParent = mockk<NavigationFragment>()
        val expected = mockk<NavigationFragment>()

        every { layout.parentFragment } returns parent
        every { parent.parentFragment } returns grandParent
        every { grandParent.ncParentFragment } returns expected

        val result = layout.getParentNavigationController()
        assertThat(result).isEqualTo(expected)
    }

}
