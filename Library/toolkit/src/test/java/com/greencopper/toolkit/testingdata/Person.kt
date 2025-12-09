package com.greencopper.toolkit.testingdata

import kotlinx.serialization.Serializable

@Serializable
internal data class Person(val name: String, val age: Int, val location: String)