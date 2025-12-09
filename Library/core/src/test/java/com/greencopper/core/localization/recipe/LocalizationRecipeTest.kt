package com.greencopper.core.localization.recipe

import com.greencopper.core.data.writeToPath
import com.greencopper.core.localization.MockTranslationRepository
import com.greencopper.core.localization.translation.TranslationRepository
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.*
import java.io.File
import java.io.FileNotFoundException

internal class LocalizationRecipeTest {

    private val json = Json
    private lateinit var classUnderTest: LocalizationRecipe
    private lateinit var mockTranslationRepository : TranslationRepository
    private val sourceDirectory = File("source")
    private val outputDirectory = File("output")

    init {
        Toolkit.setupTest()

        cleanUp()
    }

    @BeforeEach
    fun setup() {
        mockTranslationRepository = MockTranslationRepository()
        classUnderTest = LocalizationRecipe(json, mockTranslationRepository)
        sourceDirectory.mkdir()
        outputDirectory.mkdir()
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
                classUnderTest.tryToProcess(fileNotBeingADirectory, fileNotBeingADirectory)
            }
        }
    }

    @Test
    fun tryToProcess_shouldThrow_withDirectoryWithNoConfig() {
        val directoryWithNoConfig = File(sourceDirectory, "file_name")
        directoryWithNoConfig.writeText("random_content")
        assertThrows<IllegalArgumentException> {
            runTest {
                classUnderTest.tryToProcess(sourceDirectory, sourceDirectory)
            }
        }
    }

    @Test
    fun tryToProcess_shouldThrow_withNonExistentOutPutDirectory() {
        val configFile = File(sourceDirectory, "config.json")
        val localizationConfiguration = LocalizationConfiguration(
            "",
            listOf("")
        )
        configFile.writeText(localizationConfiguration.encodeToString())
        assertThrows<FileNotFoundException> {
            runTest {
                classUnderTest.tryToProcess(
                    sourceDirectory,
                    File("random_invalid_path", "random_invalid_file")
                )
            }
        }
    }

    @Test
    fun tryToProcess_shouldThrow_withInvalidPath() {
        val configFile = File(sourceDirectory, "config.json")
        val localizationConfiguration = LocalizationConfiguration(
            "",
            listOf("")
        )
        configFile.writeText(localizationConfiguration.encodeToString())
        assertThrows<FileNotFoundException> {
            runTest {
                classUnderTest.tryToProcess(
                    sourceDirectory,
                    File("random_invalid_path", "random_invalid_file")
                )
            }
        }
    }

    @Test
    fun tryToProcess_shouldThrow_withInvalidLocalesFileName() {
        val configFile = File(sourceDirectory, "config.json")
        val localizationConfiguration = LocalizationConfiguration(
            "wrong_locales",
            listOf("wrong_locales")
        )
        configFile.writeText(localizationConfiguration.encodeToString())
        assertThrows<IllegalArgumentException> {
            runTest {
                classUnderTest.tryToProcess(
                    sourceDirectory,
                    sourceDirectory
                )
            }
        }
    }

    @Test
    fun tryToProcess_shouldThrow_withInvalidLocalesFileContent() {
        val localeName = "locale"
        val configFile = File(sourceDirectory, "config.json")
        val localFile = File(sourceDirectory, "$localeName.json")
        localFile.writeText("%_| not_json_conform @#%")
        val localizationConfiguration = LocalizationConfiguration(
            localeName,
            listOf(localeName)
        )
        configFile.writeText(localizationConfiguration.encodeToString())
        assertThrows<IllegalArgumentException> {
            runTest {
                classUnderTest.tryToProcess(
                    sourceDirectory,
                    sourceDirectory
                )
            }
        }
    }

    @Test
    fun tryToProcess_shouldThrow_withDefaultLocaleFileNotProvided() {
        val localeName = "locale"
        val configFile = File(sourceDirectory, "config.json")
        val localizationConfiguration = LocalizationConfiguration(
            "default_locale",
            listOf(localeName)
        )
        configFile.writeText(localizationConfiguration.encodeToString())
        assertThrows<IllegalArgumentException> {
            runTest {
                classUnderTest.tryToProcess(
                    sourceDirectory,
                    sourceDirectory
                )
            }
        }
    }

    @Test
    fun tryToProcess_onSuccess_shouldGenerateOutputAtPath() {
        val localeOne = "localeOne"
        val localeTwo = "localeTwo"
        val configFile = File(sourceDirectory, "config.json")
        val localOneFile = File(sourceDirectory, "$localeOne.json")
        val localTwoFile = File(sourceDirectory, "$localeTwo.json")
        localOneFile.writeText("{}")
        localTwoFile.writeText("{}")
        val localizationConfiguration = LocalizationConfiguration(
            localeOne,
            listOf(localeOne, localeTwo)
        )
        configFile.writeText(localizationConfiguration.encodeToString())
        assertDoesNotThrow {
            runTest {
                classUnderTest.tryToProcess(
                    sourceDirectory,
                    outputDirectory
                )
            }
        }
        assert(File(outputDirectory, "config.json").exists())
        assert(File(outputDirectory, "$localeOne.json").exists())
        assert(File(outputDirectory, "$localeTwo.json").exists())
    }

    @Test
    fun tryToApply_shouldThrow_withNonExistingContent() {
        assertThrows<FileNotFoundException> {
            runTest {
                classUnderTest.tryToApply(File(""))
            }
        }
    }

    @Test
    fun tryToApply_shouldThrow_withWronglyFormattedConfigContent() {
        val configFile = File(sourceDirectory, "config.json")
        configFile.writeText("random_content")

        assertThrows<SerializationException> {
            runTest {
                classUnderTest.tryToApply(sourceDirectory)
            }
        }
    }

    @Test
    fun tryToApply_shouldSuccess_withConformConfigAndBlankLocales() {

        val localizationConfiguration = LocalizationConfiguration(
            "default_locale",
            listOf()
        )
        val configFile = File(sourceDirectory, "config.json")
        localizationConfiguration.writeToPath(configFile)

        assertDoesNotThrow {
            runTest {
                classUnderTest.tryToApply(sourceDirectory)
            }
        }
    }
}
