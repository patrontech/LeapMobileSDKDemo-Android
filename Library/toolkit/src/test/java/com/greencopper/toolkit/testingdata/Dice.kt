package com.greencopper.toolkit.testingdata

import kotlin.random.Random

internal interface Dice {
    fun getNumber(): Int
}

internal class RandomDice(private val sides: Int) : Dice {
    override fun getNumber(): Int {
        return Random.nextInt(sides) + 1
    }
}