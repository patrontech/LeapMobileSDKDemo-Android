package com.greencopper.interfacekit.list.initializer

import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.shouldBe
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

internal class ListModeTest {

    init {
        Toolkit.setupTest()
    }

    private val json: Json = App.resolve()

    @Test
    fun grid_serializeAndDeserialize() {
        val listMode = ListMode.Grid(3)
        val deserialized = json.decodeFromString<ListMode>(json.encodeToString(listMode))

        listMode shouldBe deserialized
    }

    @Test
    fun table_serializeAndDeserialize() {
        val listMode = ListMode.Table(true)
        val deserialized = json.decodeFromString<ListMode>(json.encodeToString(listMode))

        listMode shouldBe deserialized
    }
}
