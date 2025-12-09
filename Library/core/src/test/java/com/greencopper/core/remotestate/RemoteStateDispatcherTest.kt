package com.greencopper.core.remotestate

import com.greencopper.testmocks.MockRemoteStateDispatcher
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.Toolkit
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class RemoteStateDispatcherTest {
    private val json: Json

    init {
        Toolkit.setupTest()
        json = App.resolve<Json>()
    }

    @Test
    fun deleteRemoteState() {
        val remoteState = MockRemoteStateDispatcher(json)
        remoteState.delete("deletionKey", isUrgent = true, project = "deletionProject")
        assertThat(remoteState.dispatchProject).isEqualTo("deletionProject")
        assertThat(remoteState.dispatchedEntry).isNotNull
        assertThat(remoteState.dispatchedEntry?.value).isNull()
    }

    @Test
    fun dispatchTypedSerializedValue() {
        val remoteState = MockRemoteStateDispatcher(json)
        remoteState.dispatch(
            "dispatchKey",
            value = 12,
            domain = RemoteStateEntry.Domain.PROJECT,
            isUrgent = true,
            project = "dispatchProject"
        )
        assertThat(remoteState.dispatchProject).isEqualTo("dispatchProject")
        assertThat(remoteState.dispatchedEntry).isNotNull
        assertThat(remoteState.dispatchedEntry?.value).isEqualTo(JsonPrimitive(12))
    }

    @Test
    fun dispatchCustom() {
        val remoteState = MockRemoteStateDispatcher(json)
        remoteState.dispatchCustom(
            "customKey",
            value = "custom",
            container = "leap",
            isUrgent = false,
        )
        assertThat(remoteState.dispatchProject).isNull()
        assertThat(remoteState.dispatchedEntry).isNotNull
        assertThat(remoteState.dispatchedEntry?.value).isEqualTo(JsonPrimitive("custom"))
    }
}