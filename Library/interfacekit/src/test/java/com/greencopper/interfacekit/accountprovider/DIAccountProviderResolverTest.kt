package com.greencopper.interfacekit.accountprovider

import com.greencopper.testmocks.bindAccountProvider
import com.greencopper.testmocks.interfacekit.MockAccountProvider
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.toolkit.MockLogging
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.ResolveException
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

public class DIAccountProviderResolverTest {
    private lateinit var accountProviderResolver: DIAccountProviderResolver
    private val mockLogger = MockLogging()

    @BeforeEach
    internal fun setUp() {
        Toolkit.setupTest()
        accountProviderResolver = DIAccountProviderResolver(App, mockLogger)
    }

    @Test
    @DisplayName("Given a valid account provider registered, When calling resolve, Then the account provider should be returned")
    public fun resolveShouldReturnAccountProvider() {
        val mockAccountProvider =
            MockAccountProvider(AccountProvider.Key("Thuzi", 1), mutableMapOf("email" to "azerty@mail.com"))
        bindAccountProvider(mockAccountProvider.key) { mockAccountProvider }
        val accountProvider = accountProviderResolver.resolve(mockAccountProvider.key)
        assertThat(accountProvider).isEqualTo(mockAccountProvider)
    }

    @Test
    @DisplayName("Given no counter is registered, When calling resolve, Then null should be returned")
    public fun resolveShouldReturnNull() {
        val accountProvider = accountProviderResolver.resolve(AccountProvider.Key("Null", -1))
        assertThat(accountProvider).isNull()
        assertThat(mockLogger.lastThrowable).isInstanceOf(ResolveException::class.java)
    }
}
