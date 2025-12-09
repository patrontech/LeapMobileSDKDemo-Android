package com.greencopper.interfacekit.inbox

import com.greencopper.core.networking.SignatureGenerator
import com.greencopper.core.secrets.SecretService
import com.greencopper.core.secrets.notificationInboxApi
import com.greencopper.interfacekit.network.InterfaceKitAPI
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

public interface InboxNotificationsRepository {
    public suspend fun fetchInboxNotifications(url: String): Notifications
}

internal class ConcreteInboxNotificationsRepository(
    private val ikAPI: InterfaceKitAPI,
    private val secretService: SecretService,
    private val signatureGenerator: SignatureGenerator,
    private val coroutineContext: CoroutineContext,
) : InboxNotificationsRepository {

    override suspend fun fetchInboxNotifications(url: String): Notifications = withContext(coroutineContext) {
        ikAPI.getInboxNotifications(
            url,
            signatureGenerator.getAuthenticationKey(apiKey = secretService.notificationInboxApi),
        )
    }
}
