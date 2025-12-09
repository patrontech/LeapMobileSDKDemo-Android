package com.greencopper.core.content.manager

import com.greencopper.core.content.recipe.ContentRecipe
import com.greencopper.core.content.recipe.ContentRecipeKey

public sealed class ContentException(cause: Throwable? = null) : Throwable(cause) {

    public class ProcessorProcessException(cause: Throwable?, private val content: Content) :
        ContentException(cause) {
        override val message: String
            get() {
                return "[ContentException] Exception processing $content : \n $cause"
            }
    }

    public class InitializerProcessException(cause: Throwable?) : ContentException(cause) {
        override val message: String
            get() = "[ContentException] Exception initializing : \n $cause"
    }

    public class CouldntOpenContentException(cause: Throwable?) : ContentException(cause) {
        override val message: String
            get() = "[ContentException] Couldn't open content because of : \n $cause"
    }

    public class ContentNotOpenedException : ContentException() {
        override val message: String
            get() = "[ContentException] Content hasn't been opened yet"
    }

    public class SchemaNotMatchingException(
        private val actual: ContentSchema,
        private val expected: ContentSchema
    ) : ContentException() {
        override val message: String
            get() = "[ContentException] Wrong schema, expected was: $expected, actual was: $actual"
    }

    public class RecipeException(cause: Throwable, private val recipe: ContentRecipe) :
        ContentException(cause) {
        override val message: String
            get() = "[ContentException] Exception with recipe $recipe : \n $cause"
    }

    public class AlreadyProcessedException : ContentException() {
        override val message: String
            get() = "[ContentException] Content was already processed"
    }

    public class ProcessorApplyException(cause: Throwable?, private val content: Content) :
        ContentException(cause) {
        override val message: String
            get() {
                return "[ContentException] Exception applying $content : \n $cause"
            }
    }

    public class RecipesNotMatchingException(
        private val actual: Set<ContentRecipeKey>,
        private val expected: Set<ContentRecipeKey>
    ) : ContentException() {
        override val message: String
            get() = "[ContentException] Recipes aren't matching, expected was: $expected, actual was: $actual"
    }

    public class UnreadyStateException(private val state: State) : ContentException() {
        override val message: String
            get() = "[ContentException] Content is not ready for applying, state is $state"
    }

    public class NoRecipeRegisteredException : ContentException() {
        override val message: String
            get() = "[ContentException] Trying to process content without any Recipe"
    }
}