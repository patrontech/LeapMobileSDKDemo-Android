package com.greencopper.maps.searchProvider

import android.net.Uri
import androidx.core.text.HtmlCompat
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.interfacekit.filtering.FilteringPredicate
import com.greencopper.interfacekit.search.logic.*
import com.greencopper.maps.common.LocationData
import com.greencopper.maps.recipe.MapsRepository
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.*
import kotlinx.serialization.json.JsonElement

internal class LocationsSearchProvider (
    private val mapsRepository: MapsRepository,
    private val localizationService: LocalizationService,
): ParameterizedSearchProvider<LocationsSearchProvider.Params> {

    @Serializable
    data class Params(
        val predicate: FilteringPredicate? = null,
        val fields: List<String> = listOf("name", "subtitle"),
        @SerialName("onTap") val routeLink: String,
    ): KiboSerializable<Params> {
        override fun getSerializer(): KSerializer<Params> = serializer()
    }

    override fun entries(params: Params): List<SearchEntry> {
        val query = params.predicate?.query()
        val routeLinkUri = Uri.parse(params.routeLink)
        return runBlocking {
            mapsRepository.getLocationsRearranged(query?.toPredicate())
        }.map { location ->
            val routeLink =
                routeLinkUri.buildUpon().appendQueryParameter("locationId", "\"${location.itemId}\"").build().toString()
            SearchEntry(
                matches = matchFields(params.fields, location),
                viewData = SearchEntry.ViewData.TitleSubtitle(
                    title = localizationService.getString(location.name),
                    subtitle = localizationService.getString(location.subtitle),
                    image = location.images.firstOrNull(),
                    routeLink = routeLink,
                )
            )
        }

    }

    private fun matchFields(paramFields: List<String>, location: LocationData): List<String> {
        val fields: MutableList<String> = mutableListOf()

        if (paramFields.contains("name")) {
            fields.add(localizationService.getString(location.name))
        }

        if (paramFields.contains("subtitle")) {
            localizationService.getString(location.subtitle)?.let { fields.add(it) }
        }

        if (paramFields.contains("address")) {
            localizationService.getString(location.address)?.let { fields.add(it) }
        }

        if (paramFields.contains("tags")) {
            location.tags.mapNotNull { tag ->
                localizationService.getStringFromRepository(tag)?.let { fields.add(it) }
            }
        }

        if (paramFields.contains("description")) {
            location.description?.let { htmlDescription ->
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

    companion object {
        internal val key = SearchProviderKey("Maps.Locations", 1)
    }
}
