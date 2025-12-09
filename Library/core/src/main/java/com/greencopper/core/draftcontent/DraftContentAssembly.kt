package com.greencopper.core.draftcontent

import com.greencopper.core.conditions.conditionchecker.bindCondition
import com.greencopper.core.networking.CoreAPI
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.auto
import com.greencopper.toolkit.di.binding.bindProvider
import com.greencopper.toolkit.di.resolver.lazyResolver
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.httpclient.APIProvider

internal class DraftContentAssembly : Assembly {

    override fun registerBindings(registrar: Registrar) {
        with (registrar) {
            bindProvider<DraftContentManager> {
                ConcreteDraftContentManager(
                    lazyLocalStorage = lazyResolver(),
                    coreConfigHolder = resolve(),
                    coreAPI = resolve<APIProvider<CoreAPI>>().api(),
                    lazyRemoteStateDispatcher = lazyResolver(),
                    json = resolve(),
                )
            }

            bindCondition(IsDraftContentEnabledCondition.key, auto(::IsDraftContentEnabledCondition))
        }
    }
}
