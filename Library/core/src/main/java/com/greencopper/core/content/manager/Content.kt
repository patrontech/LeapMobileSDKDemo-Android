package com.greencopper.core.content.manager

import com.greencopper.core.content.archive.ContentArchive
import com.greencopper.core.content.ota.OTAContent
import com.greencopper.core.content.recipe.ContentRecipeInfo
import com.greencopper.core.content.recipe.ContentRecipeKey
import com.greencopper.core.data.KiboSerializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal typealias ContentSchema = Int
internal typealias ContentVersion = Int

@Serializable
public data class Content(
    val archive: ContentArchive,
    val version: ContentVersion,
    val schema: ContentSchema,
    val project: String,
    val type: OTAContent.Type? = null,
) : KiboSerializable<Content> {
    val stateHistory: StateHistory = StateHistory()
    var currentState: State
        get() = stateHistory.currentState
        internal set(value) = stateHistory.append(value)

    val processedRecipes: Set<ContentRecipeKey>
        get() = stateHistory.processedRecipeKeys ?: emptySet()

    val enabledRecipes: Set<ContentRecipeInfo>
        get() = stateHistory.enabledRecipes ?: emptySet()

    override fun toString(): String {
        return "Content(archive=$archive, project='$project', currentState=$currentState)"
    }

    override fun equals(other: Any?): Boolean {
        return (other as? Content)?.let {
            project == it.project
                    && version == other.version
                    && schema == other.schema
                    && type == other.type
        } ?: false
    }

    override fun hashCode(): Int {
        var result = archive.hashCode()
        result = 31 * result + version
        result = 31 * result + schema
        result = 31 * result + project.hashCode()
        result = 31 * result + stateHistory.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    override fun getSerializer(): KSerializer<Content> = serializer()
}
