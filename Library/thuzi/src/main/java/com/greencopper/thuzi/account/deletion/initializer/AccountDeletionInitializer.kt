package com.greencopper.thuzi.account.deletion.initializer

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localstorage.Email
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.user
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.navigation.feature.ParameterizedFeatureInitializer
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.thuzi.account.deletion.ui.AccountDeletionFragment
import com.greencopper.thuzi.account.registration.ThuziRegisteredCondition
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class AccountDeletionInitializer(
    private val registeredCondition: ThuziRegisteredCondition,
    private val localStorage: LocalStorage,
) : ParameterizedFeatureInitializer<AccountDeletionData>() {

    companion object {
        val key = FeatureKey("Thuzi.AccountDeletion", 1)
    }

    override val featureKey: FeatureKey = key

    override fun decodeParams(params: FeatureParams): AccountDeletionData = KiboSerializable.decodeFromJsonElement(params)

    override fun layoutForParams(params: AccountDeletionData): Layout {
        val registered = registeredCondition.checkWith(ThuziRegisteredCondition.ThuziRegisteredConditionData(true))
        val emailExist = localStorage.project.user.email.value[Email.THUZI.key] != null
        if (!registered || !emailExist) {
            throw AlreadyLoggedOutException()
        }
        return AccountDeletionFragment(AccountDeletionLayoutData(
            analytics = params.analytics,
            apiUrl = params.apiUrl,
            redirectionHash = redirectionHashForParams(params),
        ))
    }

    override fun redirectionHashForParams(params: AccountDeletionData): RedirectionHash = RedirectionHash(key, params.apiUrl)
}

@Serializable
internal data class AccountDeletionData(
    val apiUrl: String,
    val analytics: ScreenNameAnalytics,
) : KiboSerializable<AccountDeletionData> {

    override fun getSerializer(): KSerializer<AccountDeletionData> = serializer()
}

@Serializable
internal data class AccountDeletionLayoutData(
    val apiUrl: String,
    val analytics: ScreenNameAnalytics,
    val redirectionHash: RedirectionHash,
) : KiboSerializable<AccountDeletionLayoutData> {
    override fun getSerializer(): KSerializer<AccountDeletionLayoutData> = serializer()
}

internal class AlreadyLoggedOutException : Exception("User is already logged out or doesn't have email")
