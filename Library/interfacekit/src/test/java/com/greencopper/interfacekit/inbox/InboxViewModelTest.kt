package com.greencopper.interfacekit.inbox

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.TestLocalStorageContainer
import com.greencopper.testmocks.CoroutineTest
import com.greencopper.testmocks.core.MockConditionChecker
import com.greencopper.testmocks.core.MockLocalizationService
import com.greencopper.testmocks.core.MockTimezoneProvider
import com.greencopper.testmocks.interfacekit.MockInboxNotificationsRepository
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.ZoneId

internal class InboxViewModelTest : CoroutineTest(UnconfinedTestDispatcher()) {

    init {
        Toolkit.setupTest()
    }

    private lateinit var inboxViewModel: InboxViewModel
    private lateinit var inboxNotificationsRepository: InboxNotificationsRepository
    private lateinit var localStorage: LocalStorage
    private val timezoneProvider = MockTimezoneProvider()
    private val localizationService = MockLocalizationService()
    private val conditionChecker = MockConditionChecker()

    private val firstNotif = Notifications.Notification(
        id = "id_0",
        title = "Notif_0",
        date = "2022-10-29T18:00:00-04:00",
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

    override fun afterEach() {}

    @Test
    fun fetchNotificationsShouldSucceed() {
            inboxNotificationsRepository = MockInboxNotificationsRepository { notifications }
            localStorage = LocalStorage("project", TestLocalStorageContainer())
        inboxViewModel = InboxViewModel(
            inboxNotificationsRepository,
            localStorage,
            timezoneProvider,
            localizationService,
            conditionChecker,
            testScope
        )

            val uiStateReceived = mutableListOf<InboxViewModel.FetchNotificationsUiState>()
            val uiStateJob = testScope.launch {
                inboxViewModel.uiState.collect {
                    uiStateReceived.add(it)
                }
            }

            inboxViewModel.fetchNotifications("https//www.google.com")
            val notifs = inboxViewModel.notifications(ZoneId.systemDefault()).values.first()

            assertThat(uiStateReceived).isEqualTo(
                listOf(
                    InboxViewModel.FetchNotificationsUiState.Loading,
                    InboxViewModel.FetchNotificationsUiState.Success
                )
            )
            assertThat(notifs.size).isEqualTo(3)
            assertThat(notifs.first()).isEqualTo(firstNotif)

            uiStateJob.cancel()
    }

    @Test
    fun fetchNotificationsShouldFail() {
            inboxNotificationsRepository = MockInboxNotificationsRepository { throw IllegalStateException() }
            localStorage = LocalStorage("project", TestLocalStorageContainer())
        inboxViewModel = InboxViewModel(
            inboxNotificationsRepository,
            localStorage,
            timezoneProvider,
            localizationService,
            conditionChecker,
            testScope
        )

            val uiStateReceived = mutableListOf<InboxViewModel.FetchNotificationsUiState>()
            val uiStateJob = testScope.launch {
                inboxViewModel.uiState.collect {
                    uiStateReceived.add(it)
                }
            }

            inboxViewModel.fetchNotifications("https//www.google.com")
            val notifs = inboxViewModel.notifications(ZoneId.systemDefault()).values

            assertThat(uiStateReceived).hasSize(2)
            assertThat(uiStateReceived[0]).isEqualTo(InboxViewModel.FetchNotificationsUiState.Loading)
            assertThat(uiStateReceived[1]).isInstanceOf(InboxViewModel.FetchNotificationsUiState.Error::class.java)
            assertThat(notifs.size).isEqualTo(0)

            uiStateJob.cancel()
    }
}
