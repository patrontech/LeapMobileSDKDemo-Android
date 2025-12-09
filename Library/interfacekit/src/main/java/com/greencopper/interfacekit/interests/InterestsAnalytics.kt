package com.greencopper.interfacekit.interests

import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.metrics.events.screenName
import com.greencopper.core.metrics.labels.EventName
import com.greencopper.core.metrics.labels.EventParameter
import com.greencopper.core.metrics.labels.MappedMetrics
import com.greencopper.core.metrics.labels.itemId
import com.greencopper.core.metrics.labels.itemName
import com.greencopper.core.metrics.provider.MappedProvider
import com.greencopper.interfacekit.interests.recipe.Interest

internal class InterestSelected(
    private val itemName: String,
    private val itemId: String,
    private val screenName: String,
) : MappedMetrics {

    override fun track(provider: MappedProvider) {
        provider.track(EventName("interests_picker/select"), mapOf(
            EventParameter.itemName to itemName,
            EventParameter.itemId to itemId,
            EventParameter.screenName to screenName,
        ))
    }
}

internal class InterestUnselected(
    private val itemName: String,
    private val itemId: String,
    private val screenName: String,
) : MappedMetrics {

    override fun track(provider: MappedProvider) {
        provider.track(EventName("interests_picker/unselect"), mapOf(
            EventParameter.itemName to itemName,
            EventParameter.itemId to itemId,
            EventParameter.screenName to screenName,
        ))
    }
}

internal class InterestsPickerClosed(
    private val localizationService: LocalizationService,
    private val selectedItems: List<Interest>,
    private val screenName: String,
) : MappedMetrics {

    override fun track(provider: MappedProvider) {
        val selectedItemsValue = selectedItems.joinToString(", ") { item ->
            "${localizationService.getDefaultLocaleString(item.analyticsName)}(${item.id})"
        }

        provider.track(EventName("interests_picker/close"), mapOf(
            EventParameter("selectedItems") to selectedItemsValue,
            EventParameter.screenName to screenName,
        ))
    }
}
