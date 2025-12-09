package com.greencopper.toolkit.zip

import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import java.io.File

internal class Zip4jClientTest : CoroutineTest(UnconfinedTestDispatcher()) {

    init {
        Toolkit.setupTest()
    }

    private val zipClient: ZipClient = Zip4jClient(testScope)

    private val directoryToZipPath = "src/test/res/directoryToZip"
    private val directoryToZip = File(directoryToZipPath)

    private val zippedFilePath = "src/test/res/zippedFile.zip"
    private val zippedFile = File(zippedFilePath)

    private val unzipDestinationPath = "src/test/res/unzipDestination"
    private val unzipDestination = File(unzipDestinationPath)

    private val otaFile = File("src/test/res/content_v13.zip")
    private val otaContent = File("src/test/res/otaContent")

    override fun afterEach() {
        try {
            zippedFile.delete()
            unzipDestination.deleteRecursively()
            otaContent.deleteRecursively()
        } catch (t: Throwable) {
            println("AfterEach Zip fail ${t.message} \n ${t.printStackTrace()} ")
        }
    }

    private val textFile: File = File(unzipDestination, "directoryToZip/textFile.txt")
    private val textFileContent = "This is a file with text in it!"

    @Test
    fun whenZipping_withPassword_shouldUnzipWithPassword() {
        val password = "password"
        // Zip
        assertThat(directoryToZip).isDirectory
        runTest {
            val zipResult = zipClient.zip(directoryToZip, zippedFile, password)
            assertThat(zipResult).isEqualTo(zippedFile)
        }

        // Unzip
        runTest {
            val unZipResult =
                zipClient.unZipEncryptedFile(zippedFile, unzipDestination, password, false)
            assertThat(unZipResult).isEqualTo(unzipDestination)
            assertThat(textFile).exists()
            assertThat(textFile.readText()).contains(textFileContent)
        }

    }

    @Test
    fun whenZipping_withFakeDirectory_shouldThrow() {
        // Zip
        val directory = File("src/test/res/notARealDirectory")
        assertThat(directory).doesNotExist()
        assertThrows<ZipException.InputException> {
            runTest {
                zipClient.zip(directory, zippedFile, "password")
            }
        }
    }

    @Test
    fun whenUnzipping_nonExistentFile_shouldThrow() {
        assertThrows<UnZipException> {
            runTest {
                zipClient.unZipEncryptedFile(zippedFile, unzipDestination, "password", false)
            }
        }
    }

    @Test
    fun whenUnzipping_contentFile_shouldSucceed() {
        val password = "content_v13f0c030f1aa654aafbc206"
        runTest {
            val unzipResult = zipClient.unZipEncryptedFile(otaFile, otaContent, password, false)
            assertThat(unzipResult).exists()
            assertThat(unzipResult.list()?.firstOrNull()).isEqualTo("config.json")
        }
    }

    @Test
    fun whenUnzipping_withWrongPassword_shouldThrow() {
        // Zip
        assertThat(directoryToZip).isDirectory
        runTest {
            val zipResult = zipClient.zip(directoryToZip, zippedFile, "password")
            assertThat(zipResult).isEqualTo(zippedFile)
        }
        // Unzip
        assertThrows<UnZipException.WrongPasswordException> {
            runTest {
                zipClient.unZipEncryptedFile(zippedFile, unzipDestination, "wrongPassword", false)
            }
        }
    }

    @Test
    fun whenUnzippingEncrypted_withoutPassword_shouldThrow() {
        // Zip
        assertThat(directoryToZip).isDirectory
        runTest {
            val zipResult = zipClient.zip(directoryToZip, zippedFile, "password")
            assertThat(zipResult).isEqualTo(zippedFile)
        }

        // Unzip
        assertThrows<UnZipException> {
            runTest {
                zipClient.unZipEncryptedFile(zippedFile, unzipDestination, "", false)
            }
        }
    }

    @Test
    fun whenUnzippingNonEncrypted_withPassword_shouldNotThrow() {
        // Zip
        assertThat(directoryToZip).isDirectory
        runTest {
            val zipResult = zipClient.zip(directoryToZip, zippedFile, "password")
            assertThat(zipResult).isEqualTo(zippedFile)
        }

        // Unzip
        assertDoesNotThrow {
            runTest {
                zipClient.unZipEncryptedFile(zippedFile, unzipDestination, "password", false)
            }
        }
    }

    @Test
    fun whenZipping_toExistingDestination_shouldThrow() {
        runTest {
            // Zip
            assertThat(directoryToZip).isDirectory
            zippedFile.createNewFile()
            assertThrows<ZipException.UnknownException> {
                zipClient.zip(directoryToZip, zippedFile, "password")
            }
        }
    }

    @Test
    @DisplayName("Given a valid zip file and allowFailure is false, When calling unZipEncryptedFile, Then it does not throw")
    fun unZipWithAllowFailureShouldNotThrow() {
        // Zip
        assertThat(directoryToZip).isDirectory
        runTest {
            val zipResult = zipClient.zip(directoryToZip, zippedFile, "password")
            assertThat(zipResult).isEqualTo(zippedFile)
        }
        // Unzip
        assertDoesNotThrow {
            runTest {
                zipClient.unZipEncryptedFile(zippedFile, unzipDestination, "wrongPassword", true)
            }
        }
    }

    @Test
    @DisplayName("Given a valid zip file with no encryption, When calling unZip, Then it succeeds")
    fun unZipShouldSucceed() {
        // Zip
        assertThat(directoryToZip).isDirectory
        runTest {
            val zipResult = zipClient.zip(directoryToZip, zippedFile, null)
            assertThat(zipResult).isEqualTo(zippedFile)
        }
        // Unzip
        runTest {
            zipClient.unZipFile(zippedFile, unzipDestination, false)
            assertThat(textFile).exists()
            assertThat(textFile.readText()).contains(textFileContent)
        }
    }

    @Test
    @DisplayName("Given a invalid zip file, When calling unZip, Then it throws IllegalArgumentException")
    fun unZipShouldFail() {
        assertThrows<IllegalArgumentException> {
            runTest {
                zipClient.unZipFile(zippedFile, unzipDestination, false)
            }
        }
    }
}
