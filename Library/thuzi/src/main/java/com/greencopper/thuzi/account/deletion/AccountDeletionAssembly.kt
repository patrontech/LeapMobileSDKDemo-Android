package com.greencopper.thuzi.account.deletion

import com.greencopper.core.conditions.conditionchecker.Condition
import com.greencopper.interfacekit.bindViewModel
import com.greencopper.interfacekit.navigation.feature.bindFeature
import com.greencopper.thuzi.account.deletion.initializer.AccountDeletionInitializer
import com.greencopper.thuzi.account.registration.ThuziRegisteredCondition
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.bindProvider
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.Dispatchers

internal class AccountDeletionAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindFeature(AccountDeletionInitializer.key) {
                AccountDeletionInitializer(
                    registeredCondition = resolve<Condition>(tag = ThuziRegisteredCondition.key) as ThuziRegisteredCondition,
                    localStorage = resolve(),
                )
            }

            bindViewModel {
                AccountDeletionViewModel(
                    deleteAccountService = resolve(),
                    aggregateMetricsService = resolve(),
                    coroutineDispatcher = Dispatchers.IO,
                )
            }

            bindProvider<DeleteAccountService> {
                ConcreteDeleteAccountService(
                    secretService = resolve(),
                    signatureGenerator = resolve(),
                    registrationManager = resolve(),
                    thuziAPI = resolve(),
                    localStorage = resolve(),
                    logger = resolve(),
                    App.locale,
                )
            }
        }
    }
}
