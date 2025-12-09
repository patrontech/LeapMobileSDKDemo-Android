package com.greencopper.interfacekit.multiproject.ui

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.core.metrics.labels.EventParameter
import com.greencopper.core.metrics.labels.itemCategory
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.color.ScreenColor
import com.greencopper.interfacekit.databinding.FragmentProjectSwitcherBinding
import com.greencopper.interfacekit.metrics.projectSwitcher
import com.greencopper.interfacekit.metrics.projectSwitcherScreenClass
import com.greencopper.interfacekit.multiproject.*
import com.greencopper.interfacekit.multiproject.viewmodel.ProjectSwitcherViewModel
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayout
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.fragment.ParameterizedFragment
import com.greencopper.interfacekit.ui.viewBinding
import com.greencopper.interfacekit.viewModel
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy

internal class ProjectSwitcherFragment :
    ParameterizedFragment<ProjectSwitcherLayoutData>,
    OnboardingPageLayout {

    constructor(constructorData: ProjectSwitcherLayoutData?) : super(constructorData)

    @Deprecated("To conform to system and provide to FragmentFactory an empty constructor accessed by reflection")
    constructor() : super(null)

    override val onboardingScreenViewEvent: ScreenViewEvent? by lazy {
        data.onboardingPageLayoutData?.onboardingAnalytics?.let {
            val screen = Screen.projectSwitcher(it.screenName)
            val parameters =
                mapOf(EventParameter.itemCategory to it.featureName.plus(" Onboarding"))
            ScreenViewEvent(screen, parameters)
        }
    }

    override val onboardingPageId: String by lazy {
        data.onboardingPageLayoutData?.pageId ?: throw createPageIdMissingException()
    }

    override val binding: FragmentProjectSwitcherBinding by viewBinding(
        FragmentProjectSwitcherBinding::inflate
    )
    override val screenColor: ScreenColor get() = InterfaceKitColor.projectSwitcher

    private val viewModel: ProjectSwitcherViewModel by viewModel()
    private val localizationService: LocalizationService by App.lazy()
    private val metricsService: AggregateMetricsService by App.lazy()
    private val routeController: RouteController by App.lazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.setItems(data)

        binding.apply {
            rvMultiProjectSwitcher.layoutManager = LinearLayoutManager(requireContext())
            rvMultiProjectSwitcher.adapter = ProjectSwitcherAdapter(
                this@ProjectSwitcherFragment,
                viewModel,
                localizationService,
                metricsService,
                data.onboardingPageLayoutData?.onboardingAnalytics?.screenName ?: data.analytics?.screenName
                ?: Screen.projectSwitcherScreenClass(),
            )

            bMultiProjectSwitcherContinue.text = localizationService.getString("common.continue")
            bMultiProjectSwitcherContinue.setOnClickListener {

                if (viewModel.canSwitchProject()) {
                    data.onboardingPageLayoutData?.let {
                        onboardingPageDelegate?.pageDidComplete(it.pageId, true)
                    }
                    showProjectSwitching()
                }
            }
            bMultiProjectSwitcherContinue.setFont(InterfaceKitTextStyle.projectSwitcher.continueButton)
        }

        bindColors()
    }

    private fun showProjectSwitching() {
        val route = computeProjectSwitchingRoute()
        routeController.redirect(route, this@ProjectSwitcherFragment)
    }

    override fun onResume() {
        super.onResume()
        data.analytics?.let {
            metricsService.track(ScreenViewEvent(Screen.projectSwitcher(it.screenName)))
        }
    }

    private fun computeProjectSwitchingRoute(): Route.Present {
        val project = data.projects.first { it.content.project == viewModel.selectedItemId }
        val params = ProjectSwitchingData(
            ProjectSwitchingData.Project(
                name = project.name,
                date = ProjectSwitchingData.Project.ProjectDate(
                    project.date?.start,
                    project.date?.end
                ),
                image = project.image,
                content = ProjectSwitchingData.Project.Content(
                    project.content.project,
                    project.content.otaApiUrl
                )
            ),
            ScreenNameAnalytics("Project Switching")
        )
        return Route.Present(
            FeatureInfo(
                ProjectSwitchingInitializer.key,
                params.encodeToJsonElement()
            )
        )
    }

    override fun restoreData(encodedData: String): ProjectSwitcherLayoutData =
        KiboSerializable.decodeFromString(encodedData)

    private fun bindColors() {
        binding.apply {
            val colors = InterfaceKitColor.projectSwitcher
            backgroundMultiProjectSwitcher.setBackgroundColor(colors.background)
            bMultiProjectSwitcherContinue.setBackgroundColor(colors.continueButton.background)
            bMultiProjectSwitcherContinue.setTextColor(colors.continueButton.text)
        }
    }
}
