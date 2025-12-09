package com.greencopper.core.draftcontent

import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.localstorage.core
import com.greencopper.core.networking.CoreAPI
import com.greencopper.core.recipe.CoreConfigurationHolder
import com.greencopper.core.remotestate.RemoteStateDispatcher
import com.greencopper.core.remotestate.RemoteStateEntry
import com.greencopper.toolkit.di.resolver.LazyResolver
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import retrofit2.HttpException

public interface DraftContentManager {
    public val passcode: String?
    public val passcodeFlow: Flow<String?>

    public suspend fun setPasscode(passcode: String)
    public fun deletePasscode()
}

internal class ConcreteDraftContentManager(
    private val lazyLocalStorage: LazyResolver<LocalStorage>,
    private val coreConfigHolder: CoreConfigurationHolder,
    private val coreAPI: CoreAPI,
    private val lazyRemoteStateDispatcher: LazyResolver<RemoteStateDispatcher>,
    private val json: Json,
) : DraftContentManager {

    private val localStorage: LocalStorage
        get() = lazyLocalStorage.resolve()

    private val remoteStateDispatcher: RemoteStateDispatcher
        get() = lazyRemoteStateDispatcher.resolve()

    override val passcode: String?
        get() = localStorage.app.core.draftContentPasscode.value

    override val passcodeFlow: Flow<String?>
        get() = localStorage.app.core.draftContentPasscode.state

    @Throws(HttpException::class)
    override suspend fun setPasscode(passcode: String) {
        val apiUrl = coreConfigHolder.currentConfiguration.value?.ota?.apiUrl ?: return

        coreAPI.getDraftOTAContent(apiUrl, "Token $passcode")

        // if getDraftOTAContent fails it will throw, and passcode will not be saved
        localStorage.app.core.draftContentPasscode.value = passcode
        remoteStateDispatcher.dispatch(DraftContentEnabledRemoteStateEntry(true, json))
    }

    override fun deletePasscode() {
        localStorage.app.core.draftContentPasscode.value = null
        remoteStateDispatcher.dispatch(DraftContentEnabledRemoteStateEntry(false, json))
    }
}

internal class DraftContentEnabledRemoteStateEntry(enabled: Boolean, json: Json) :
    RemoteStateEntry(
        key = "draft_enabled",
        value = json.encodeToJsonElement(enabled),
        isUrgent = false,
    )
