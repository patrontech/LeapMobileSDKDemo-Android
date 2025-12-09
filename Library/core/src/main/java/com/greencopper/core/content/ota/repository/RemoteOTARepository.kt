package com.greencopper.core.content.ota.repository

import com.greencopper.core.content.ota.OTAContent
import com.greencopper.core.content.ota.OTAManagerException
import com.greencopper.core.draftcontent.DraftContentManager
import com.greencopper.core.networking.CoreAPI
import com.greencopper.toolkit.extensions.mapErrorNotType
import com.greencopper.toolkit.httpclient.APIProvider
import com.greencopper.toolkit.httpclient.saveToFile
import com.greencopper.toolkit.logging.Logging
import com.greencopper.toolkit.logging.e
import com.greencopper.toolkit.logging.i
import com.greencopper.toolkit.storage.StorageManager
import retrofit2.HttpException
import java.io.File
import java.net.HttpURLConnection
import java.time.Duration

internal class RemoteOTARepository(
    private val coreAPIProvider: APIProvider<CoreAPI>,
    private val storageManager: StorageManager,
    private val draftContentManager: DraftContentManager,
    private val logger: Logging,
) : OTARepository {

    override suspend fun getContents(otaApiUrl: String, fetchTimeout: Duration?): List<OTAContent> {
        val api = coreAPIProvider.api(fetchTimeout)

        try {
            val request = draftContentManager.passcode
                ?.let { passcode -> api.getDraftOTAContent(otaApiUrl, "Token $passcode") }
                ?: api.getOTAContent(otaApiUrl)

            return request.filter { it.versionType != null }
        } catch (e: HttpException) {
            if (e.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                draftContentManager.deletePasscode()
            }

            throw e
        }
    }

    override suspend fun getArchiveFile(otaContent: OTAContent, downloadTimeout: Duration?): File {
        val url = otaContent.url ?: throw OTAManagerException.MissingUrlException(otaContent)
        val api = coreAPIProvider.api(downloadTimeout)

        try {
            val request = draftContentManager.passcode
                ?.let { passcode -> api.downloadDraftFile(url, "Token $passcode") }
                ?: api.downloadFile(url)
            val file = request.saveToFile(url, storageManager.getProjectCacheStorage(otaContent.project))
            logger.i(message = "Success download OTA Content: $otaContent")
            return file
        } catch (t: Throwable) {
            if ((t as? HttpException)?.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                draftContentManager.deletePasscode()
            }
            logger.e(message = "Failed to download OTA Content: $otaContent", throwable = t)
            throw t.mapErrorNotType<OTAManagerException> { OTAManagerException.DownloadFailedException(it) }
        }
    }
}
