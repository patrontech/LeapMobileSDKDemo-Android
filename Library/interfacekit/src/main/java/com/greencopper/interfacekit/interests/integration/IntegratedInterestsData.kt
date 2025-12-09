package com.greencopper.interfacekit.interests.integration

import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.empty.EmptyPage
import com.greencopper.interfacekit.filtering.filteringbar.FilteringButton
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
public data class IntegratedInterestsData(
    val activeOnLoad: Boolean = false,
    val filteringButton: FilteringButton? = null,
    val emptyPage: EmptyPage,
) : KiboSerializable<IntegratedInterestsData> {
    override fun getSerializer(): KSerializer<IntegratedInterestsData> = serializer()
}
