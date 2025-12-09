package com.greencopper.toolkit.testingdata

import kotlinx.serialization.Serializable

@Serializable
internal sealed class Animal(open val name: String)
internal interface Zoo

@Serializable
internal data class Dog(private val dogName: String) : Animal(dogName)
internal data class Cat(override val name: String) : Animal(name)
internal data class Rat(override val name: String) : Animal(name)
internal data class Zoo1(val dog: Dog): Zoo
internal data class Zoo2(val dog: Dog, val dog2: Dog): Zoo
internal data class Zoo3(val dog: Dog, val dog2: Dog, val dog3: Dog): Zoo
internal data class Zoo4(val dog: Dog, val dog2: Dog, val dog3: Dog, val dog4: Dog): Zoo
internal data class Zoo5(val dog: Dog, val dog2: Dog, val dog3: Dog, val dog4: Dog, val dog5: Dog): Zoo
internal data class Zoo6(val dog: Dog, val dog2: Dog, val dog3: Dog, val dog4: Dog, val dog5: Dog, val dog6: Dog): Zoo
