package com.greencopper.interfacekit.interests.viewmodel

import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.LocalStorageProperty
import com.greencopper.core.remotestate.RemoteStateDispatcher
import com.greencopper.core.remotestate.RemoteStateEntry
import com.greencopper.interfacekit.common.interfaceKit
import com.greencopper.interfacekit.interests.recipe.InterestsConfiguration
import com.greencopper.interfacekit.interests.InterestsLayoutData
import com.greencopper.interfacekit.interests.recipe.InterestsConfigurationHolder
import com.toggl.komposable.architecture.ReduceResult
import com.toggl.komposable.architecture.Reducer
import com.toggl.komposable.extensions.withoutEffect
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement

internal class InterestsReducer(
    private val localizationService: LocalizationService,
    private val localStorage: LocalStorage,
    private val configHolder: InterestsConfigurationHolder,
    private val remoteStateDispatcher: RemoteStateDispatcher,
    private val json: Json,
) : Reducer<InterestsState, InterestsAction> {

    private val config: InterestsConfiguration?
        get() = configHolder.currentConfiguration.value

    private val localStorageInterestIds: LocalStorageProperty<Set<String>>
        get() = localStorage.project.interfaceKit.interestIds

    override fun reduce(
        state: InterestsState,
        action: InterestsAction
    ): ReduceResult<InterestsState, InterestsAction> {

        return when (action) {
            is InterestsAction.LoadInitialState -> {
                state.copy(
                    title = localizationService.getString(action.data.title),
                    subtitle = localizationService.getString(action.data.subtitle),
                    buttonTitle = localizationService.getString("common.confirm"),
                    interests = getInterestsStates()
                ).withoutEffect()
            }
            is InterestsAction.InterestTapped -> {
                if (action.isSelected) {
                    localStorageInterestIds.value -= action.id
                } else {
                    localStorageInterestIds.value += action.id
                }

                remoteStateDispatcher.dispatch(InterestsRemoteState(localStorageInterestIds.value, json))

                state.copy(interests = getInterestsStates()).withoutEffect()
            }
            is InterestsAction.InterestsClosed -> state.withoutEffect()
        }
    }

    private fun getInterestsStates(): List<InterestState> {
        val storedInterestIds = localStorageInterestIds.value
        return config?.interests
            ?.sortedBy { it.order }
            ?.map { interest ->
                InterestState(
                    id = interest.id,
                    title = localizationService.getString(interest.name),
                    selected = storedInterestIds.contains(interest.id),
                )
            } ?: emptyList()
    }
}

@Serializable
internal sealed class InterestsAction {

    data class LoadInitialState(val data: InterestsLayoutData) : InterestsAction()

    data class InterestTapped(val id: String, val isSelected: Boolean) : InterestsAction()

    data object InterestsClosed : InterestsAction()
}

internal class InterestsRemoteState(interestIds: Set<String>, json: Json) :
    RemoteStateEntry(
        key = "interests",
        value = json.encodeToJsonElement(interestIds),
        isUrgent = false,
    )
