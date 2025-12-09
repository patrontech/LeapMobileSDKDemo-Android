package com.greencopper.core.conditions

import com.greencopper.core.conditions.conditionchecker.*
import com.greencopper.core.conditions.parser.ComplexPredicateParser
import com.greencopper.core.conditions.parser.PredicateParser
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.auto
import com.greencopper.toolkit.di.binding.bindProvider
import com.greencopper.toolkit.di.resolver.resolve

internal class ConditionsAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindProvider<PredicateParser>(auto(::ComplexPredicateParser))
            bindProvider<ConditionResolver> { DIConditionResolver() }
            bindProvider<ConditionChecker>(auto(::ConcreteConditionChecker))
            bindCondition(PlatformCondition.key, auto(::PlatformCondition))
            bindCondition(TimeCondition.key) {
                TimeCondition(timezoneProvider = App.resolve())
            }
            bindCondition(MetadataQueryCondition.key, auto(::MetadataQueryCondition))
            bindCondition(AppVersionCondition.key, auto(::AppVersionCondition))
        }
    }
}
