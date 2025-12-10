package com.example.kibasdkpoc

import android.content.Context
import android.net.Uri
import androidx.fragment.app.FragmentManager
import com.greencopper.core.CoreAssembly
import com.greencopper.core.content.initialcontent.ContentInitializer
import com.greencopper.core.metrics.provider.MappedProvider
import com.greencopper.core.services.RecipeRegisterService
import com.greencopper.event.EventAssembly
import com.greencopper.interfacekit.InterfaceKitAssembly
import com.greencopper.interfacekit.links.resolver.LinkResolver
import com.greencopper.interfacekit.navigation.feature.FeatureResolver
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.interfacekit.rootview.RootLayoutManager
import com.greencopper.interfacekit.session.SessionManager
import com.greencopper.maps.MapsAssembly
import com.greencopper.thuzi.ThuziAssembly
import com.greencopper.ticketing.TicketingAssembly
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.bindSingleton
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.logging.multilogging.LoggingConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

public object LeapMobileSDK {

    private var isInitialized = false

    public fun initialize(
        context: Context,
        logging: LoggingConfiguration? = null,
        metrics: MappedProvider? = null,
    ) {
        if (isInitialized) return

        val assemblies = mutableListOf(
            CoreAssembly(),
            EventAssembly(),
            InterfaceKitAssembly(),
            MapsAssembly(),
            ThuziAssembly(),
            TicketingAssembly(),
        )

        metrics?.let { assemblies.add(SDKAssembly(it)) }

        Toolkit.setup(
            assemblies = assemblies,
            loggingConfigurations = logging?.let { listOf(it) } ?: emptyList(),
            applicationContext = context.applicationContext,
        )

        App.resolve<RecipeRegisterService>().register()

        CoroutineScope(Dispatchers.IO).launch {
            App.resolve<SessionManager>().resume()
        }

        isInitialized = true
    }

    public fun getRootLayout(fragmentManager: FragmentManager): Flow<Layout> =
        App.resolve<RootLayoutManager>()
            .setupRootLayout(fragmentManager, false)

    public fun resolveDeeplink(uri: Uri): Layout? {
        val route = App.resolve<LinkResolver>().route(uri.toString()) ?: return null
        val featureInfo = (route as? Route.Push)?.feature
            ?: (route as? Route.Present)?.feature
            ?: return null

        return App.resolve<FeatureResolver>().resolve(featureInfo)
    }
}

private class SDKAssembly(private val metrics: MappedProvider) : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.bindSingleton { metrics }
    }
}
