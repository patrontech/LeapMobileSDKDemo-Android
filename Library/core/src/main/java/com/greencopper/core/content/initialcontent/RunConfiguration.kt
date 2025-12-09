package com.greencopper.core.content.initialcontent

import com.greencopper.core.content.manager.ContentSchema
import com.greencopper.core.content.manager.ContentVersion
import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.e
import com.greencopper.toolkit.storage.StorageManager
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
public data class RunConfiguration internal constructor(val content: Content) {

    @Serializable
    public data class Content(
        val fileName: String? = null,
        internal val secret: String,
        val schema: ContentSchema,
        val version: ContentVersion,
        val project: String,
        val deprecatedProjects: List<String> = emptyList(),
    )

    public companion object {
        public fun build(storageManager: StorageManager, json: Json): RunConfiguration =
            runBlocking {
                withContext(Dispatchers.IO) {
                    val runConfigurationFile = storageManager.getAssetAsFile("content/runConfig.json")
                    val fileContent = try {
                        runConfigurationFile.readText()
                    } catch (e: Exception) {
                        App.log.e("Failed to read run configuration file", "RunConfiguration", e)
                        throw e
                    }

                    return@withContext try {
                        json.decodeFromString(serializer(), fileContent)
                    } catch (e: Exception) {
                        App.log.e("Failed to deserialize run configuration file : $fileContent", "RunConfiguration", e)
                        throw e
                    }
                }
            }
    }

}
