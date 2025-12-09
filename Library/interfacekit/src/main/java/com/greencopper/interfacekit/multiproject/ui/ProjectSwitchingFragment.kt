package com.greencopper.interfacekit.multiproject.ui

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.core.metrics.labels.*
import com.greencopper.core.services.track
import com.greencopper.interfacekit.activityViewModel
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.color.ScreenColor
import com.greencopper.interfacekit.databinding.FragmentProjectSwitchingBinding
import com.greencopper.interfacekit.metrics.projectSwitching
import com.greencopper.interfacekit.multiproject.ProjectSwitchingLayoutData
import com.greencopper.interfacekit.multiproject.viewmodel.ProjectSwitchingViewModel
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayout
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.fragment.ParameterizedFragment
import com.greencopper.interfacekit.ui.viewBinding
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.logging.e
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal class ProjectSwitchingFragment :
    ParameterizedFragment<ProjectSwitchingLayoutData>,
    OnboardingPageLayout {

    constructor(constructorData: ProjectSwitchingLayoutData?) : super(constructorData)

    @Deprecated("To conform to system and provide to FragmentFactory an empty constructor accessed by reflection")
    constructor() : super(null)

    override val onboardingScreenViewEvent: ScreenViewEvent? by lazy {
        data.onboardingPageLayoutData?.onboardingAnalytics?.let {
            val screen = Screen.projectSwitching(it.screenName)
            val parameters =
                mapOf(EventParameter.itemCategory to it.featureName.plus(" Onboarding"))
            ScreenViewEvent(screen, parameters)
        }
    }

    override val onboardingPageId: String by lazy {
        data.onboardingPageLayoutData?.pageId ?: throw createPageIdMissingException()
    }

    override val binding: FragmentProjectSwitchingBinding by viewBinding(
        FragmentProjectSwitchingBinding::inflate
    )
    override val screenColor: ScreenColor get() = InterfaceKitColor.projectSwitching

    private val viewModel: ProjectSwitchingViewModel by activityViewModel()
    private val localizationService: LocalizationService by lazy { App.resolve() }

    private val routeController: RouteController by lazy { App.resolve() }

    override fun restoreData(encodedData: String): ProjectSwitchingLayoutData =
        KiboSerializable.decodeFromString(encodedData)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.resetViewModel()
        disableBackButton(view)
        with(binding.projectSwitchingLabelView) {
            text = localizationService.getString("interfaceKit.project_switching.loading")
            setFont(InterfaceKitTextStyle.projectSwitching.label)
            setTextColor(InterfaceKitColor.projectSwitching.label)
        }
        binding.projectSwitchingProgressBar.indeterminateTintList =
            ColorStateList.valueOf(InterfaceKitColor.projectSwitching.activityIndicator)
        lifecycleScope.launch {
            viewModel.error.collectLatest {
                it?.let {
                    routeController.showAlert(
                        message = localizationService.getString("common.an_error_occured"),
                        positiveText = localizationService.getString("common.ok"),
                        isCancelable = false,
                        onPositiveClicked = {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                    )
                    App.log.e(
                        message = "An error occurred during switching project",
                        throwable = it
                    )
                }
            }
        }
        viewModel.switchProject(data.project)
    }

    private fun disableBackButton(view: View) {
        isCancelable = false
        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return@OnKeyListener true
                }
            }
            false
        })
    }

    override fun onResume() {
        super.onResume()
        App.track(
            ScreenViewEvent(
                Screen.Companion.projectSwitching(data.analytics.screenName),
                parameters = mapOf(
                    EventParameter.itemId to data.project.content.project,
                    EventParameter.itemName to data.project.name,
                )
            )
        )
    }
}
