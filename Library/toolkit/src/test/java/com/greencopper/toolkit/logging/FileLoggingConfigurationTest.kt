package com.greencopper.toolkit.logging

import com.greencopper.testmocks.runUntilEventually
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.logging.multilogging.LoggingException
import com.greencopper.toolkit.logging.multilogging.MultiLoggingConfigurationsImpl
import com.greencopper.toolkit.logging.multilogging.configurations.FileLoggingConfiguration
import com.greencopper.toolkit.logging.multilogging.configurations.TagFileLoggingConfiguration
import com.greencopper.toolkit.storage.StorageManager
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.io.File
import java.io.IOException
import java.time.Duration

/** This test only exists to test the write functionality of the [FileLoggingConfiguration],
 * not the Logging interface, if you want to see it, go to [MultiLoggingConfigurationTest]
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class FileLoggingConfigurationTest {

    private val loggingImpl: Logging =
        MultiLoggingConfigurationsImpl()

    private val storageManager: StorageManager

    init {
        Toolkit.setupTest()
        storageManager = App.resolve()
    }

    @BeforeAll
    fun cleanup() {
        loggingImpl.removeAllConfigurations()
    }

    private val cacheFolder = runBlocking { storageManager.getCacheStorage() }

    @Test
    fun whenLogging_fileShouldContainLog() {
        val fileName = "test_fileLogging"
        val logConfig = FileLoggingConfiguration(File(cacheFolder, "log"), fileName, false)
        loggingImpl.addConfiguration(logConfig)
        loggingImpl.d("DebugTest")
        loggingImpl.d("DebugTestException", throwable = IOException())
        assertContains(logConfig.file, "DebugTest")
        assertContains(logConfig.file, ": DebugTestException\njava.io.IOException")
    }

    @Test
    fun whenLoggingAsync_fileShouldContainLog() {
        val fileName = "test_fileLoggingAsync"
        val logConfig = FileLoggingConfiguration(File(cacheFolder, "log"), fileName)
        loggingImpl.addConfiguration(logConfig)
        loggingImpl.d("DebugTest")
        runUntilEventually(Duration.ofMillis(200), "Async logging didn't work") {
            checkContains(logConfig.file, "DebugTest")
        }
    }

    @Test
    fun whenCreatingFileLogging_withExistingParentFile_shouldFail() {
        val fileName = "test_fileLoggingFailure_withWrongBaseDirectory"
        // Create it as a file so that the directory can't be created.
        val baseDir = File(cacheFolder, "log").apply { createNewFile() }
        val exception = assertThrows<LoggingException.IOInputException> {
            FileLoggingConfiguration(baseDir, fileName, false)
        }
        assertThat(exception.message).isEqualTo("[LoggingException] IO Error with file parameters")
    }

    @Test
    fun whenCreatingFileLogging_withExistingFile_shouldFail() {
        val fileName = "test_fileLoggingFailure_withWrongFilename"
        val baseDir = File(cacheFolder, "log").apply { mkdir() }
        // Create it as a directory so that the file can't be created.
        File(baseDir, "$fileName.log").mkdir()
        assertThrows<LoggingException.IOInputException> {
            FileLoggingConfiguration(baseDir, fileName, false)
        }
    }

    @Test
    fun whenLogging_withTagRestriction_shouldOnlyContainTag() {
        val fileName = "test_fileLogging_withRestrictingTag"
        val tagFileLoggingConfiguration =
            TagFileLoggingConfiguration(File(cacheFolder, "log"), fileName, "HTTP", false)
        loggingImpl.addConfiguration(tagFileLoggingConfiguration)
        loggingImpl.d("DebugTestNotInLog", "TestTag")
        loggingImpl.d("DebugTestInLog", "HTTP")
        loggingImpl.d("DebugTest3", "TestTag")
        loggingImpl.d("DebugTestWithConfigInLog", "HTTP.Config")
        val logFile = tagFileLoggingConfiguration.file
        assertNotContains(logFile, "DebugTestNotInLog")
        assertContains(logFile, "DebugTestInLog")
        assertNotContains(logFile, "DebugTest3")
        assertContains(logFile, "DebugTestWithConfigInLog")
    }

    @Test
    fun whenLoggingAsync_withTagRestriction_shouldOnlyContainTag() {
        val fileName = "test_fileLogging_withRestrictingTag"
        val tagFileLoggingConfiguration =
            TagFileLoggingConfiguration(File(cacheFolder, "log"), fileName, "HTTP")
        loggingImpl.addConfiguration(tagFileLoggingConfiguration)
        loggingImpl.d("DebugTestInLog", "HTTP")
        loggingImpl.d("DebugTestNotInLog", "TestTag")
        loggingImpl.d("DebugTest3", "TestTag")
        loggingImpl.d("DebugTestWithConfigInLog", "HTTP.Config")
        val logFile = tagFileLoggingConfiguration.file
        runUntilEventually(Duration.ofMillis(500), "Async logging didn't work") {
            checkContains(logFile, "DebugTestInLog")
        }
        assertNotContains(logFile, "DebugTestNotInLog")
        assertNotContains(logFile, "DebugTest3")
        assertContains(logFile, "DebugTestWithConfigInLog")
    }

    private fun assertContains(file: File, text: String) {
        assertThat(checkContains(file, text))
            .withFailMessage("File ${file.path} does not contain $text")
            .isTrue
    }

    private fun assertNotContains(file: File, text: String) {
        assertThat(checkContains(file, text))
            .withFailMessage("File ${file.path} should not contain $text")
            .isFalse
    }

    private fun checkContains(file: File, text: String): Boolean {
        return if (file.exists() && file.isFile) {
            file.readText().contains(text)
        } else {
            false
        }
    }

    @AfterEach
    fun tearDown() {
        File(cacheFolder, "log").deleteRecursively()
    }
}
