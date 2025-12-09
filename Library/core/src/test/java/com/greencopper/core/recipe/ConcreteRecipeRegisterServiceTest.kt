package com.greencopper.core.recipe

import android.content.Context
import com.greencopper.core.content.recipe.ContentRecipe
import com.greencopper.core.content.recipe.ContentRecipeKey
import com.greencopper.core.content.recipe.RecipeOverride
import com.greencopper.core.content.recipe.TryContentRecipe
import com.greencopper.core.services.ConcreteRecipeRegisterService
import com.greencopper.testmocks.core.MockContentManager
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.toolkit.MockBuildConfigProvider
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.bindRecipe
import com.greencopper.toolkit.di.binding.bindRecipeOverride
import com.greencopper.toolkit.di.resolver.resolve
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

internal class ConcreteRecipeRegisterServiceTest {
    private val context: Context = mockk(relaxed = true)
    private var appliedContentRecipes: MutableList<ContentRecipe> = mutableListOf()
    private val contentManager = MockContentManager(registerValue = {
        appliedContentRecipes.add(it)
    })
    private val buildConfigProvider = MockBuildConfigProvider()
    private val realRecipe = TestContentRecipeReal()
    private val recipeOverride = TestContentRecipeOverride()

    private val classUnderTest = ConcreteRecipeRegisterService(context, contentManager, buildConfigProvider)

    init {
        Toolkit.setupTest()
        App.resolve<Registrar>().apply {
            bindRecipe { realRecipe }
            bindRecipeOverride { recipeOverride }
        }
    }

    @Test
    fun testWhenRelease_shouldRegisterRealRecipe() {
        buildConfigProvider.mockIsDebug = false
        classUnderTest.register()
        assertThat(appliedContentRecipes.contains(realRecipe)).isTrue
        assertThat(appliedContentRecipes.contains(recipeOverride)).isFalse
    }

    @Test
    fun testWhenDebugAndOverrideDoesntExists_shouldRegisterRealRecipe() {
        buildConfigProvider.mockIsDebug = true
        recipeOverride.overrideConfigExists = false
        classUnderTest.register()
        assertThat(appliedContentRecipes.contains(realRecipe)).isTrue
        assertThat(appliedContentRecipes.contains(recipeOverride)).isFalse
    }

    @Test
    fun testWhenDebugAndOverrideExists_shouldRegisterRecipeOverride() {
        buildConfigProvider.mockIsDebug = true
        recipeOverride.overrideConfigExists = true
        classUnderTest.register()
        assertThat(appliedContentRecipes.contains(realRecipe)).isFalse
        assertThat(appliedContentRecipes.contains(recipeOverride)).isTrue
    }

    @Test
    fun testWhenRecipeOverrideDoesntExists_shouldRegisterRealRecipe() {
        buildConfigProvider.mockIsDebug = true
        classUnderTest.register()
        assertThat(appliedContentRecipes.contains(realRecipe)).isTrue
        assertThat(appliedContentRecipes.contains(recipeOverride)).isFalse
    }

    @Test
    fun testWhenRegisteredWrongRecipe() {
        App.resolve<Registrar>().bindRecipeOverride { TestContentRecipeReal() }
        buildConfigProvider.mockIsDebug = true
        classUnderTest.register()
        assertThat(appliedContentRecipes.contains(realRecipe)).isTrue
        assertThat(appliedContentRecipes.contains(recipeOverride)).isFalse
    }
}

internal open class TestContentRecipeReal : TryContentRecipe {
    override val key: ContentRecipeKey = ContentRecipeKey("Test", 1)
    override val componentPath: String = ""

    override suspend fun tryToProcess(unarchivedDirectory: File, contentDirectory: File) {}
    override suspend fun tryToApply(contentDirectory: File) {}
}

internal class TestContentRecipeOverride: TestContentRecipeReal(), RecipeOverride {
    override val componentPathOverride = componentPath

    var overrideConfigExists = false

    override fun overrideConfigExists(context: Context): Boolean =
        overrideConfigExists
}
