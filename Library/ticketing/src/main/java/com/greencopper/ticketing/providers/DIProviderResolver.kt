package com.greencopper.ticketing.providers

import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.logging.e

internal class DIProviderResolver: ProviderResolver {
    override fun resolve(providerInfo: ProviderInfo): Provider {
        val provider = try {
            App.resolve<Provider>(tag = providerInfo.key)
        } catch (throwable: Throwable) {
            App.log.e("Ticket provider was not resolved for ${providerInfo.key}: ${throwable.message}")
            throw throwable
        }

        provider.setup(providerInfo.params)
        return provider
    }
}