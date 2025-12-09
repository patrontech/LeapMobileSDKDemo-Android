package com.greencopper.interfacekit.interests.viewmodel

import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.interfacekit.common.interfaceKit
import com.greencopper.interfacekit.interests.InterestSelected
import com.greencopper.interfacekit.interests.InterestUnselected
import com.greencopper.interfacekit.interests.InterestsLayoutData
import com.greencopper.interfacekit.interests.InterestsPickerClosed
import com.greencopper.interfacekit.interests.recipe.Interest
import com.greencopper.interfacekit.interests.recipe.InterestsConfigurationHolder
import com.greencopper.interfacekit.metrics.interestsPicker
import com.toggl.komposable.architecture.ReduceResult
import com.toggl.komposable.architecture.Reducer
import com.toggl.komposable.extensions.withoutEffect

internal class InterestsAnalyticsReducer(
    private val metricsService: AggregateMetricsService,
    private val localizationService: LocalizationService,
    private val configHolder: InterestsConfigurationHolder,
    private val localStorage: LocalStorage,
    private val layoutData: InterestsLayoutData,
) : Reducer<InterestsState, InterestsAction> {

    override fun reduce(
        state: InterestsState,
        action: InterestsAction
    ): ReduceResult<InterestsState, InterestsAction> {
        return state.also {
            when (action) {
                is InterestsAction.LoadInitialState -> {
                    if (action.data.onboardingPageLayoutData == null) { // onboarding handles its own screenview events
                        metricsService.track(ScreenViewEvent(Screen.interestsPicker(action.data.analytics.screenName)))
                    }
                }
                is InterestsAction.InterestTapped -> {
                    getInterestData(action.id)?.let { interest ->
                        if (action.isSelected) {
                            metricsService.track(
                                InterestUnselected(
                                    interest.analyticsName,
                                    interest.id,
                                    layoutData.analytics.screenName
                                )
                            )
                        } else {
                            metricsService.track(
                                InterestSelected(
                                    interest.analyticsName,
                                    interest.id,
                                    layoutData.analytics.screenName
                                )
                            )
                        }
                    }
                }
                is InterestsAction.InterestsClosed -> {
                     metricsService.track(InterestsPickerClosed(localizationService, getSelectedInterests(), layoutData.analytics.screenName))
                }
            }
        }.withoutEffect()
    }

    private fun getInterestData(id: String): Interest? =
        configHolder.currentConfiguration.value?.interests?.firstOrNull { it.id == id }

    private fun getSelectedInterests(): List<Interest> {
        val interestsConfig = configHolder.currentConfiguration.value?.interests ?: emptyList()
        return localStorage.project.interfaceKit.interestIds.value.mapNotNull { selectedInterestId ->
            interestsConfig.firstOrNull { it.id == selectedInterestId }
        }
    }
}
