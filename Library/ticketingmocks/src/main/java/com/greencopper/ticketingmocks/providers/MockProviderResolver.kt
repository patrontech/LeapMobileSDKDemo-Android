package com.greencopper.ticketingmocks.providers

import com.greencopper.ticketing.providers.Provider
import com.greencopper.ticketing.providers.ProviderInfo
import com.greencopper.ticketing.providers.ProviderResolver

public class MockProviderResolver : ProviderResolver {

    public var providers: Map<ProviderInfo.Key, Provider> = emptyMap()

    override fun resolve(providerInfo: ProviderInfo): Provider {
        return providers[providerInfo.key] ?: throw NoSuchElementException()
    }

}
