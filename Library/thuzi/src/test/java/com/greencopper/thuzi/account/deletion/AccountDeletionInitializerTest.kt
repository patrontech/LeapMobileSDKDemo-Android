package com.greencopper.thuzi.account.deletion

import com.greencopper.core.localstorage.*
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.testmocks.*
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.thuzi.account.deletion.initializer.AccountDeletionData
import com.greencopper.thuzi.account.deletion.initializer.AccountDeletionInitializer
import com.greencopper.thuzi.account.deletion.initializer.AlreadyLoggedOutException
import com.greencopper.thuzi.account.deletion.ui.AccountDeletionFragment
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.thuzi.account.registration.ThuziRegisteredCondition
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.ZonedDateTime

internal class AccountDeletionInitializerTest {
    private val localStorage: LocalStorage

    init {
        Toolkit.setupTest()
        localStorage = App.resolve()
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())
    }


    private val initializer = AccountDeletionInitializer(
        ThuziRegisteredCondition(localStorage),
        localStorage,
    )

    private val accountDeletionData =
        AccountDeletionData(apiUrl = "apiUrl", analytics = ScreenNameAnalytics("accountDeletion"))

    @Test
    fun whenRegisteredAndEmailExists_withCorrectParams_shouldGetLayout() {
        // Registered
        localStorage.project.thuzi.jwt.value = "jwt"
        localStorage.project.thuzi.jwtExpirationDate.value = ZonedDateTime.now().plusDays(10).toString()
        localStorage.project.thuzi.registered.value = true

        // Email Exists
        localStorage.project.user.putEmail(Email.THUZI, "email")

        mockBundleConstructor()
        val layout = initializer.getLayout(accountDeletionData.encodeToJsonElement())
        assertThat(layout).isInstanceOf(AccountDeletionFragment::class.java)
    }

    @Test
    fun whenRegisteredAndEmailNotExists_shouldThrow() {
        // Registered
        localStorage.project.thuzi.jwt.value = "jwt"
        localStorage.project.thuzi.jwtExpirationDate.value = ZonedDateTime.now().plusDays(10).toString()

        assertThrows<AlreadyLoggedOutException> {
            initializer.getLayout(accountDeletionData.encodeToJsonElement())
        }
    }

    @Test
    fun whenNotRegisteredAndEmailExists_shouldThrow() {
        // Email Exists
        localStorage.project.user.putEmail(Email.THUZI, "email")

        assertThrows<AlreadyLoggedOutException> {
            initializer.getLayout(accountDeletionData.encodeToJsonElement())
        }
    }

    @Test
    fun whenNotRegisteredAndEmailNotExists_shouldThrow() {
        assertThrows<AlreadyLoggedOutException> {
            initializer.getLayout(accountDeletionData.encodeToJsonElement())
        }
    }
}
