package com.example.kibasdkpoc

import android.content.Context
import android.net.Uri
import androidx.fragment.app.FragmentManager
import com.greencopper.leapmobilesdk.core.CoreAssembly
import com.greencopper.leapmobilesdk.core.content.initialcontent.ContentInitializer
import com.greencopper.leapmobilesdk.core.metrics.provider.MappedProvider
import com.greencopper.leapmobilesdk.core.services.RecipeRegisterService
import com.greencopper.leapmobilesdk.event.EventAssembly
import com.greencopper.leapmobilesdk.interfacekit.InterfaceKitAssembly
import com.greencopper.leapmobilesdk.interfacekit.links.resolver.LinkResolver
import com.greencopper.leapmobilesdk.interfacekit.navigation.feature.FeatureResolver
import com.greencopper.leapmobilesdk.interfacekit.navigation.layout.Layout
import com.greencopper.leapmobilesdk.interfacekit.navigation.route.Route
import com.greencopper.leapmobilesdk.interfacekit.rootview.RootLayoutManager
import com.greencopper.leapmobilesdk.interfacekit.session.SessionManager
import com.greencopper.leapmobilesdk.maps.MapsAssembly
import com.greencopper.leapmobilesdk.thuzi.ThuziAssembly
import com.greencopper.leapmobilesdk.ticketing.TicketingAssembly
import com.greencopper.leapmobilesdk.toolkit.App
import com.greencopper.leapmobilesdk.toolkit.Toolkit
import com.greencopper.leapmobilesdk.toolkit.di.assembly.Assembly
import com.greencopper.leapmobilesdk.toolkit.di.binding.Registrar
import com.greencopper.leapmobilesdk.toolkit.di.binding.bindSingleton
import com.greencopper.leapmobilesdk.toolkit.di.resolver.resolve
import com.greencopper.leapmobilesdk.toolkit.logging.multilogging.LoggingConfiguration
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
