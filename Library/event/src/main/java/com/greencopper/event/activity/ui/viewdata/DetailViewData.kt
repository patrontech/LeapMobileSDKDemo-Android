package com.greencopper.event.activity.ui.viewdata

import com.greencopper.event.scheduleItem.ui.ScheduleItemViewData
import com.greencopper.interfacekit.favorites.Favoriteable
import com.greencopper.interfacekit.tags.DisplayableTag

internal data class DetailViewData<T>(
    override val itemId: T,
    val name: String,
    val subtitle: String?,
    val description: CharSequence?,
    val photo: String?,
    val scheduleItemList: List<ScheduleItemViewData>,
    val tags: List<DisplayableTag>,
    val widgetCollectionKey: String? = null,
) : Favoriteable<T>
