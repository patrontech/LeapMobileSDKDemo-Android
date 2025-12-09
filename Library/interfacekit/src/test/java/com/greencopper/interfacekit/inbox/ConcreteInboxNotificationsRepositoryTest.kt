package com.greencopper.interfacekit.inbox

import com.greencopper.core.networking.SignatureGenerator
import com.greencopper.core.secrets.SecretService
import com.greencopper.coremocks.SignatureGeneratorMock
import com.greencopper.interfacekit.mocks.MockInterfaceKitAPI
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.ZonedDateTime

internal class ConcreteInboxNotificationsRepositoryTest : CoroutineTest() {

    init {
        Toolkit.setupTest()
    }

    private val secretService: SecretService =
        SecretService(mapOf("notificationInboxApi" to "secret"))
    private val signatureGenerator: SignatureGenerator = SignatureGeneratorMock()
    private val ikAPI = MockInterfaceKitAPI()

    private val inboxNotificationsRepository = ConcreteInboxNotificationsRepository(
        ikAPI,
        secretService,
        signatureGenerator,
        dispatcher,
    )

    override fun afterEach() {
        unmockkAll()
    }

    private val firstNotif = Notifications.Notification(
        id = "id_0",
        title = "Notif_0",
        date = ZonedDateTime.now().toString(),
        onTap = null
    )
    private val secondNotif = firstNotif.copy(
        id = "id_1",
        title = "Notif_1"
    )
    private val thirdNotif = firstNotif.copy(
        id = "id_2",
        title = "Notif_2"
    )
    private val notifications = Notifications(
        listOf(firstNotif, secondNotif, thirdNotif)
    )

    @Test
    @DisplayName("Given valid data are available, When calling fetchNotifications, Then 3 notifications should be fetched")
    fun fetchNotificationsShouldReturnValidData() {
        ikAPI.notificationsResponse = { notifications }

        runTest {
            val result = inboxNotificationsRepository.fetchInboxNotifications("https://www.google.com/")
            assertThat(result.items.count()).isEqualTo(3)
            assertThat(result.items.first()).isEqualTo(firstNotif)
        }
    }

    @Test
    @DisplayName("Given an error occurs, When calling fetchNotifications, Then it should throw")
    fun fetchNotificationsShouldThrow() {
        ikAPI.notificationsResponse = { throw IllegalStateException() }
        runTest {
            assertThrows<IllegalStateException> {
                inboxNotificationsRepository.fetchInboxNotifications("https://www.google.com/")
            }
        }
    }
}
