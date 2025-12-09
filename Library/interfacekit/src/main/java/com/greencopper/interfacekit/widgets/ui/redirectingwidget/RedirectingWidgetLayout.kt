package com.greencopper.interfacekit.widgets.ui.redirectingwidget

import android.content.Context
import android.util.AttributeSet
import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.widgets.ui.WidgetLayout
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy

public abstract class RedirectingWidgetLayout<T : KiboSerializable<T>> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : WidgetLayout<T>(context, attrs, defStyleAttr) {

    protected val routeController: RouteController by App.lazy()

    protected fun redirectTo(route: Route, origin: Layout) {
        routeController.redirect(route, origin)
    }

    protected fun redirectToRouteLink(routeLink: String, origin: Layout) {
        routeController.redirectRouteLink(routeLink, origin)
    }
}
