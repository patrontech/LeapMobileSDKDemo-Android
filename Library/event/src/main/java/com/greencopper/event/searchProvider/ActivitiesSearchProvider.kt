package com.greencopper.event.searchProvider

import androidx.core.text.HtmlCompat
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.event.activity.ContentActivity
import com.greencopper.event.activity.data.repository.ActivityRepository
import com.greencopper.interfacekit.filtering.FilteringPredicate
import com.greencopper.interfacekit.links.resolver.LinkResolver
import com.greencopper.interfacekit.search.logic.ParameterizedSearchProvider
import com.greencopper.interfacekit.search.logic.SearchEntry
import com.greencopper.interfacekit.search.logic.SearchProviderKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

internal class ActivitiesSearchProvider(
    private val activityRepository: ActivityRepository,
    private val localizationService: LocalizationService,
    private val linkResolver: LinkResolver,
): ParameterizedSearchProvider<ActivitiesSearchProvider.Params> {

    override fun entries(params: Params): List<SearchEntry> {
        val query = params.predicate?.query()
        return runBlocking {
            activityRepository.getActivitiesForTags(query?.toSQL()).first()
        }.map {
            val routeLink = linkResolver.routeUri(params.routeLink, mapOf("activityId" to it.itemId.toString()))

            SearchEntry(
                matches = matchFields(params.fields, it),
                viewData = SearchEntry.ViewData.TitleSubtitle(
                    title = localizationService.getString(it.name),
                    subtitle = localizationService.getString(it.subtitle),
                    image = it.photos.firstOrNull(),
                    routeLink = routeLink.toString(),
                )
            )
        }
    }

    private fun matchFields(paramFields: List<String>, activity: ContentActivity): List<String> {
        val fields: MutableList<String> = mutableListOf()

        if (paramFields.contains("name")) {
            fields.add(localizationService.getString(activity.name))
        }

        if (paramFields.contains("subtitle")) {
            localizationService.getString(activity.subtitle)?.let { fields.add(it) }
        }

        if (paramFields.contains("tags")) {
            activity.tags.mapNotNull { tag ->
                localizationService.getStringFromRepository(tag)?.let { fields.add(it) }
            }
        }

        if (paramFields.contains("description")) {
            activity.description?.let { htmlDescription ->
                fields.add(
                    HtmlCompat
                        .fromHtml(localizationService.getString(htmlDescription), HtmlCompat.FROM_HTML_MODE_LEGACY)
                        .toString()
                )
            }
        }

        return fields
    }

    override fun deserialize(encodedParams: JsonElement): Params =
        KiboSerializable.decodeFromJsonElement(encodedParams)

    @Serializable
    data class Params(
        val predicate: FilteringPredicate? = null,
        val fields: List<String> = listOf("name", "subtitle"),
        @SerialName("onTap") val routeLink: String,
    ) : KiboSerializable<Params> {
        override fun getSerializer(): KSerializer<Params> = serializer()
    }

    companion object {
        internal val key = SearchProviderKey("Event.Activities", 1)
    }
}
