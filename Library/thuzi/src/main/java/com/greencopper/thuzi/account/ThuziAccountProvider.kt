package com.greencopper.thuzi.account

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.interfacekit.accountprovider.AccountProvider
import com.greencopper.thuzi.localstorage.thuzi
import com.greencopper.toolkit.di.resolver.LazyResolver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class ThuziAccountProvider(
    private val lazyLocalStorage: LazyResolver<LocalStorage>,
) : AccountProvider {

    override fun getAccount(params: AccountProvider.AccountProviderParams?): Flow<AccountProvider.AccountInfo> {
        val localStorage = lazyLocalStorage.resolve()
        return localStorage.project.thuzi.state.state.map { state ->
            val result = mutableMapOf<String, String>()
            state.attendee.lastName?.let {
                result.put("lastName", it)
            }
            state.attendee.firstName?.let {
                result["firstName"] = it
                result.put("name", "$it ${result["lastName"]}")
            }
            state.attendee.email?.let {
                result.put("email", it)
            }
            AccountProvider.AccountInfo(result)
        }
    }

    companion object {
        val key = AccountProvider.Key("Thuzi.AccountProvider", 1)
    }
}