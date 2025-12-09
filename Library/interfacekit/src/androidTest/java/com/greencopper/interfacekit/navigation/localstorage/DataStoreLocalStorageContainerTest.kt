package com.greencopper.interfacekit.navigation.localstorage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localstorage.LocalStorageKey
import com.greencopper.interfacekit.navigation.localStorage.DataStoreLocalStorageContainer
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class DataStoreLocalStorageContainerTest : CoroutineTest() {

    init {
        Toolkit.setupTest()
    }

    private val datastoreName = "datastore_name"
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val dataStoreFile = context.preferencesDataStoreFile(datastoreName)
    private val testDataStore: DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { dataStoreFile }
        )
    private val storageContainer = DataStoreLocalStorageContainer(testDataStore)

    private val storageKey = LocalStorageKey("data")

    override fun afterEach() {
        dataStoreFile.delete()
    }

    @Test
    fun addData_shouldSave() {
        val data = TestParameter("data", 2).encodeToString()
        storageContainer.setJSON(storageKey, data)

        runTest {
            val result = testDataStore.data.first()[stringPreferencesKey(storageKey.toString())]?.toString()
            assertThat(result).isEqualTo(data)
        }

    }

    @Test
    fun getKnownKey_shouldReturnData() {
        runTest {
            testDataStore.edit { pref ->
                pref[stringPreferencesKey(storageKey.toString())] = "value1"
            }
        }

        val result = storageContainer.getJSON(storageKey)
        assertThat(result).isEqualTo("value1")
    }

    @Test
    fun getUnknownKey_shouldReturnNull() {
        val data = TestParameter("data", 2).encodeToString()
        storageContainer.setJSON(storageKey, data)

        val result = storageContainer.getJSON(LocalStorageKey("data1"))
        assertThat(result).isNull()
    }

    @Test
    fun setNull_withExistingData_shouldClean() {
        val data = TestParameter("data", 2).encodeToString()
        storageContainer.setJSON(storageKey, data)
        storageContainer.setJSON(storageKey, null.toString())

        val result = storageContainer.getJSON(storageKey)
        assertThat(result).isNull()
    }

    @Test
    fun setNull_withNonexistentData_shouldNotCrash() {
        assertDoesNotThrow {
            storageContainer.setJSON(storageKey, null.toString())

            val result = storageContainer.getJSON(storageKey)
            assertThat(result).isNull()
        }
    }

}

@Serializable
private data class TestParameter(val title: String, val version: Int) :
    KiboSerializable<TestParameter> {
    override fun getSerializer(): KSerializer<TestParameter> = serializer()
}
