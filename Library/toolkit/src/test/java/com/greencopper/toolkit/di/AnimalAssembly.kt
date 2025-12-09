package com.greencopper.toolkit.di

import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.auto
import com.greencopper.toolkit.di.binding.bindProvider
import com.greencopper.toolkit.di.binding.bindSingleton
import com.greencopper.toolkit.testingdata.*
import kotlin.math.pow
import kotlin.random.Random

internal class AnimalAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindProvider { params ->
                Cat(params.getOrNull(0) ?: randomNumberString())
            }
            bindProvider(tag = "numberedCat") { params ->
                val catNumber: Int = params.toArray()[0] as Int
                Cat("Cat number $catNumber")
            }
            bindProvider(tag = "dogCat") { params ->
                val dogNumber: Int = params[0]
                Dog("Dog number $dogNumber")
            }
            bindProvider(tag = "shortCat") { params ->
                val catNumber: Short = params[0]
                Cat("Short cat number $catNumber")
            }
            bindProvider(tag ="blueCat") { params ->
                val catNumber: Short = params[0]
                Cat("Short blue cat number $catNumber")
            }
            bindProvider(tag = Dog("Louis")) {
                Cat("John")
            }
            bindProvider(tag ="bestCat") {
                Cat("Charlie")
            }
            bindSingleton {
                Dog("Bernard")
            }
            bindSingleton(tag = "withTag") {
                Dog("Laurent")
            }
            bindSingleton(auto(::Zoo1))
            bindSingleton(auto(::Zoo2))
            bindSingleton(auto(::Zoo3))
            bindSingleton(auto(::Zoo4))
            bindSingleton(auto(::Zoo5))
            bindSingleton(auto(::Zoo6))

            bindSingleton<Zoo>(tag = 1, auto(::Zoo1))
            bindSingleton<Zoo>(tag = 2, auto(::Zoo2))
            bindSingleton<Zoo>(tag = 3, auto(::Zoo3))
            bindSingleton<Zoo>(tag = 4, auto(::Zoo4))
            bindSingleton<Zoo>(tag = 5, auto(::Zoo5))
            bindSingleton<Zoo>(tag = 6, auto(::Zoo6))
        }
    }

    private fun randomNumberString(): String {
        return Random.nextDouble(20.0.pow(9), 20.0.pow(10)).toString()
    }
}