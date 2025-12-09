package com.greencopper.ticketing.providers

public interface ProviderResolver {
    public fun resolve(providerInfo: ProviderInfo): Provider
}
