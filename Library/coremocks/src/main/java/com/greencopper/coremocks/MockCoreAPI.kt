package com.greencopper.coremocks

import com.greencopper.core.content.ota.OTAContent
import com.greencopper.core.networking.CoreAPI
import com.greencopper.core.networking.CoreRequest
import com.greencopper.core.services.iplocation.IPLocation
import com.greencopper.toolkit.testing.unimplemented
import kotlinx.serialization.json.JsonElement
import okhttp3.ResponseBody
import retrofit2.Response

public class MockCoreAPI(
    public var getOTAContentResponse: () -> List<OTAContent> = { unimplemented() },
    public var getDraftOTAContentResponse: () -> List<OTAContent> = { unimplemented() },
    public var downloadFileResponse: () -> Response<ResponseBody> = { unimplemented() },
    public var downloadDraftFileResponse: () -> Response<ResponseBody> = { unimplemented() },
    public var sendUserStateResponse: () -> Unit = { unimplemented() },
    public var getIPLocationResponse: () -> IPLocation = { unimplemented() },
    public var registerNotificationsResponse: () -> Unit = { unimplemented() },
    public var unregisterNotificationsResponse: () -> Unit = { unimplemented() },
) : CoreAPI {

    public var downloadFileCount: Int = 0
    public var sendUserStateCount: Int = 0
    public var registerCount: Int = 0
    public var unregisterCount: Int = 0

    override suspend fun getOTAContent(url: String): List<OTAContent> = getOTAContentResponse()

    override suspend fun getDraftOTAContent(url: String, authHeader: String): List<OTAContent> =
        getDraftOTAContentResponse()

    override suspend fun downloadFile(url: String): Response<ResponseBody> {
        downloadFileCount += 1
        return downloadFileResponse()
    }

    override suspend fun downloadDraftFile(url: String, authHeader: String): Response<ResponseBody> =
        downloadDraftFileResponse()

    override suspend fun sendUserState(url: String, authHeader: String, body: Map<String, JsonElement?>) {
        sendUserStateCount += 1
        sendUserStateResponse()
    }

    override suspend fun getIPLocation(url: String): IPLocation = getIPLocationResponse()

    override suspend fun registerNotifications(url: String, authHeader: String, request: CoreRequest.Registration) {
        registerCount += 1
        registerNotificationsResponse()
    }

    override suspend fun unregisterNotifications(url: String, authHeader: String) {
        unregisterCount += 1
        unregisterNotificationsResponse()
    }
}
