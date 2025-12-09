package com.greencopper.ticketing.providers.showclix.conditions

import com.greencopper.core.conditions.ConditionInfo
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.testmocks.setupTest
import com.greencopper.ticketing.providers.showclix.showclix
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.LazyResolver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ShowclixLoggedInConditionTest {

    private val showclixLoggedInCondition: ShowclixLoggedInCondition
    private val lazyLocalStorage: LazyResolver<LocalStorage>

    private val conditionData = ShowclixLoggedInCondition.ShowclixLoggedInConditionData(false)

    init {
        Toolkit.setupTest()
        lazyLocalStorage = LazyResolver.adhoc(LocalStorage("project"))
        showclixLoggedInCondition = ShowclixLoggedInCondition(lazyLocalStorage)
    }

    @Test
    fun verifyKey() {
        assertThat(ShowclixLoggedInCondition.key).isEqualTo(
            ConditionInfo.Key(
                "Ticketing.Showclix.LoggedIn",
                1
            )
        )
    }

    @Test
    fun checkWithNullToken_shouldReturnTrue() {
        assertThat(showclixLoggedInCondition.checkWith(conditionData)).isTrue
    }

    @Test
    fun checkWithEmptyToken_shouldReturnTrue() {
        token = ""
        assertThat(showclixLoggedInCondition.checkWith(conditionData)).isTrue
    }

    @Test
    fun checkWithBlankToken_shouldReturnTrue() {
        token = " "
        assertThat(showclixLoggedInCondition.checkWith(conditionData)).isTrue
    }

    @Test
    fun checkWithToken_shouldReturnFalse() {
        token = "token"
        assertThat(showclixLoggedInCondition.checkWith(conditionData)).isFalse
    }

    @Test
    fun checkFlowWithNullToken_shouldReturnTrue() {

        runTest {
            val first = showclixLoggedInCondition.checkWithFlow(conditionData).first()
            assertThat(first).isTrue
        }
    }

    @Test
    fun checkFlowWithEmptyToken_shouldReturnTrue() {
        token = ""

        runTest {
            val first = showclixLoggedInCondition.checkWithFlow(conditionData).first()
            assertThat(first).isTrue
        }
    }

    @Test
    fun checkFlowWithBlankToken_shouldReturnTrue() {
        token = " "

        runTest {
            val first = showclixLoggedInCondition.checkWithFlow(conditionData).first()
            assertThat(first).isTrue
        }
    }

    @Test
    fun checkFlowWithToken_shouldReturnFalse() {
        token = "token"

        runTest {
            val first = showclixLoggedInCondition.checkWithFlow(conditionData).first()
            assertThat(first).isFalse
        }
    }

    @Test
    fun deserialize_shouldReturnObject() {
        val deserializedData =
            showclixLoggedInCondition.deserialize(conditionData.encodeToJsonElement())

        assertThat(deserializedData).isEqualTo(conditionData)
    }

    private var token: String?
        get() = lazyLocalStorage.resolve().project.showclix.validationToken.value
        set(value) {
            lazyLocalStorage.resolve().project.showclix.validationToken.value = value
        }

}
