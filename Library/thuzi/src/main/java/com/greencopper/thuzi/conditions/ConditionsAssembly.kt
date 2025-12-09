package com.greencopper.thuzi.conditions

import com.greencopper.core.conditions.conditionchecker.bindCondition
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.resolver.lazyResolver
import com.greencopper.toolkit.di.binding.auto

internal class ConditionsAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindCondition(CustomAnswerCondition.key) {
                CustomAnswerCondition(lazyLocalStorage = lazyResolver())
            }
            bindCondition(VirtualAccessCardCondition.key, auto(::VirtualAccessCardCondition))
        }
    }
}