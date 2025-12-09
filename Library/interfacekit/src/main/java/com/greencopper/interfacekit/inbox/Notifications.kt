package com.greencopper.interfacekit.inbox

import com.greencopper.core.data.KiboSerializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
public data class Notifications(val items: List<Notification>) :
    KiboSerializable<Notifications> {
    override fun getSerializer(): KSerializer<Notifications> = serializer()

    @Serializable
    public data class Notification(
        val id: String,
        val title: String,
        val message: String? = null,
        val date: String,
        val onTap: OnTap? = null,
    ) {
        @Serializable
        public data class OnTap(val routeLink: String, val analytics: Analytics) {

            @Serializable
            public data class Analytics(val itemName: String? = null, val itemId: String)
        }
    }
}