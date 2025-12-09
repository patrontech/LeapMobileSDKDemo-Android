package com.greencopper.core.asset.recipe

import android.content.Context
import com.greencopper.core.asset.manager.AssetsManager
import com.greencopper.testmocks.core.MockAssetsManager
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.io.FileNotFoundException

internal class AssetsRecipeTest {

    private val context: Context = mockk(relaxed = true)
    private lateinit var assetsManager: AssetsManager
    private val assetsConfigurationHolder = AssetsConfigurationHolder()
    private lateinit var assetsRecipe: AssetsRecipe
    private val sourceDirectory = File("source")
    private val outputDirectory = File("output")

    init {
        Toolkit.setupTest(applicationContext = context)
    }

    @BeforeEach
    fun setupEach() {
        assetsManager = MockAssetsManager()
        assetsRecipe = AssetsRecipe(
            assetsManager,
            assetsConfigurationHolder
        )
        assetsConfigurationHolder.currentConfiguration.value = null
    }

    @AfterEach
    fun cleanUp() {
        sourceDirectory.deleteRecursively()
        outputDirectory.deleteRecursively()
    }

    @Test
    fun tryToProcess_shouldThrow_withFileNotBeingADirectory() {
        val fileNotBeingADirectory = File("test.txt")
        assertThrows<IllegalArgumentException> {
            runTest {
                assetsRecipe.tryToProcess(fileNotBeingADirectory, fileNotBeingADirectory)
            }
        }
    }

    @Test
    fun tryToApply_shouldThrow_withNonExistingContent() {
        assertThrows<FileNotFoundException> {
            runTest {
                assetsRecipe.tryToApply(File(""))
            }
        }
    }
}
