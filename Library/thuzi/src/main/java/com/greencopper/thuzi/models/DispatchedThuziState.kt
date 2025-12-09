package com.greencopper.thuzi.models

import com.greencopper.core.data.KiboSerializable
import kotlinx.serialization.*

@Serializable
public data class DispatchedThuziState(
    internal val registration: Registration = Registration(false),
    internal val answers: Map<String, String> = emptyMap(),
    internal val attendee: Attendee = Attendee(postalCode = null),
    @SerialName("virtual_access_cards")
    internal val virtualAccessCards: List<String> = emptyList(),
) : KiboSerializable<DispatchedThuziState> {

    public constructor(
        registered: Boolean,
        answers: Map<String, String> = emptyMap(),
        postalCode: String? = null,
        virtualAccessCards: List<String> = emptyList(),
    ) :
            this(Registration(registered), answers, Attendee(postalCode), virtualAccessCards)

    override fun getSerializer(): KSerializer<DispatchedThuziState> = serializer()

    public companion object {
        public const val dispatcherKey: String = "thuzi"
    }
}

@Serializable
public data class Registration(
    internal var isRegistered: Boolean,
)

@Serializable
public data class Attendee(
    internal var postalCode: String? = null,
)
