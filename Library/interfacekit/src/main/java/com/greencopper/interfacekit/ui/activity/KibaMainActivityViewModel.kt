package com.greencopper.interfacekit.ui.activity

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.greencopper.core.content.manager.ContentManager
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.interfacekit.links.resolver.LinkResolver
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.navigation.route.getRoute
import com.greencopper.interfacekit.rootview.RootLayoutHolder
import com.greencopper.interfacekit.session.SessionManager
import com.greencopper.interfacekit.ui.activity.KibaMainActivity.Companion.INTENT_KEY_ON_TAP
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.flow.*

public class KibaMainActivityViewModel : ViewModel() {

    internal val sessionManager: SessionManager by App.lazy()
    internal val aggregateMetricsService: AggregateMetricsService by App.lazy()
    internal val linkResolver: LinkResolver by App.lazy()
    internal val rootLayoutHolder: RootLayoutHolder by App.lazy()
    internal val routeController: RouteController by App.lazy()

    internal val uiRefreshCount: MutableStateFlow<Int> = MutableStateFlow<Int>(0)
    internal val uiRefreshVersion: MutableSharedFlow<Int> = MutableSharedFlow<Int>(replay = 1)

    internal lateinit var deeplinkScheme: String
    private val contentManager = App.resolve<ContentManager>()

    /**
     * This is a combination of waiting for the content to be done applying
     * and the rootLayout refresh corresponding to the latest content version to be set.
     */
    private val flowReadyForRedirect = combine(
        contentManager.isApplyingContent,
        contentManager.currentContentFlow.filterNotNull(),
        uiRefreshVersion
    ) { isApplying, currentContent, uiRefreshContentVersion ->
        !isApplying && currentContent.version == uiRefreshContentVersion
    }.filter { it }

    internal fun refreshUiVersion() {
        val version = contentManager.currentContent?.version
        uiRefreshVersion.tryEmit(version ?: 0)
    }

    internal suspend fun redirect(intent: Intent): Boolean {
        sessionManager.resume()
        flowReadyForRedirect.first()

        (redirectWithRouteLink(intent)
            ?: redirectWithBundleExtras(intent)
            ?: redirectWithUri(intent.data)
                )?.let {
                routeController.redirect(it, null)
                return true
            }

        return false
    }

    private fun redirectWithBundleExtras(intent: Intent): Route? {
        return intent.extras?.getRoute(INTENT_KEY_ON_TAP)
    }

    private fun redirectWithUri(uri: Uri?): Route? {
        return uri?.let {
            val localUri = it.buildUpon().scheme(deeplinkScheme)
            linkResolver.route(localUri.toString())
        }
    }

    private fun redirectWithRouteLink(intent: Intent): Route? {
        return intent.extras?.getString(INTENT_KEY_ON_TAP)?.let { fallbackRouteLink ->
            linkResolver.route(fallbackRouteLink)
        }
    }
}
