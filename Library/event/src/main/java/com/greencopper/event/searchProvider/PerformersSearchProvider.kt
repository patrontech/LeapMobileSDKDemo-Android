package com.greencopper.event.searchProvider

import androidx.core.text.HtmlCompat
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.event.performers.Performer
import com.greencopper.event.performers.data.repository.PerformerRepository
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

internal class PerformersSearchProvider(
    private val performersRepository: PerformerRepository,
    private val localizationService: LocalizationService,
    private val linkResolver: LinkResolver,
) : ParameterizedSearchProvider<PerformersSearchProvider.Params> {

    override fun entries(params: Params): List<SearchEntry> {
        val query = params.predicate?.query()
        return runBlocking {
            performersRepository.getPerformersForTags(query?.toSQL()).first()
        }.map {
            val routeLink = linkResolver.routeUri(params.routeLink, mapOf("performerId" to "\"${it.itemId}\""))
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

    private fun matchFields(paramFields: List<String>, performer: Performer): List<String> {
        val fields: MutableList<String> = mutableListOf()

        if (paramFields.contains("name")) {
            fields.add(localizationService.getString(performer.name))
        }

        if (paramFields.contains("subtitle")) {
            localizationService.getString(performer.subtitle)?.let { fields.add(it) }
        }

        if (paramFields.contains("tags")) {
            performer.tags.mapNotNull { tag ->
                localizationService.getStringFromRepository(tag)?.let { fields.add(it) }
            }
        }

        if (paramFields.contains("description")) {
            performer.description?.let { htmlDescription ->
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
        @SerialName("onTap") val routeLink: String
    ) : KiboSerializable<Params> {
        override fun getSerializer(): KSerializer<Params> = serializer()
    }

    companion object {
        internal val key = SearchProviderKey("Event.Performers", 1)
    }
}
