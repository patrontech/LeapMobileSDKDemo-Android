package com.greencopper.testmocks.interfacekit

import com.greencopper.interfacekit.accountprovider.AccountProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

public class MockAccountProvider(public val key: AccountProvider.Key, private val accountInfo: MutableMap<String, String>) :
    AccountProvider {

    public var getAccountCallCount: Int = 0

    override fun getAccount(params: AccountProvider.AccountProviderParams?): Flow<AccountProvider.AccountInfo> = flowOf(
        AccountProvider.AccountInfo(accountInfo).also {
            getAccountCallCount++
        }
    )
}
