package com.greencopper.ticketing.providers.showclix

import com.greencopper.core.conditions.conditionchecker.bindCondition
import com.greencopper.core.secrets.SecretMissingException
import com.greencopper.core.secrets.SecretService
import com.greencopper.interfacekit.bindViewModel
import com.greencopper.interfacekit.commands.system.Command
import com.greencopper.interfacekit.commands.system.bindCommand
import com.greencopper.interfacekit.onboarding.initializers.OnboardingPageInitializer
import com.greencopper.ticketing.providers.Provider
import com.greencopper.ticketing.providers.showclix.conditions.ShowclixLoggedInCondition
import com.greencopper.ticketing.providers.showclix.conditions.ShowclixTicketsCondition
import com.greencopper.ticketing.providers.showclix.login.ShowclixLoginMagicLinkCommand
import com.greencopper.ticketing.providers.showclix.login.ShowclixLoginPageInitializer
import com.greencopper.ticketing.providers.showclix.login.ShowclixLoginViewModel
import com.greencopper.ticketing.providers.showclix.repository.ConcreteShowclixMemberRepository
import com.greencopper.ticketing.providers.showclix.repository.ShowclixMemberRepository
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.auto
import com.greencopper.toolkit.di.binding.bindProvider
import com.greencopper.toolkit.di.binding.bindSingleton
import com.greencopper.toolkit.di.resolver.lazyResolver
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import retrofit2.Retrofit

internal class ShowclixAssembly : Assembly {

    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindProvider<OnboardingPageInitializer>(tag = ShowclixLoginPageInitializer.key) {
                resolve<SecretService>()["showclixApi"]
                    ?: throw SecretMissingException("showclixApi secret missing")
                ShowclixLoginPageInitializer()
            }
            bindCondition(ShowclixLoggedInCondition.key) {
                ShowclixLoggedInCondition(lazyResolver())
            }
            bindCondition(ShowclixTicketsCondition.key) {
                ShowclixTicketsCondition(lazyResolver())
            }
            bindViewModel(auto(::ShowclixLoginViewModel))
            bindProvider<ShowclixMemberRepository> {
                val showclixApiSharedKey = resolve<SecretService>()["showclixApi"]
                    ?: throw SecretMissingException("showclixApi secret missing")
                ConcreteShowclixMemberRepository(
                    showclixAPI = resolve(),
                    signatureGenerator = resolve(),
                    showclixApiSharedKey = showclixApiSharedKey,
                    remoteStateDispatcher = resolve(),
                    scope = CoroutineScope(Dispatchers.IO),
                    localStorage = resolve(),
                )
            }
            bindProvider<Provider>(tag = ShowclixProvider.key) {
                ShowclixProvider(
                    showclixMemberRepository = resolve(),
                    localStorage = resolve(),
                )
            }
            bindCommand<Command>(ShowclixLoginMagicLinkCommand.key) {
                ShowclixLoginMagicLinkCommand(
                    resolve(),
                    resolve(),
                    resolve(),
                    CoroutineScope(Dispatchers.IO)
                )
            }

            bindSingleton<ShowclixAPI> { resolve<Retrofit>().create(ShowclixAPI::class.java) }
        }
    }
}
