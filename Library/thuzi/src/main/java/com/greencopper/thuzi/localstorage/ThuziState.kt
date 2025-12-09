package com.greencopper.thuzi.localstorage

import com.greencopper.core.data.KiboSerializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
public data class ThuziState(
    val answers: Map<String, String> = emptyMap(),
    val attendee: Attendee = Attendee(),
    val virtualAccessCards: List<String>? = emptyList(),
    val registered: Boolean = false,
) : KiboSerializable<ThuziState> {

    override fun getSerializer(): KSerializer<ThuziState> = serializer()
}

@Serializable
public data class Attendee(
    val postalCode: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
) : KiboSerializable<Attendee> {
    override fun getSerializer(): KSerializer<Attendee> = serializer()
}
