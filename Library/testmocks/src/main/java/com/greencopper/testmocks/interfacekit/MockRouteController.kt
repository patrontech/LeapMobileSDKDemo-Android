package com.greencopper.testmocks.interfacekit

import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.interfacekit.navigation.route.RouteController

public class MockRouteController() : RouteController {

    public enum class AlertButton {
        POSITIVE,
        NEGATIVE,
        DISMISS,
        NONE
    }

    private var willClickOnAlertButton = AlertButton.NONE

    public var lastResolveRoute: Route? = null
    public var lastResolveOrigin: Layout? = null
    override fun resolve(route: Route, origin: Layout, failWithoutUI: Boolean, throwOnBrowser: Boolean) {
        lastResolveRoute = route
        lastResolveOrigin = origin
    }

    public var lastRedirectRoute: Route? = null
    public var lastRedirectOrigin: Layout? = null
    override fun redirect(route: Route, origin: Layout?) {
        lastRedirectRoute = route
        lastRedirectOrigin = origin
    }

    public var lastReplaceFeatureInfo: FeatureInfo? = null
    public var lastReplaceOrigin: Layout? = null
    override fun replace(origin: Layout, featureInfo: FeatureInfo) {
        lastReplaceFeatureInfo = featureInfo
        lastReplaceOrigin = origin
    }

    public var lastReplaceBackStackAwareOrigin: Layout? = null
    public var lastReplaceBackStackAwareFeatureInfo: FeatureInfo? = null
    override fun replaceBackStackAware(origin: Layout, featureInfo: FeatureInfo) {
        lastReplaceBackStackAwareOrigin = origin
        lastReplaceBackStackAwareFeatureInfo = featureInfo

    }

    public var lastOpenBottomSheetOrigin: Layout? = null
    public var lastOpenBottomSheetFeatureInfo: FeatureInfo? = null
    override fun openBottomSheet(origin: Layout?, featureInfo: FeatureInfo, backgroundColor: Int) {
        lastOpenBottomSheetOrigin = origin
        lastOpenBottomSheetFeatureInfo = featureInfo
    }

    public var lastResolveRouteLink: String? = null
    public var lastResolveRouteLinkOrigin: Layout? = null
    public var lastResolveRouteLinkParams: Map<String, String>? = null
    override fun resolveRouteLink(routeLink: String, origin: Layout, params: Map<String, String>) {
        lastResolveRouteLink = routeLink
        lastResolveRouteLinkOrigin = origin
        lastResolveRouteLinkParams = params
    }

    public var lastRedirectRouteLink: String? = null
    public var lastRedirectRouteLinkOrigin: Layout? = null
    override fun redirectRouteLink(routeLink: String, origin: Layout?) {
        lastRedirectRouteLink = routeLink
        lastRedirectRouteLinkOrigin = origin
    }

    public var present3rdPartyFragmentCalled: Boolean = false
    override fun present3rdPartyFragment(origin: Layout, fragment: Layout, onAttach: () -> Unit) {
        present3rdPartyFragmentCalled = true
    }

    public var showAlertCalled: Boolean = false
    override fun showAlert(
        title: String?,
        message: String,
        positiveText: String?,
        negativeText: String?,
        onPositiveClicked: (() -> Unit)?,
        onNegativeClicked: (() -> Unit)?,
        onDismissed: (() -> Unit)?,
        isCancelable: Boolean
    ) {
        showAlertCalled = true
        when (willClickOnAlertButton) {
            AlertButton.POSITIVE -> onPositiveClicked?.invoke()
            AlertButton.NEGATIVE -> onNegativeClicked?.invoke()
            AlertButton.DISMISS -> onDismissed?.invoke()
            AlertButton.NONE -> Unit
        }
    }

    public var showInputAlertCalled: Boolean = false
    override fun showInputAlert(
        title: String?,
        message: String,
        hint: String?,
        isPassword: Boolean,
        positiveText: String?,
        negativeText: String?,
        onPositiveClicked: ((String) -> Unit)?,
        onNegativeClicked: (() -> Unit)?,
        isCancelable: Boolean
    ) {
        showInputAlertCalled = true
        when (willClickOnAlertButton) {
            AlertButton.POSITIVE -> onPositiveClicked?.invoke("")
            AlertButton.NEGATIVE -> onNegativeClicked?.invoke()
            AlertButton.DISMISS, AlertButton.NONE -> Unit
        }
    }

    public fun willSimulateClickAlert(alertButton: AlertButton) {
        willClickOnAlertButton = alertButton
    }

    public var showListAlertCalled: Boolean = false
    public var itemToSelect: String? = null
    override fun showListAlert(
        title: String?,
        items: Array<String>,
        onItemSelected: (String) -> Unit,
        isCancelable: Boolean
    ) {
        showListAlertCalled = true
        onItemSelected(itemToSelect.orEmpty())
    }
}
