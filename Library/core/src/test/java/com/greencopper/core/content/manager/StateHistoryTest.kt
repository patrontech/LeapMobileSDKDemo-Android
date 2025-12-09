package com.greencopper.core.content.manager

import com.greencopper.core.data.KiboSerializable
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class StateHistoryTest {

    init {
        Toolkit.setupTest()
    }

    @Test
    fun whenAddingSimilarStates_dontAppend() {
        val stateHistory = StateHistory()
        assertThat(stateHistory.states).hasSize(1)
        assertThat(stateHistory.currentState).isInstanceOf(State.Created::class.java)
        stateHistory.append(State.Created())
        assertThat(stateHistory.states).hasSize(1)
        assertThat(stateHistory.currentState).isInstanceOf(State.Created::class.java)
    }

    @Test
    fun whenAddingDifferentState_shouldAppend() {
        val stateHistory = StateHistory()
        assertThat(stateHistory.states).hasSize(1)
        assertThat(stateHistory.currentState).isInstanceOf(State.Created::class.java)
        stateHistory.append(State.Processing())
        assertThat(stateHistory.states).hasSize(2)
        assertThat(stateHistory.currentState).isInstanceOf(State.Processing::class.java)
        assertThat(stateHistory.processedRecipeKeys).isNull()
    }

    @Test
    fun whenSerialized_canDeserialize() {
        //given
        val stateHistory = StateHistory()
        stateHistory.append(State.Processing())

        //when
        val restoredState =
            KiboSerializable.decodeFromString<StateHistory>(stateHistory.encodeToString())

        //then
        assertThat(stateHistory.currentState.javaClass).isEqualTo(restoredState.currentState.javaClass)
        assertThat(stateHistory.currentState.date.epochSecond).isEqualTo(restoredState.currentState.date.epochSecond)
        assertThat(stateHistory.states.size).isEqualTo(restoredState.states.size)
        assertThat(stateHistory.processedRecipeKeys?.size).isEqualTo(restoredState.processedRecipeKeys?.size)
    }
}