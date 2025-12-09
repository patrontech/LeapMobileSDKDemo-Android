package com.greencopper.core.content.projectswitcher

import com.greencopper.core.data.KiboSerializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
public data class ProjectParams(val project: String, val otaApiUrl: String) :
    KiboSerializable<ProjectParams> {

    override fun getSerializer(): KSerializer<ProjectParams> = serializer()
}