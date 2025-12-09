package com.greencopper.toolkit.di

import com.greencopper.toolkit.di.assembly.assemble
import com.greencopper.toolkit.di.binding.bindSingleton
import com.greencopper.toolkit.di.binding.unbind
import com.greencopper.toolkit.di.container.Container
import com.greencopper.toolkit.di.resolver.*
import com.greencopper.toolkit.testingdata.Animal
import com.greencopper.toolkit.testingdata.Cat
import com.greencopper.toolkit.testingdata.Dog
import com.greencopper.toolkit.testingdata.Zoo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

internal class ResolverTest {
    private var container = Container()
    private val resolver: Resolver get() = container

    @BeforeEach
    fun setupEach() {
        container = Container()
        container.assemble(TestingAssembly(), AnimalAssembly())
    }

    @Test
    fun whenProviderBinding_shouldResolveDifferentInstance() {
        val cat1 = resolver.resolve<Cat>()
        val cat2 = resolver.resolve<Cat>()
        assertThat(cat1).isNotEqualTo(cat2)
    }

    @Test
    fun whenSingletonBinding_shouldResolveSameInstance() {
        val dog1 = resolver.tryResolve<Dog>()
        val dog2 = resolver.tryResolve<Dog>()
        assertThat(dog1).isNotNull
        assertThat(dog2).isNotNull
        assertThat(dog1).isEqualTo(dog2)
    }

    @Test
    fun whenFactoryBinding_withSameParameter_shouldResolveDifferentInstanceWithSameValues() {
        val firstCat = resolver.resolve<Cat>("Caramel")
        val secondCat = resolver.resolve<Cat>("Caramel")
        assertThat(firstCat).isNotSameAs(secondCat)
        // Values are equal
        assertThat(firstCat).isEqualTo(secondCat)
    }

    @Test
    fun whenFactoryBinding_withDifferentType_shouldResolveDifferentInstance() {
        val firstCat = resolver.resolve<Cat>(25, tag = "numberedCat")
        val secondCat = resolver.resolve<Cat>("25")
        assertThat(firstCat).isNotSameAs(secondCat)
    }

    @Test
    fun whenFactoryBinding_withTag_shouldResolveDifferentInstance() {
        val firstCat = resolver.resolve<Cat>(25.toShort(), tag = "shortCat")
        val catWithTag = resolver.resolve<Cat>(25.toShort(), tag = "blueCat")
        assertThat(firstCat).isNotSameAs(catWithTag)
        assertThat(firstCat).isNotEqualTo(catWithTag)
    }

    @Test
    fun whenResolving_withoutBinding_shouldResolveNull() {
        assertThat(resolver.tryResolve<Animal>(tag = "fakeTag")).isNull()
        assertThat(resolver.tryResolve<Animal>(65, tag = "fakeTag")).isNull()
    }

    @Test
    fun whenSingletonBinding_withSameTag_shouldResolveSameInstance() {
        val dog1 = resolver.tryResolve<Dog>(tag = "withTag")
        val dog2 = resolver.tryResolve<Dog>(tag = "withTag")
        assertThat(dog1).isNotNull
        assertThat(dog2).isNotNull
        assertThat(dog1).isEqualTo(dog2)
    }

    @Test
    fun whenSingletonBinding_withoutTag_shouldResolveDifferentInstance() {
        val dog1 = resolver.tryResolve<Dog>()
        val dog2 = resolver.tryResolve<Dog>(tag = "withTag")
        assertThat(dog1).isNotNull
        assertThat(dog2).isNotNull
        assertThat(dog1).isNotEqualTo(dog2)
    }

    @Test
    fun whenBinding_withObjectTag_shouldResolve() {
        val dogLouis = Dog("Louis")
        val catJohn = resolver.tryResolve<Cat>(tag = dogLouis)
        assertThat(catJohn).isNotNull
        assertThat(catJohn!!.name).isEqualTo("John")
    }

    @Test
    fun whenResolving_afterUnbinding_shouldFail() {
        val dog = resolver.tryResolve<Dog>()
        assertThat(dog).isNotNull
        container.unbind<Dog>()
        val dog2 = resolver.tryResolve<Dog>()
        assertThat(dog2).isNull()
    }

    @Test
    fun whenResolvingAll_withExactType_shouldResolve() {
        val zoos = assertDoesNotThrow {
            container.resolveAll<Zoo>()
        }
        assertThat(zoos.size).isEqualTo(6)
    }

    @Test
    fun whenResolvingAll_withSubclasses_shouldResolve() {
        val zoos = assertDoesNotThrow {
            container.resolveAll<Zoo>(allowSubclasses = true)
        }
        assertThat(zoos.size).isEqualTo(12)
    }

    @Test
    fun whenGettingLazyResolver_withoutTag_shouldSucceed() {
        val lazyDog = resolver.lazyResolver<Dog>()
        assertDoesNotThrow { lazyDog.resolve() }
    }

    @Test
    fun whenGettingLazyResolver_withUnknownTag_shouldSucceedButResolveNull() {
        val lazyDog = resolver.lazyResolver<Dog>(tag = 99)
        val dog = lazyDog.tryResolve()
        assertThat(dog).isNull()
    }

    @Test
    fun whenResolvingWithNestedContainer_shouldResolveFromParent_ifKeyAbsentInChild() {
        val child = Container(parent = container)
        val dog = child.tryResolve<Dog>()
        assertThat(dog).isNotNull
    }

    @Test
    fun whenResolvingWithNestedContainer_shouldResolveFromChild_ifKeyPresentInChild() {
        val child = Container(parent = container)
        child.bindSingleton { Dog("Rufus") }
        val childDog = child.tryResolve<Dog>()
        assertThat(childDog).isNotNull
        assertThat(childDog?.name).isEqualTo("Rufus")
        val parentDog = container.tryResolve<Dog>()
        assertThat(parentDog).isNotNull
        assertThat(parentDog).isNotEqualTo(childDog)
    }
}
