package com.greencopper.core.content.manager

import com.greencopper.core.content.recipe.ContentRecipeInfo
import com.greencopper.core.content.recipe.ContentRecipeKey
import com.greencopper.core.content.serializers.InstantSerializer
import com.greencopper.core.data.KiboSerializable
import com.greencopper.toolkit.App
import com.greencopper.toolkit.extensions.getFormattedDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.FormatStyle

@Serializable
public class StateHistory : KiboSerializable<StateHistory> {

    override fun getSerializer(): KSerializer<StateHistory> = serializer()

    private val _states: MutableList<State> = mutableListOf(State.Created())
    public val states: List<State>
        get() = _states

    public val currentState: State
        get() = states.last()

    public val enabledRecipes: Set<ContentRecipeInfo>?
        get() = states.filterIsInstance<State.Opened>().firstOrNull()?.enabledRecipes

    public val processedRecipeKeys: Set<ContentRecipeKey>?
        get() = states.filterIsInstance<State.Processed>().firstOrNull()?.processedRecipeKeys

    public fun append(state: State) {
        if (state::class != currentState::class) {
            _states.add(state)
        }
    }
}

public interface Timed {
    public val date: Instant
}

@Serializable
public sealed class State : KiboSerializable<State>, Timed {

    override fun getSerializer(): KSerializer<State> = serializer()

    @Serializable
    public data class Created(
        @Serializable(with = InstantSerializer::class)
        override val date: Instant = App.date().toInstant()
    ) : State()

    @Serializable
    public data class Opening(
        @Serializable(with = InstantSerializer::class)
        override val date: Instant = App.date().toInstant()
    ) : State()

    @Serializable
    public data class Opened(
        @SerialName("enabledRecipeKeys")
        public val enabledRecipes: Set<ContentRecipeInfo>,
        @Serializable(with = InstantSerializer::class)
        override val date: Instant = App.date().toInstant()
    ) : State()

    @Serializable
    public data class OpeningFailed(
        @Serializable(with = InstantSerializer::class)
        override val date: Instant = App.date().toInstant()
    ) : State()

    @Serializable
    public data class Processing(
        @Serializable(with = InstantSerializer::class)
        override val date: Instant = App.date().toInstant()
    ) : State()

    @Serializable
    public data class ProcessingFailed(
        @Serializable(with = InstantSerializer::class)
        override val date: Instant = App.date().toInstant()
    ) : State()

    @Serializable
    public data class Processed(
        public val processedRecipeKeys: Set<ContentRecipeKey>,
        @Serializable(with = InstantSerializer::class)
        override val date: Instant = App.date().toInstant()
    ) : State()

    @Serializable
    public data class Applying(
        @Serializable(with = InstantSerializer::class)
        override val date: Instant = App.date().toInstant()
    ) : State()

    @Serializable
    public data class ApplyingFailed(
        @Serializable(with = InstantSerializer::class)
        override val date: Instant = App.date().toInstant()
    ) : State()

    @Serializable
    public data class Applied(
        public val recipeKeys: Set<ContentRecipeKey>,
        @Serializable(with = InstantSerializer::class)
        override val date: Instant = App.date().toInstant()
    ) : State()

    @Serializable
    public data class Cleaned(
        @Serializable(with = InstantSerializer::class)
        override val date: Instant = App.date().toInstant()
    ) : State()

    override fun toString(): String {
        val formattedDateTime = ZonedDateTime.ofInstant(date, App.zoneId).getFormattedDateTime(
            FormatStyle.MEDIUM, FormatStyle.MEDIUM, App.zoneId)
        return "State.${javaClass.simpleName} { date: $formattedDateTime }"
    }

    override fun equals(other: Any?): Boolean {
        return (other as? State)?.let {
            javaClass.simpleName == other.javaClass.simpleName
        } ?: false
    }

    override fun hashCode(): Int = date.hashCode()


}
