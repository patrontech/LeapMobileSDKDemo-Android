package com.greencopper.toolkit.di

import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.bindProvider
import com.greencopper.toolkit.testingdata.Dice
import com.greencopper.toolkit.testingdata.RandomDice

internal fun bindTestingAssembly(registrar: Registrar) {
    registrar.apply {
        bindProvider<Dice> { params ->
            RandomDice(params[0])
        }
        bindAssembly(AnimalAssembly())
    }
}

internal class TestingAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        bindTestingAssembly(registrar)
    }
}