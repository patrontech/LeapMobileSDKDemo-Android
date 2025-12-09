package com.greencopper.core.remotestate.models

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.TestLocalStorageContainer
import com.greencopper.core.localstorage.core
import com.greencopper.core.remotestate.remoteState
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.json.*
import org.assertj.core.api.Assertions.assertThat

import org.junit.jupiter.api.Test

internal class CustomRemoteStateTest {

    init {
        Toolkit.setupTest()
    }

    private val json: Json = App.resolve()

    @Test
    fun persistsToLocalStorage() {
        val custom = CustomRemoteState()
        custom.setCustom("myID", "disney", "xyz-abc")
        val localStorage = LocalStorage("test", TestLocalStorageContainer())
        localStorage.project.core.remoteState.custom.value = custom
        val jsonObject = localStorage.project.core.remoteState.custom.value.toJson()

        assertThat(jsonObject).isEqualTo(
            JsonObject(
                mapOf("disney" to JsonObject(
                    mapOf("myID" to JsonPrimitive("xyz-abc"))
                ))
            )
        )
    }

    @Test
    internal fun serializesToCorrectFormat() {
        val custom = CustomRemoteState()
        custom.setCustom("myID", "disney", "xyz-abc")
        val jsonObject = json.encodeToJsonElement(custom).jsonObject

        assertThat(jsonObject).isEqualTo(
            JsonObject(
                mapOf("disney" to JsonObject(
                    mapOf("myID" to JsonPrimitive("xyz-abc"))
                ))
            )
        )
    }

    @Test
    internal fun deserializesFromCorrectJsonFormat() {
        val jsonObject = JsonObject(
            mapOf("disney" to JsonObject(
                mapOf("myID" to JsonPrimitive("abc-123-xyz"))
            ))
        )
        val custom: CustomRemoteState = json.decodeFromJsonElement(jsonObject)

        assertThat(custom["myID", "disney"]).isEqualTo(JsonPrimitive("abc-123-xyz"))
    }
}