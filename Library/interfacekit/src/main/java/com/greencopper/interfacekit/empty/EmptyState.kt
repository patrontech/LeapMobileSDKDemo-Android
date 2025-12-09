package com.greencopper.interfacekit.empty

import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import kotlinx.serialization.Serializable

/**
 * Used in TCA states
 */
@Serializable
public open class EmptyState(
    public val title: String,
    public val subtitle: String,
    public val imageName: String,
    public val topWidgetCollection: WidgetCollectionConfiguration.Instance? = null,
    public val screenName: String,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EmptyState) return false

        if (title != other.title) return false
        if (subtitle != other.subtitle) return false
        if (imageName != other.imageName) return false
        if (topWidgetCollection != other.topWidgetCollection) return false
        if (screenName != other.screenName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + subtitle.hashCode()
        result = 31 * result + imageName.hashCode()
        result = 31 * result + (topWidgetCollection?.hashCode() ?: 0)
        result = 31 * result + screenName.hashCode()
        return result
    }

    override fun toString(): String {
        return "EmptyState(title='$title', subtitle='$subtitle', imageName='$imageName', topWidgetCollection='$topWidgetCollection', screenName='$screenName')"
    }
}
