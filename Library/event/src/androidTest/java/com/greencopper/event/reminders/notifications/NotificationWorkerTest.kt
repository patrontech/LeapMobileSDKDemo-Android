package com.greencopper.event.reminders.notifications

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.testing.TestWorkerBuilder
import com.greencopper.core.notification.notificationmanager.NotificationManagerClient
import com.greencopper.coremocks.MockNotificationManagerClient
import com.greencopper.event.recipe.EventConfiguration
import com.greencopper.event.recipe.EventConfigurationHolder
import com.greencopper.interfacekit.links.resolver.LinkResolver
import com.greencopper.interfacekit.ui.activity.KibaMainActivity
import com.greencopper.testmocks.bindProvider
import com.greencopper.testmocks.interfacekit.MockLinkResolver
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.toolkit.MockBuildConfigProvider
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.versionprovider.BuildConfigProvider
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors

internal class NotificationWorkerTest {

    private val context = InstrumentationRegistry.getInstrumentation().context
    private val executor = Executors.newSingleThreadExecutor()
    private val configHolder = EventConfigurationHolder()
    private val config = EventConfiguration(
        EventConfiguration.Reminders(
            topBarIcon = "",
            timeIntervals = listOf(),
            defaultTimeInterval = 1,
            onFirstAddToMyScheduleRouteLink = "deeplink://destination",
            onNotificationTapRouteLink = "deeplink://destination",
        )
    )

    @BeforeEach
    internal fun setUp() {
        Toolkit.setupTest()
        configHolder.currentConfiguration.value = config
        bindProvider<BuildConfigProvider>(MockBuildConfigProvider())
        bindProvider<LinkResolver>(MockLinkResolver())
        bindProvider(configHolder)
        bindProvider<NotificationManagerClient>(
            MockNotificationManagerClient(
                notifyAction = { _, _ -> }
            )
        )

        mockkStatic(PendingIntent::class)
        every { PendingIntent.getActivity(any(), any(), any(), any()) } returns mockk(relaxed = true)
    }

    @AfterEach
    internal fun tearDown() {
        unmockkAll()
    }

    @Test
    fun doWork_returnsSuccess() {
        val scheduleItemId = 10L
        val data = Data.Builder()
            .putLong(NotificationWorker.SCHEDULE_ITEM_ID_KEY, scheduleItemId)
            .build()
        val worker = TestWorkerBuilder<NotificationWorker>(context, executor, data).build()
        val spykWorker = spyk(worker, recordPrivateCalls = true)

        val result = spykWorker.doWork()

        verify(atLeast = 1) { spykWorker["buildUri"]("deeplink://destination", scheduleItemId) }
        assertThat(result).isInstanceOf(ListenableWorker.Result.Success::class.java)
    }

    @Test
    @DisplayName("Given an uri, When calling buildOnTap, Then intent should contain this uri")
    fun buildOnTapShouldReturnValidIntent() {
        val uri = Uri.parse("deeplink://destination")
        val data = Data.Builder()
            .putLong(NotificationWorker.SCHEDULE_ITEM_ID_KEY, 1)
            .build()
        val worker = TestWorkerBuilder<NotificationWorker>(context, executor, data).build()
        val intent = Intent()
        worker.buildOnTap(intent, uri)

        assertThat(intent.extras?.getString("onTap")).isEqualTo("deeplink://destination")
    }

    @Test
    @DisplayName("Given a null uri, When calling buildOnTap, Then intent should have nothing in onTap key")
    fun buildOnTapShouldReturnNull() {
        val uri = null
        val data = Data.Builder()
            .putLong(NotificationWorker.SCHEDULE_ITEM_ID_KEY, 1)
            .build()
        val worker = TestWorkerBuilder<NotificationWorker>(context, executor, data).build()
        val intent = Intent()
        worker.buildOnTap(intent, uri)

        assertThat(intent.extras?.getString(KibaMainActivity.INTENT_KEY_ON_TAP)).isNull()
    }
}
