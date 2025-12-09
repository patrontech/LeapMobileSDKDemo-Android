package com.greencopper.interfacekit.navigation.route

import android.content.*
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.net.MailTo
import androidx.core.net.toUri
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.interfacekit.commands.system.CommandExecutor
import com.greencopper.interfacekit.links.resolver.LinkResolver
import com.greencopper.interfacekit.navigation.NavigationController
import com.greencopper.interfacekit.navigation.feature.*
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.layout.*
import com.greencopper.interfacekit.rootview.RootLayoutHolder
import com.greencopper.interfacekit.ui.dpToPx
import com.greencopper.interfacekit.ui.fragment.BottomSheetChild
import com.greencopper.interfacekit.ui.shouldColorNavigationBar
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.*
import com.greencopper.toolkit.di.resolver.LazyResolver
import com.greencopper.toolkit.logging.Logging
import com.greencopper.toolkit.logging.e
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class ConcreteRouteController(
    private val featureResolver: FeatureResolver,
    private val linkResolver: LinkResolver,
    private val commandExecutor: CommandExecutor,
    private val context: Context,
    private val rootLayout: StateFlow<Layout?>,
    private val localizationService: LocalizationService,
    private val lazyLocalStorage: LazyResolver<LocalStorage>,
    private val mainThreadScope: CoroutineScope,
    private val logger: Logging,
) : RouteController {

    private fun getFirstRedirectingLayout(): RedirectingLayout? = try {
        rootLayout.value?.childFragmentManager?.let {
            findRedirectingLayoutInFragmentsStack(it)
        }
    } catch (t: Throwable) {
        logger.e("Could not get root layout feature", throwable = t)
        null
    }

    private fun findRedirectingLayoutInFragmentsStack(fragmentManager: FragmentManager): RedirectingLayout? {
        return try {
            // Find the latest NavigationController in this stack
            val fragment = fragmentManager.fragments.last { fragment ->
                fragment is NavigationController<*>
            }
            // Check if this NavigationController contains another in its own child stack
            findRedirectingLayoutInFragmentsStack(fragment.childFragmentManager)
                ?: fragment as? RedirectingLayout
        } catch (_: NoSuchElementException) {
            // This exception occurs if no match where found for last{}
            // it would meant we reach the end of our journey in the stack
            null
        }
    }

    override fun resolve(
        route: Route,
        origin: Layout,
        failWithoutUI: Boolean,
        throwOnBrowser: Boolean,
    ) {
        when (route) {
            is Route.Push -> resolve(pushRoute = route, origin = origin)
            is Route.Present -> resolve(presentRoute = route, origin = origin)
            is Route.External -> resolve(externalRoute = route, failWithoutUI, throwOnBrowser)
            is Route.Execute -> execute(executeRoute = route, origin = origin)
        }
    }

    private fun resolve(
        externalRoute: Route.External,
        failWithoutUI: Boolean = false,
        throwOnBrowser: Boolean = false,
    ) {
        try {
            val url = localizationService.getString(externalRoute.url)
            val parameterizedUrl = lazyLocalStorage.resolve().replaceUrlParameters(url)
            val intent = if (parameterizedUrl.startsWith("mailto:")) {
                MailTo.parse(parameterizedUrl)
                    .let { newEmailIntent(it.to, it.subject, it.body, it.cc) }
            } else {
                Intent(Intent.ACTION_VIEW, parameterizedUrl.toUri())
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (throwOnBrowser) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER)
            }
            context.startActivity(intent)
        } catch (error: Throwable) {
            logger.e("Intent couldn't be resolved", throwable = error)
            if (!failWithoutUI) {
                showAlertFeatureUnavailable(null, error)
            }
            if (error is ActivityNotFoundException && throwOnBrowser) {
                throw error
            }
        }
    }

    private fun resolve(pushRoute: Route.Push, origin: Layout) {
        try {
            val layout = featureResolver.resolve(pushRoute.feature)
            layout.addNavigationButtonsFlags(origin.getNavigationButtonsFlags() or NavigationControlsHandler.SHOW_BACK_BUTTON_FLAG)
            layout.shouldColorNavigationBar = origin.shouldColorNavigationBar
            if (origin is NavigationController<*>) {
                origin.push(layout)
            } else {
                origin.getNavigationController()?.push(layout)
            }
        } catch (t: Throwable) {
            showAlertFeatureUnavailable(pushRoute.feature, t)
        }
    }

    private fun resolve(presentRoute: Route.Present, origin: Layout) {
        try {
            val layout = featureResolver.resolve(presentRoute.feature)
            layout.addNavigationButtonsFlags(NavigationControlsHandler.SHOW_CLOSE_BUTTON_FLAG)
            layout.shouldColorNavigationBar = true
            if (layout is BottomSheetChild) {
                origin.getNavigationController()?.bottomSheetPresent(layout, layout.backgroundColor)
            } else if (origin is NavigationController<*>) {
                origin.present(layout)
            } else {
                origin.getNavigationController()?.present(layout)
            }
        } catch (t: Throwable) {
            showAlertFeatureUnavailable(presentRoute.feature, t)
        }
    }

    override fun redirect(route: Route, origin: Layout?) {
        when (route) {
            is Route.Push -> redirectFeatured(route, route.feature, origin)
            is Route.Present -> redirectFeatured(route, route.feature, origin)
            is Route.External -> resolve(route)
            is Route.Execute -> execute(route, origin)
        }
    }

    private fun execute(executeRoute: Route.Execute, origin: Layout?) {
        try {
            commandExecutor.executeAsync(executeRoute.command, origin ?: rootLayout.value)
        } catch (throwable: Throwable) {
            logger.e("Couldn't resolve command ${executeRoute.command}")
            showAlertFeatureUnavailable(null, throwable)
        }
    }

    private fun redirectFeatured(route: Route, featureInfo: FeatureInfo, origin: Layout?) {
        try {
            val redirected = redirectIfPossible(featureInfo)
            if (redirected.not()) {
                (origin ?: rootLayout.value)?.let { resolve(route, it) }
            }
        } catch (throwable: Throwable) {
            showAlertFeatureUnavailable(featureInfo, throwable)
        }
    }

    private fun redirectIfPossible(featureInfo: FeatureInfo): Boolean {
        val redirectionHash = findRedirectionHash(featureInfo)
        getFirstRedirectingLayout()?.let { redirectingLayout ->
            if (redirectingLayout.availableRedirections.contains(redirectionHash)) {
                redirectingLayout.redirectTo(redirectionHash)
                return true
            }
        }
        return false
    }

    private fun findRedirectionHash(featureInfo: FeatureInfo): RedirectionHash =
        featureResolver.resolveInitializer(featureInfo).redirectionHashFor(featureInfo.params)

    override fun replaceBackStackAware(origin: Layout, featureInfo: FeatureInfo) {
        try {
            val layout = featureResolver.resolve(featureInfo)
            layout.addNavigationButtonsFlags(origin.getNavigationButtonsFlags())
            layout.shouldColorNavigationBar = origin.shouldColorNavigationBar
            origin.getNavigationController()?.replaceBackStackAware(origin, layout)
        } catch (t: Throwable) {
            showAlertFeatureUnavailable(featureInfo, t)
        }
    }

    override fun replace(origin: Layout, featureInfo: FeatureInfo) {
        try {
            val layout = featureResolver.resolve(featureInfo)
            layout.addNavigationButtonsFlags(origin.getNavigationButtonsFlags())
            layout.shouldColorNavigationBar = origin.shouldColorNavigationBar
            if (origin is NavigationController<*>) {
                origin.replace(layout)
            } else {
                origin.getNavigationController()?.replace(layout)
            }
        } catch (t: Throwable) {
            showAlertFeatureUnavailable(featureInfo, t)
        }
    }

    override fun openBottomSheet(origin: Layout?, featureInfo: FeatureInfo, backgroundColor: Int) {
        try {
         val layout = featureResolver.resolve(featureInfo)
         val navController =
             origin?.getNavigationController() ?: RootLayoutHolder.rootLayoutHolder.value as? NavigationController<*> ?: return
         navController.bottomSheetPresent(layout, backgroundColor)
        } catch (t: Throwable) {
         showAlertFeatureUnavailable(featureInfo, t)
        }
    }

    override fun present3rdPartyFragment(origin: Layout, fragment: Layout, onAttach: () -> Unit) {
        if (origin is NavigationController<*>) {
            origin.present(fragment, onAttach)
        } else {
            origin.getNavigationController()?.present(fragment, onAttach)
        }
    }

    override fun resolveRouteLink(routeLink: String, origin: Layout, params: Map<String, String>) {
        linkResolver.route(routeLink, params)?.let { route ->
            resolve(route, origin)
        } ?: run {
            logger.e("Couldn't resolve route link $routeLink")
        }
    }

    override fun redirectRouteLink(routeLink: String, origin: Layout?) {
        linkResolver.route(routeLink)?.let { route ->
            redirect(route, origin)
        } ?: run {
            logger.e("Couldn't redirect route link $routeLink")
        }
    }

    private fun showAlertFeatureUnavailable(featureInfo: FeatureInfo?, cause: Throwable) {
        var message = localizationService.getString("interfaceKit.unavailable_feature.message")
        when (cause) {
            is FeatureResolverException.FeatureNotRegisteredException -> {
                logger.e("Feature $featureInfo is not registered", throwable = cause)
            }
            is FeatureInitializerException.FeatureDisabled -> {
                message = localizationService.getString("interfaceKit.disabled_feature.message")
                logger.e("Feature $featureInfo has been disabled in ServiceManager", throwable = cause)
            }
            else -> logger.e("Couldn't resolve feature $featureInfo", throwable = cause)
        }

        showAlert(localizationService.getString("interfaceKit.unavailable_feature.title"), message)
    }

    private fun newEmailIntent(
        address: String?,
        subject: String?,
        body: String?,
        cc: String?,
    ): Intent =
        Intent(Intent.ACTION_SEND)
            .putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
            .putExtra(Intent.EXTRA_TEXT, body)
            .putExtra(Intent.EXTRA_SUBJECT, subject)
            .putExtra(Intent.EXTRA_CC, cc).apply {
                type = "message/rfc822"
            }

    override fun showAlert(
        title: String?,
        message: String,
        positiveText: String?,
        negativeText: String?,
        onPositiveClicked: (() -> Unit)?,
        onNegativeClicked: (() -> Unit)?,
        onDismissed: (() -> Unit)?,
        isCancelable: Boolean,
    ) {
        mainThreadScope.launch {
            // Fragment's context is needed because app context doesn't have theming.
            val fragmentContext = rootLayout.value?.context
                ?: return@launch
            val builder = MaterialAlertDialogBuilder(fragmentContext).apply {
                if (title != null) setTitle(title)
                setMessage(message)
                setPositiveButton(
                    positiveText ?: localizationService.getString("common.ok")
                ) { _, _ ->
                    onPositiveClicked?.invoke()
                }
                setOnDismissListener {
                    onDismissed?.invoke()
                }
                setCancelable(isCancelable)
            }

            negativeText?.let {
                builder.setNegativeButton(negativeText) { _, _ ->
                    onNegativeClicked?.invoke()
                }
            }

            builder.show()
        }
    }

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
        mainThreadScope.launch {
            // Fragment's context is needed because app context doesn't have theming.
            val fragmentContext = rootLayout.value?.context ?: return@launch

            val inputView = LinearLayout(fragmentContext)
            inputView.setPadding(19.dpToPx(), 0, 19.dpToPx(), 0)
            val editText = EditText(fragmentContext).apply {
                this.hint = hint
                maxLines = 1
                isSingleLine = true
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                requestFocus()
                if (isPassword) {
                    inputType = inputType or InputType.TYPE_TEXT_VARIATION_PASSWORD
                }
            }
            inputView.addView(editText)

            val builder = MaterialAlertDialogBuilder(fragmentContext).apply {
                if (title != null) setTitle(title)
                setMessage(message)
                setPositiveButton(
                    positiveText ?: localizationService.getString("common.ok")
                ) { _, _ ->
                    onPositiveClicked?.invoke(editText.text.toString())
                }
                setCancelable(isCancelable)
                setView(inputView)
            }

            negativeText?.let {
                builder.setNegativeButton(negativeText) { _, _ ->
                    onNegativeClicked?.invoke()
                }
            }

            builder.show()
        }
    }

    override fun showListAlert(
        title: String?,
        items: Array<String>,
        onItemSelected: (String) -> Unit,
        isCancelable: Boolean
    ) {
        mainThreadScope.launch {
            val fragmentContext = rootLayout.value?.context ?: return@launch
            MaterialAlertDialogBuilder(fragmentContext).apply {
                setTitle(title)
                setItems(items) { _, which ->
                    val selectedItem = items[which]
                    onItemSelected(selectedItem)
                }
                setCancelable(isCancelable)
                create()
                show()
            }
        }
    }
}
