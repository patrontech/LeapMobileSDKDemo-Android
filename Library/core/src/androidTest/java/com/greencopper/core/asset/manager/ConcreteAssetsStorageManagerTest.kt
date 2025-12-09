package com.greencopper.core.asset.manager

import androidx.test.platform.app.InstrumentationRegistry
import com.greencopper.core.asset.recipe.*
import com.greencopper.testmocks.*
import com.greencopper.testmocks.toolkit.MockLogging
import com.greencopper.testmocks.toolkit.MockStorageManager
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File

internal class ConcreteAssetsStorageManagerTest : CoroutineTest() {

    private val context = InstrumentationRegistry.getInstrumentation().context

    init {
        Toolkit.setupTest()
        bindProvider(context)
    }

    private val storageManager = MockStorageManager(
        projectTag = "project",
        projectFilesStorage = { File(context.filesDir, "project") }
    )
    private val configHolder = AssetsConfigurationHolder()

    private val assetsStorageManager = ConcreteAssetsStorageManager(
        context,
        configHolder,
        storageManager,
        MockLogging(),
    )

    override fun afterEach() {}

    @Test
    @DisplayName("Given assets directory exists, When calling getAssetsDirectory, Then the assets dir should be returned")
    fun getAssetsDirectoryShouldSucceed() {
        assertThat(assetsStorageManager.getAssetManager()).isEqualTo(context.assets)
    }

    @Test
    @DisplayName("Given a valid project, When calling getAssetsDownloadDirectory, Then the downloaded assets dir should be returned")
    fun getAssetsDownloadDirectoryShouldSucceed() {
        runTest {
            configHolder.currentConfiguration.value = AssetsConfiguration(
                emptyList(),
                "content/placeholder.png",
                "content/placeholder.png",
                "content/failedImage.png",
                "project",
            )
            assertThat(assetsStorageManager.getAssetsDownloadDirectory()).isDirectory
        }
    }

    @Test
    @DisplayName("Given an asset exists, When calling removeAssets, Then the asset should be deleted")
    fun removeAssetsShouldSucceed() {
        runTest {
            configHolder.currentConfiguration.value = AssetsConfiguration(
                listOf(Asset("new_asset.txt", "blank")),
                "content/placeholder.png",
                "content/placeholder.png",
                "content/failedImage.png",
                "project",
            )
            val downloadAssetsDir = File(context.filesDir, "project/assets").apply { mkdirs() }
            val assetFile = File(downloadAssetsDir, "new_asset.txt")
            assetFile.writeText("Hello World!")

            assetsStorageManager.removeAssets(
                configHolder.currentConfiguration.value!!.assets.map { it.name }.toSet()
            )
            assertThat(assetFile.exists()).isFalse
        }
    }
}
