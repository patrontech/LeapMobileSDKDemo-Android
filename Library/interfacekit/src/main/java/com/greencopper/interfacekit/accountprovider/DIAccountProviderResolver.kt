package com.greencopper.interfacekit.accountprovider

import com.greencopper.toolkit.di.resolver.Resolver
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.logging.Logging
import com.greencopper.toolkit.logging.e

internal class DIAccountProviderResolver(
    private val resolver: Resolver,
    private val logger: Logging,
) : AccountProviderResolver {
    override fun resolve(key: AccountProvider.Key, params: AccountProvider.AccountProviderParams?): AccountProvider? =
        try {
            resolver.resolve(tag = key, args = arrayOf(params))
        } catch (error: Throwable) {
            logger.e("Couldn't resolve AccountProvider $key", throwable = error)
            null
        }
}
