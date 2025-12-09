package com.greencopper.interfacekit.accountprovider

import com.greencopper.core.data.KiboSerializable
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

public interface AccountProvider {
    public fun getAccount(params: AccountProviderParams?): Flow<AccountInfo>

    public interface AccountProviderParams : KiboSerializable<AccountProviderParams>

    public data class AccountInfo(val info: Map<String, String>)

    @Serializable
    public data class Key(val name: String, val version: Int)
}
