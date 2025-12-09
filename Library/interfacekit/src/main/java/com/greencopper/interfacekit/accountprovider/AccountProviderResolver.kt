package com.greencopper.interfacekit.accountprovider

public interface AccountProviderResolver {
    public fun resolve(
        key: AccountProvider.Key,
        params: AccountProvider.AccountProviderParams? = null
    ): AccountProvider?
}
