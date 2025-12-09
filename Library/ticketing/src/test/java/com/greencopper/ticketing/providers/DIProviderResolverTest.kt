package com.greencopper.ticketing.providers

import com.greencopper.testmocks.bindProvider
import com.greencopper.testmocks.setupTest
import com.greencopper.ticketingmocks.providers.MockProvider
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.ResolveException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class DIProviderResolverTest {

    private val providerResolver: ProviderResolver

    init {
        Toolkit.setupTest()

        providerResolver = DIProviderResolver()
    }

    @Test
    fun resolveUnknownProvider_shouldThrow() {
        assertThrows<ResolveException> {
            providerResolver.resolve(ProviderInfo(MockProvider.key, MockProvider.Data("apiUrl").encodeToJsonElement()))
        }
    }

    @Test
    fun resolveKnownProvider_shouldReturnProvider() {
        bindProvider<Provider>(tag = MockProvider.key, mock = MockProvider())

        assertDoesNotThrow {
            providerResolver.resolve(ProviderInfo(MockProvider.key, MockProvider.Data("apiUrl").encodeToJsonElement()))
        }
    }

}
