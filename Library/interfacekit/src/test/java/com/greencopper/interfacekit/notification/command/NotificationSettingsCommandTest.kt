package com.greencopper.interfacekit.notification.command

import android.content.Context
import android.content.Intent
import com.greencopper.testmocks.core.MockNotificationPermissionService
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkClass
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class NotificationSettingsCommandTest {

    private val context: Context = mockk(relaxed = true)
    private val notificationPermissionService = MockNotificationPermissionService()
    private val notificationsSettingsCommand = NotificationsSettingsCommand(context, notificationPermissionService)

    init {
        Toolkit.setupTest()
        every { context.packageName } returns "com.my.package"
        every { context.startActivity(any(), any()) } returns Unit

        val mockIntent = mockkClass(Intent::class)
        every { mockIntent.addFlags(any()) } returns mockIntent

        notificationPermissionService.mockIntent = mockIntent
    }

    @Test
    fun executeCommand_shouldStartActivity() {
        notificationsSettingsCommand.execute()

        assertThat(notificationPermissionService.getSettingsIntentCalled).isTrue
    }

}
