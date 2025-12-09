package com.greencopper.core.metrics.provider

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.greencopper.core.content.manager.ContentManager
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.metrics.events.screenClass
import com.greencopper.core.metrics.events.screenName
import com.greencopper.core.metrics.labels.*
import com.greencopper.toolkit.versionprovider.BuildConfigProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

public class FirebaseProvider(
    private val contentManager: ContentManager,
    localStorage: LocalStorage,
    context: Context,
    coroutineScope: CoroutineScope,
    private val buildConfigProvider: BuildConfigProvider,
) : MappedProvider {

    public companion object {
        public const val providerId: String = "Firebase"
    }

    private val analytics: FirebaseAnalytics by lazy { Firebase.analytics }
    private var enabled: Boolean = false
    override val name: Provider = Provider.firebase

    init {
        // Screens
        EventParameter.screenClass[name] = FirebaseAnalytics.Param.SCREEN_CLASS
        EventParameter.screenName[name] = FirebaseAnalytics.Param.SCREEN_NAME

        // Item informations
        EventParameter.itemId[name] = FirebaseAnalytics.Param.ITEM_ID
        EventParameter.itemName[name] = FirebaseAnalytics.Param.ITEM_NAME
        EventParameter.itemCategory[name] = FirebaseAnalytics.Param.ITEM_CATEGORY

        val crashlytics = FirebaseCrashlytics.getInstance()

        coroutineScope.launch {
            localStorage.app.installationId.state.collectLatest {
                crashlytics.setCustomKey("installation_id", it)
            }
        }

        coroutineScope.launch {
            contentManager.currentContentFlow.collectLatest {
                crashlytics
                    .setCustomKey("current_content_version", it?.version?.toString() ?: "unavailable")
            }
        }

        crashlytics.setCustomKey(
            "version",
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unavailable"
        )

        crashlytics.setCustomKey("library_version", buildConfigProvider.libraryVersion)
    }

    override fun track(event: EventName, parameters: Map<EventParameter, String>) {
        if (!enabled) return

        val eventName = event[name]?.sanitize() ?: return
        val bundle = Bundle()
        contentManager.currentContent?.project?.let { project ->
            bundle.putString(FirebaseAnalytics.Param.GROUP_ID, project)
        }
        parameters.forEach {
            if (it.value.isNotBlank()) {
                bundle.putString(it.key[this.name]?.sanitize(), it.value.take(100))
            }
        }
        analytics.logEvent(eventName, bundle)
    }

    private fun String.sanitize(): String = this.take(40).replace(Regex("[^a-zA-Z0-9_]"), "_")

    private fun setAnalyticsCollectionEnabled(enabled: Boolean) {
        this.enabled = enabled
        analytics.setAnalyticsCollectionEnabled(enabled)
    }

    override fun track(parameters: Map<UserProperty, String>) {
        if (!enabled) return

        parameters.forEach {
            val key = it.key[this.name]
            key?.let { safeKey ->
                analytics.setUserProperty(safeKey, it.value)
            }
        }
    }

    override fun enable() {
        setAnalyticsCollectionEnabled(true)
    }

    override fun disable() {
        setAnalyticsCollectionEnabled(false)
    }
}

internal val Provider.Companion.firebase: Provider by lazy { Provider("firebase") }
