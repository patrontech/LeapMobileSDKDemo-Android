package com.greencopper.thuzi.account.provider

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.testmocks.setupTest
import com.greencopper.thuzi.account.ThuziAccountProvider
import com.greencopper.thuzi.localstorage.*
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.LazyResolver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.*
import java.time.ZonedDateTime

public class ThuziAccountProviderTest {
    private lateinit var thuziAccountProvider: ThuziAccountProvider
    private val localStorage = LocalStorage("test")
    private val lazyLocalStorage = LazyResolver.adhoc(localStorage)

    @BeforeEach
    internal fun setUp() {
        Toolkit.setupTest()
        thuziAccountProvider = ThuziAccountProvider(lazyLocalStorage)
    }

    @Test
    @DisplayName("Given an empty local storage, When calling getAccount, Then it should return an empty map")
    public fun getAccountShouldReturnEmptyMap() {
        runTest {
            assertThat(thuziAccountProvider.getAccount(null).first().info).isEmpty()
        }
    }

    @Test
    @DisplayName("Given all info is in local storage, When calling getAccount, Then it should return three fields")
    public fun getAccountShouldReturnThreeFields() {
        localStorage.project.thuzi.jwtExpirationDate.value = ZonedDateTime.now().plusDays(1).toString()
        localStorage.project.thuzi.state.value = ThuziState(
            registered = true,
            attendee = Attendee(
                postalCode = "H2S2K6",
                firstName = "Solid",
                lastName = "Snake",
                email = "azerty@mail.com"
            )
        )
        runTest {
            assertThat(thuziAccountProvider.getAccount(null).first().info.size).isEqualTo(4)
        }
    }
}
