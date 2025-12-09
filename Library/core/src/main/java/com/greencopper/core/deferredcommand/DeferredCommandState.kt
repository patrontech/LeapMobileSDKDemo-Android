package com.greencopper.core.deferredcommand

import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.encodeToString
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.*

@Serializable
public data class DeferredCommandState(
    public val key: DeferredCommandKey,
    /**
     * [data] can be anything. It does *not* have to be JSON,
     * though it usually will be. It could be an image, a text file,
     * whatever.
     *
     * What exactly it is will depend on the [key] and its
     * corresponding [DeferredCommand].
     */
    public val data: ByteArray? = null
) {
    public companion object {
        public inline fun <reified T> create(
            key: DeferredCommandKey,
            state: T,
            json: Json = App.resolve()
        ): DeferredCommandState =
            DeferredCommandState(key, json.encodeToString(state).toByteArray())
    }

    public val id: String = UUID.randomUUID().toString()

    public inline fun <reified T> get(json: Json = App.resolve()): T? =
        data?.let {
            json.decodeFromString(String(it))
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as DeferredCommandState
        return id == other.id
    }

    override fun hashCode(): Int =
        id.hashCode()
}
