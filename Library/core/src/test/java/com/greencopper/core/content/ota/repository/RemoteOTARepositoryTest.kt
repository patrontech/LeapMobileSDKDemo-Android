package com.greencopper.core.content.ota.repository

import com.greencopper.core.content.ota.OTAContent
import com.greencopper.core.content.ota.OTAManagerException
import com.greencopper.core.networking.CoreAPI
import com.greencopper.coremocks.MockCoreAPI
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.MockAPIProvider
import com.greencopper.testmocks.core.MockDraftContentManager
import com.greencopper.testmocks.toolkit.MockLogging
import com.greencopper.testmocks.toolkit.MockStorageManager
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.time.Duration
import kotlin.io.path.createTempDirectory

internal class RemoteOTARepositoryTest : CoroutineTest() {

    private val projectCacheStorage = createTempDirectory().toFile()
    private val storageManager = MockStorageManager(
        projectCacheStorage = { projectCacheStorage }
    )

    private val logger = MockLogging()
    private val coreAPI = MockCoreAPI()
    private val apiProvider = MockAPIProvider<CoreAPI>(coreAPI)
    private var passcodeDeleted = false
    private val draftContentManager = MockDraftContentManager(
        passcodeReturnValue = { null },
        deletePasscodeResult = { passcodeDeleted = true }
    )

    private val otaContent = OTAContent("", "test_project", null, 1, "release", 1)
    private val otaRepository = RemoteOTARepository(apiProvider, storageManager,
        draftContentManager, logger)

    override fun afterEach() {
        projectCacheStorage.deleteOnExit()
    }

    @Test
    fun getContentsShouldSucceed() {
        coreAPI.getOTAContentResponse = { listOf(otaContent) }

        runTest {
            assertThat(otaRepository.getContents(""))
                .isEqualTo(listOf(otaContent))
        }
    }

    @Test
    fun getContentsShouldFail() {
        coreAPI.getOTAContentResponse = {
            throw HttpException(Response.error<ResponseBody>(400, "".toResponseBody("text/plain".toMediaType())))
        }

        runTest {
            assertThrows<HttpException> {
                otaRepository.getContents("")
            }
        }
    }

    @Test
    fun getContentsWithTimeoutShouldSucceed() {
        coreAPI.getOTAContentResponse = { listOf(otaContent) }

        runTest {
            assertThat(
                otaRepository.getContents("", Duration.ofSeconds(10))
            ).isEqualTo(listOf(otaContent))
        }
    }

    @Test
    fun getContentsWithTimeoutShouldFail() {
        coreAPI.getOTAContentResponse = {
            throw HttpException(Response.error<ResponseBody>(400, "".toResponseBody("text/plain".toMediaType())))
        }

        runTest {
            assertThrows<HttpException> {
                otaRepository.getContents("")
            }
        }
    }

    @Test
    fun getArchiveShouldSucceed() {
        runTest {
            val file = File.createTempFile("pref", "suf")
            val response = Response.success(file.readBytes().toResponseBody("text/plain".toMediaType()))
            coreAPI.downloadFileResponse = { response }

            val result = otaRepository.getArchiveFile(otaContent)

            assertThat(result.readBytes()).isEqualTo(file.readBytes())
            file.delete()
        }
    }

    @Test
    fun getArchiveFileShouldFail() {
        coreAPI.downloadFileResponse = { throw Exception() }

        runTest {
            assertThrows<OTAManagerException.DownloadFailedException> {
                otaRepository.getArchiveFile(otaContent)
            }
        }
    }

    @Test
    fun getArchiveWithTimeoutShouldSucceed() {
        val file = File.createTempFile("file", "txt")
        val response = Response.success(file.readBytes().toResponseBody("text/plain".toMediaType()))
        coreAPI.downloadFileResponse = { response }

        runTest {
            val result = otaRepository.getArchiveFile(otaContent, Duration.ofSeconds(10))
            assertThat(result.readBytes()).isEqualTo(file.readBytes())
        }

        file.deleteOnExit()
    }

    @Test
    fun getArchiveFileWithTimeoutShouldFail() {
        coreAPI.downloadFileResponse = { throw Exception() }

        runTest {
            assertThrows<OTAManagerException.DownloadFailedException> {
                otaRepository.getArchiveFile(otaContent, Duration.ofSeconds(10))
            }
        }
    }

    @Test
    fun getContentsWithPasscodeShouldSucceed() {
        draftContentManager.passcodeReturnValue = { "passcode" }
        coreAPI.getDraftOTAContentResponse = { listOf(otaContent) }

        runTest {
            assertThat(otaRepository.getContents("")).isEqualTo(listOf(otaContent))
        }
    }

    @Test
    fun getContentsWithPasscodeShouldReturn401() {
        draftContentManager.passcodeReturnValue = { "passcode" }
        coreAPI.getDraftOTAContentResponse = {
            throw HttpException(Response.error<ResponseBody>(401, "".toResponseBody("text/plain".toMediaType())))
        }

        runTest {
            assertThrows<HttpException> {
                otaRepository.getContents("")
            }
            assertThat(passcodeDeleted).isTrue
        }
    }

    @Test
    fun getContentsWithPasscodeShouldReturnOtherError() {
        draftContentManager.passcodeReturnValue = { "passcode" }
        coreAPI.getDraftOTAContentResponse = {
            throw HttpException(Response.error<ResponseBody>(500, "".toResponseBody("text/plain".toMediaType())))
        }

        runTest {
            assertThrows<HttpException> {
                otaRepository.getContents("")
            }
            assertThat(passcodeDeleted).isFalse
        }
    }

    @Test
    fun getArchiveFileWithPasscodeShouldSucceed() {
        draftContentManager.passcodeReturnValue = { "passcode" }
        val file = File.createTempFile("pref", "suf")
        val response = Response.success(file.readBytes().toResponseBody("text/plain".toMediaType()))
        coreAPI.downloadDraftFileResponse = { response }

        runTest {
            val result = otaRepository.getArchiveFile(otaContent)
            assertThat(result.readBytes()).isEqualTo(file.readBytes())
        }

        file.delete()
    }

    @Test
    fun getArchiveFileWithPasscodeShouldReturn401() {
        draftContentManager.passcodeReturnValue = { "passcode" }
        coreAPI.downloadDraftFileResponse = {
            throw HttpException(Response.error<ResponseBody>(401, "".toResponseBody("text/plain".toMediaType())))
        }

        runTest {
            assertThrows<OTAManagerException.DownloadFailedException> {
                otaRepository.getArchiveFile(otaContent)
            }
            assertThat(passcodeDeleted).isTrue
        }
    }

    @Test
    fun getArchiveFileWithPasscodeShouldReturnOtherError() {
        draftContentManager.passcodeReturnValue = { "passcode" }
        coreAPI.downloadDraftFileResponse = {
            throw HttpException(Response.error<ResponseBody>(500, "".toResponseBody("text/plain".toMediaType())))
        }

        runTest {
            assertThrows<OTAManagerException.DownloadFailedException> {
                otaRepository.getArchiveFile(otaContent)
            }
            assertThat(passcodeDeleted).isFalse
        }
    }
}
