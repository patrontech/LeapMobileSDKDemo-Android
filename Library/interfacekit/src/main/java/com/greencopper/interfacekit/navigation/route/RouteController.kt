package com.greencopper.interfacekit.navigation.route

import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectingLayout

public interface RouteController {

    /** Open the feature specified by this [Route] **/
    public fun resolve(route: Route, origin: Layout, failWithoutUI: Boolean = false, throwOnBrowser: Boolean = false)

    /** Redirect to a feature specified by this [Route] while possibly navigating through the multiple [RedirectingLayout] **/
    public fun redirect(route: Route, origin: Layout?)

    /** Replace current feature by the one passed **/
    public fun replace(origin: Layout, featureInfo: FeatureInfo)

    /** Replace current feature by the one passed, adding the new one the backstack **/
    public fun replaceBackStackAware(origin: Layout, featureInfo: FeatureInfo)

    public fun openBottomSheet(origin: Layout?, featureInfo: FeatureInfo, backgroundColor: Int)

    public fun present3rdPartyFragment(origin: Layout, fragment: Layout, onAttach: () -> Unit = {})

    public fun resolveRouteLink(routeLink: String, origin: Layout, params: Map<String, String> = emptyMap())

    public fun redirectRouteLink(routeLink: String, origin: Layout?)

    /** Show alert dialog **/
    public fun showAlert(
        title: String? = null,
        message: String,
        positiveText: String? = null,
        negativeText: String? = null,
        onPositiveClicked: (() -> Unit)? = null,
        onNegativeClicked: (() -> Unit)? = null,
        onDismissed: (() -> Unit)? = null,
        isCancelable: Boolean = true,
    )

    /** Show alert dialog with text input, and receive the input in onPositiveClicked **/
    public fun showInputAlert(
        title: String? = null,
        message: String,
        hint: String?,
        isPassword: Boolean,
        positiveText: String? = null,
        negativeText: String? = null,
        onPositiveClicked: ((String) -> Unit)? = null,
        onNegativeClicked: (() -> Unit)? = null,
        isCancelable: Boolean = true,
    )

    /** Show alert dialog with list, and receive the item selected in onItemSelected **/
    public fun showListAlert(
        title: String? = null,
        items: Array<String>,
        onItemSelected: (String) -> Unit,
        isCancelable: Boolean = true,
    )
}
