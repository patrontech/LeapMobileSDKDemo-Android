package com.greencopper.core.content.manager

import com.greencopper.core.data.KiboSerializable
import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.e
import com.greencopper.toolkit.storage.StorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

internal class ConcreteContentHistory(
    private val storageManager: StorageManager,
    private val json: Json
) : ContentHistory {
    private val persistedData: PersistedData = restoreData()

    private val _currentProject: MutableSharedFlow<String?> =
        MutableSharedFlow<String?>(
            replay = 1
        )

    override val currentProject: String?
        get() = persistedData.currentProject

    override val currentProjectFlow: SharedFlow<String?>
        get() = _currentProject

    private val _currentContent: MutableStateFlow<Content?> = MutableStateFlow(null)

    override var currentContent: Content?
        get() = _currentContent.value
        set(value) {
            persistedData.currentProject = value?.project
            runBlocking { saveHistory() }
            _currentContent.value = value
            _currentProject.tryEmit(value?.project)
        }

    override val currentContentFlow: Flow<Content?>
        get() = _currentContent

    override var forcedContent: Content? = null
        get() = persistedData.forcedContent
        set(value) {
            field = value
            persistedData.forcedContent = field
            runBlocking { saveHistory() }
        }

    override val contents: Set<Content>
        get() = persistedData.contents

    override suspend fun saveContent(content: Content) {
        persistedData.contents.add(content)
        saveHistory()
    }

    override suspend fun saveHistory() {
        val history = persistedData.encodeToString()
        val historyFile =
            File(storageManager.getFilesStorage(), HISTORY_FILENAME).apply { createNewFile() }
        try {
            historyFile.writeText(history)
        } catch (e: Exception) {
            App.log.e("Persisted data could not be saved: ${e.message}")
        }
    }

    private fun restoreData(): PersistedData = runBlocking {
        withContext(Dispatchers.IO) {
            val history = File(storageManager.getFilesStorage(), HISTORY_FILENAME)
            try {
                if (history.exists()) {
                    return@withContext KiboSerializable.decodeFromJsonElement<PersistedData>(json.parseToJsonElement(history.readText()))
                }
            } catch (e: Exception) {
                App.log.e("Persisted data could not be restored: ${e.message}")
            }

            return@withContext PersistedData(mutableSetOf())
        }
    }

    @Serializable
    data class PersistedData(
        val contents: MutableSet<Content>,
        var forcedContent: Content? = null,
        var currentProject: String? = null
    ) : KiboSerializable<PersistedData> {
        override fun getSerializer(): KSerializer<PersistedData> = serializer()
    }

    companion object {
        private const val HISTORY_FILENAME = "history.json"
    }
}
