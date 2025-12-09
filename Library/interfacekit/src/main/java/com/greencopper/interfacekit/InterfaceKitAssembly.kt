package com.greencopper.interfacekit

import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import com.greencopper.interfacekit.accountprovider.AccountProvider
import com.greencopper.interfacekit.accountprovider.AccountProviderAssembly
import com.greencopper.interfacekit.appreview.AppReviewAssembly
import com.greencopper.interfacekit.color.ColorAssembly
import com.greencopper.interfacekit.commands.PresentAppStoreCommand
import com.greencopper.interfacekit.commands.system.CommandAssembly
import com.greencopper.interfacekit.commands.system.bindCommand
import com.greencopper.interfacekit.counter.Counter
import com.greencopper.interfacekit.counter.CounterAssembly
import com.greencopper.interfacekit.draftcontent.DraftContentAssembly
import com.greencopper.interfacekit.editorial.EditorialPageAssembly
import com.greencopper.interfacekit.filtering.FilteringAssembly
import com.greencopper.interfacekit.fullscreenmedia.FullScreenMediaAssembly
import com.greencopper.interfacekit.imageservice.ImageServiceAssembly
import com.greencopper.interfacekit.inbox.InboxAssembly
import com.greencopper.interfacekit.interests.InterestsAssembly
import com.greencopper.interfacekit.links.LinksAssembly
import com.greencopper.interfacekit.list.ListAssembly
import com.greencopper.interfacekit.multiproject.ProjectSwitcherAssembly
import com.greencopper.interfacekit.navigation.NavigationAssembly
import com.greencopper.interfacekit.navigation.localStorage.LayoutDataLocalStorageAssembly
import com.greencopper.interfacekit.network.InterfaceKitAPI
import com.greencopper.interfacekit.notification.NotificationAssembly
import com.greencopper.interfacekit.onboarding.OnboardingAssembly
import com.greencopper.interfacekit.permissions.PermissionsAssembly
import com.greencopper.interfacekit.rootview.RootViewAssembly
import com.greencopper.interfacekit.sample.SampleAssembly
import com.greencopper.interfacekit.search.SearchAssembly
import com.greencopper.interfacekit.session.SessionAssembly
import com.greencopper.interfacekit.tabBar.TabBarAssembly
import com.greencopper.interfacekit.textstyle.subsystem.TextStyleAssembly
import com.greencopper.interfacekit.ui.AppOverlay
import com.greencopper.interfacekit.ui.ConcreteAppOverlay
import com.greencopper.interfacekit.ui.ViewModelFactory
import com.greencopper.interfacekit.ui.compose.IKViewBuilder
import com.greencopper.interfacekit.utils.StoreCoroutineProvider
import com.greencopper.interfacekit.webview.WebviewAssembly
import com.greencopper.interfacekit.widgets.WidgetsAssembly
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.*
import com.greencopper.toolkit.di.container.Key
import com.greencopper.toolkit.di.resolver.resolve
import retrofit2.Retrofit

public class InterfaceKitAssembly : Assembly {

    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindAssembly(LayoutDataLocalStorageAssembly())
            bindAssembly(OnboardingAssembly())
            bindAssembly(RootViewAssembly())
            bindAssembly(TabBarAssembly())
            bindAssembly(NavigationAssembly())
            bindAssembly(SampleAssembly())
            bindAssembly(WebviewAssembly())
            bindAssembly(ColorAssembly())
            bindAssembly(WidgetsAssembly())
            bindAssembly(FullScreenMediaAssembly())
            bindAssembly(PermissionsAssembly())
            bindAssembly(LinksAssembly())
            bindAssembly(ProjectSwitcherAssembly())
            bindAssembly(EditorialPageAssembly())
            bindProvider { params -> ViewModelFactory(*(params.toArray())) }
            bindAssembly(CommandAssembly())
            bindAssembly(FilteringAssembly())
            bindAssembly(ImageServiceAssembly())
            bindAssembly(SearchAssembly())
            bindAssembly(InboxAssembly())
            bindAssembly(SessionAssembly())
            bindAssembly(TextStyleAssembly())
            bindAssembly(CounterAssembly())
            bindAssembly(AppReviewAssembly())
            bindAssembly(AccountProviderAssembly())
            bindAssembly(NotificationAssembly())
            bindAssembly(ListAssembly())
            bindAssembly(InterestsAssembly())
            bindAssembly(DraftContentAssembly())

            bindProvider(auto(::IKViewBuilder))
            bindProvider { StoreCoroutineProvider() }
            bindSingleton<InterfaceKitAPI> { resolve<Retrofit>().create(InterfaceKitAPI::class.java) }

            bindCommand<PresentAppStoreCommand>(PresentAppStoreCommand.key, auto(::PresentAppStoreCommand))

            bindProvider<AppOverlay> {
                ConcreteAppOverlay(
                    contentManager = resolve(),
                    draftContentManager = resolve(),
                    routeController = resolve(),
                    localizationService = resolve(),
                    viewBuilder = resolve(),
                    localStorage = resolve(),
                )
            }
        }
    }
}

public inline fun <reified VM : ViewModel> Registrar.bindViewModel(
    noinline creator: Creator<VM>
): Key = bindProvider<ViewModel>(tag = VM::class.java) { params ->
    creator(params)
}

public inline fun <reified VM : ViewModel> Fragment.viewModel(noinline args: (() -> List<Any?>)? = null): Lazy<VM> {
    return viewModels(factoryProducer = {
        val viewModelArgs = args?.invoke()?.toTypedArray() ?: arrayOf()
        App.resolve<ViewModelFactory>(args = viewModelArgs)
    })
}

public inline fun <reified VM : ViewModel> Fragment.activityViewModel(noinline args: (() -> List<Any?>)? = null): Lazy<VM> {
    return activityViewModels(factoryProducer = {
        val viewModelArgs = args?.invoke()?.toTypedArray() ?: arrayOf()
        App.resolve<ViewModelFactory>(args = viewModelArgs)
    })
}

public inline fun <reified T : Counter<*>> Registrar.bindCounter(
    key: Counter.Key,
    noinline creator: Creator<T>
): Key = bindProvider<Counter<*>>(tag = key) { params ->
    creator(params)
}

public inline fun <reified T : AccountProvider> Registrar.bindAccountProvider(
    key: AccountProvider.Key,
    noinline creator: Creator<T>
): Key = bindSingleton<AccountProvider>(tag = key) { params ->
    creator(params)
}
