package com.greencopper.core.content.archive

import kotlinx.serialization.Serializable

@Serializable
internal data class VersionConfiguration(val version: Int, val schema: Int)