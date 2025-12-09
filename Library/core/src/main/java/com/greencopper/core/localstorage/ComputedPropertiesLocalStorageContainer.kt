package com.greencopper.core.localstorage

import android.Manifest.permission
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.greencopper.core.content.initialcontent.RunConfiguration
import com.greencopper.core.content.manager.Content
import com.greencopper.core.content.manager.ContentManager
import com.greencopper.core.content.ota.OTAContent
import com.greencopper.core.notification.repository.NotificationRepository
import com.greencopper.toolkit.di.resolver.LazyResolver
import com.greencopper.toolkit.versionprovider.BuildConfigProvider
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

public class ComputedPropertiesLocalStorageContainer(
    private val container: LocalStorageContainer,
    private val context: Context,
    private val initialContentConfig: RunConfiguration.Content,
    private val lazyContentManager: LazyResolver<ContentManager>,
    private val lazyNotificationRepository: LazyResolver<NotificationRepository>,
    private val buildConfigProvider: BuildConfigProvider,
    private val json: Json,
) : LocalStorageContainer {
    private companion object {
        const val INAPPLICABLE = "inapplicable"
        const val UNAVAILABLE = "unavailable"
        const val UPTODATE = "up-to-date"
        const val NONE = "none"
    }

    private val computations: Map<LocalStorageKey, () -> String> = mapOf(
        "%/version" to { versionResult },
        "%/platform" to { "Android" },
        "%/device" to { deviceResult },
        "%/locationPermission" to { locationPermissionResult },
        "%/notificationPermission" to { INAPPLICABLE },
        "%/registeredForPush" to { registeredForPushResult },
        "%/attPermission" to { INAPPLICABLE },
        "%/currentContent" to { currentContentResult },
        "%/currentContentVersion" to { currentContentVersionResult },
        "%/contentToApply" to { contentToApplyResult },
        "%/initialContentConfig" to { initialContentConfig.shortDescription },
        "%/currentContentVersionWithType" to { currentContentVersionWithTypeResult },
    ).mapKeys { LocalStorageKey(it.key) }

    override fun getJSON(key: LocalStorageKey): String? =
        if (key.rootType == LocalStorageKey.RootType.COMPUTED) {
            computations[key]?.let { compute ->
                json.encodeToString(compute())
            } ?: throw IllegalArgumentException("Unknown computed key $key.")
        } else {
            container.getJSON(key)
        }

    override fun setJSON(key: LocalStorageKey, json: String) {
        if (key.rootType == LocalStorageKey.RootType.COMPUTED) {
            throw IllegalArgumentException("Computed values are immutable.")
        }
        container.setJSON(key, json)
    }

    override fun keyExists(key: LocalStorageKey): Boolean =
        computations.containsKey(key) ||
                (key.rootType != LocalStorageKey.RootType.COMPUTED && container.keyExists(key))

    private val versionResult: String
        get() {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return "${packageInfo.versionName}"
        }

    private val deviceResult: String
        get() {
            return "${buildConfigProvider.manufacturer} ${buildConfigProvider.model} (v${buildConfigProvider.versionName} api ${buildConfigProvider.sdkInt})"
        }

    private val currentContentResult: String
        get() {
            val contentManager = lazyContentManager.resolve()
            return contentManager.currentContent?.shortDescription ?: UNAVAILABLE
        }

    private val currentContentVersionResult: String
        get() {
            val contentManager = lazyContentManager.resolve()
            return contentManager.currentContent?.version?.toString() ?: UNAVAILABLE
        }

    private val contentToApplyResult: String
        get() {
            val contentManager = lazyContentManager.resolve()
            return contentManager.contentToApply?.shortDescription ?: UPTODATE
        }

    private val registeredForPushResult: String
        get() {
            val notificationRepository = lazyNotificationRepository.resolve()
            return "${notificationRepository.isRegistered()}"
        }

    private val locationPermissionResult: String
        get() {
            val permissions = listOf(
                permission.ACCESS_FINE_LOCATION,
                permission.ACCESS_COARSE_LOCATION,
                permission.ACCESS_BACKGROUND_LOCATION
            )
            val result = permissions
                .filter {
                    ActivityCompat.checkSelfPermission(context, it) ==
                            PackageManager.PERMISSION_GRANTED
                }.joinToString(",") {
                    it.split('.').last()
                }
            return result.ifEmpty {
                NONE
            }
        }

    private val currentContentVersionWithTypeResult: String
        get() {
            return lazyContentManager.resolve().currentContent?.let { currentContent ->
                val contentType = if (currentContent.type == OTAContent.Type.Draft) " (draft)" else ""
                "${currentContent.version}$contentType"
            } ?: UNAVAILABLE
        }
}

private val RunConfiguration.Content.shortDescription
    get() = "s${schema}v${version}_$project"

private val Content.shortDescription
    get() = "s${schema}v${version}_$project"
