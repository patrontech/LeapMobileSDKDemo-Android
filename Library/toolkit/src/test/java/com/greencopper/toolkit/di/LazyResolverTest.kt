package com.greencopper.toolkit.di

import com.greencopper.toolkit.di.assembly.assemble
import com.greencopper.toolkit.di.container.Container
import com.greencopper.toolkit.di.resolver.LazyResolver
import com.greencopper.toolkit.di.resolver.lazyResolver
import com.greencopper.toolkit.testingdata.Cat
import com.greencopper.toolkit.testingdata.Dice
import com.greencopper.toolkit.testingdata.Dog
import com.greencopper.toolkit.testingdata.RandomDice
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

internal class LazyResolverTest {
    var container = Container()

    @BeforeEach
    fun setupEach() {
        container = Container()
        container.assemble(TestingAssembly())
    }

    @Test
    fun testAdHoc() {
        val lazyDog = LazyResolver.adhoc(Dog("Fido"))
        assertDoesNotThrow { lazyDog.resolve() }
    }

    @Test
    fun testLazyResolution() {
        val lazyDiceResolver = container.lazyResolver<Dice>()
        val dice = assertDoesNotThrow { lazyDiceResolver.resolve(3) }
        assertThat(dice).isInstanceOf(RandomDice::class.java)
    }

    @Test
    fun testLazyResolution_withTag() {
        val lazyCatResolver = container.lazyResolver<Cat>()
        val cat = lazyCatResolver.tryResolve(7.toShort(), tag = "blueCat")
        assertThat(cat).isNotNull
        assertThat(cat?.name).isEqualTo("Short blue cat number 7")
    }
}