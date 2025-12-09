package com.greencopper.interfacekit.navigation.localstorage

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.interfacekit.common.interfaceKit
import com.greencopper.interfacekit.mocks.TestParameter
import com.greencopper.interfacekit.navigation.layout.ConcreteLayoutDataProvider
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ConcreteLayoutDataProviderTest : CoroutineTest(StandardTestDispatcher()) {

    init {
        Toolkit.setupTest()
    }

    private val localStorage: LocalStorage = App.resolve()
    private val layoutDataProvider = ConcreteLayoutDataProvider(localStorage, testScope)

    override fun afterEach() {
    }

    @Test
    fun addNonexistentKey_shouldSave() = runTest {
        val data = TestParameter("test", 2)
        layoutDataProvider.addLayoutData(1, data)
        delay(1000)

        val result = localStorage.app.interfaceKit.layoutData.value
        assertThat(result[1]).isEqualTo(data.encodeToString())
    }

    @Test
    fun addExistingKey_shouldNotSave() = runTest {
        val data = TestParameter("test", 2)
        layoutDataProvider.addLayoutData(1, data)
        delay(1000)
        layoutDataProvider.addLayoutData(1,
            TestParameter("test1", 3)
        )
        delay(1000)

        val result = localStorage.app.interfaceKit.layoutData.value
        assertThat(result[1]).isEqualTo(data.encodeToString())
    }

    @Test
    fun getExistingKey_shouldReturnData() = runTest {
        val data = TestParameter("test", 2)
        localStorage.app.interfaceKit.layoutData.value = mapOf(
            1 to data.encodeToString()
        )

        val result = layoutDataProvider.getLayoutData(1) {
            KiboSerializable.decodeFromString<TestParameter>(it)
        }
        assertThat(result).isEqualTo(data)
    }

    @Test
    fun getNonexistentKey_shouldReturnNull() = runTest {
        val result = layoutDataProvider.getLayoutData(1) {
            KiboSerializable.decodeFromString<TestParameter>(it)
        }
        assertThat(result).isNull()
    }

}
