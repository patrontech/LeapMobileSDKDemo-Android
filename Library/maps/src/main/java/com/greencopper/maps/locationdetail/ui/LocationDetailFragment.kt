package com.greencopper.maps.locationdetail.ui

import android.animation.AnimatorInflater
import android.animation.StateListAnimator
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.*
import com.greencopper.core.conditions.conditionchecker.ConditionChecker
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.core.metrics.labels.*
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.core.services.track
import com.greencopper.interfacekit.color.ScreenColor
import com.greencopper.interfacekit.common.setOnSafeClickListener
import com.greencopper.interfacekit.favorites.toFavoriteable
import com.greencopper.interfacekit.favorites.translate
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.layout.RedirectableLayout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.*
import com.greencopper.interfacekit.ui.fragment.ParameterizedFragment
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.DefaultBackCloseToolbarNavigationControlsHandler
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.NavigationControlsHandler
import com.greencopper.interfacekit.viewModel
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import com.greencopper.kiba_maps.R
import com.greencopper.kiba_maps.databinding.LocationDetailFragmentBinding
import com.greencopper.maps.colors.MapsColor
import com.greencopper.maps.common.ui.AddToMyLocationsAnalytics
import com.greencopper.maps.common.ui.RemoveFromMyLocationsAnalytics
import com.greencopper.maps.locationdetail.*
import com.greencopper.maps.metrics.locationDetail
import com.greencopper.maps.textstyle.MapsTextStyle
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.extensions.decodeHtmlString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement


internal class LocationDetailFragment : ParameterizedFragment<LocationDetailLayoutData>, RedirectableLayout {

    constructor(params: LocationDetailLayoutData) : super(params)

    @Deprecated("To conform to system and provide to FragmentFactory an empty constructor accessed by reflection")
    constructor() : super(null)

    override val screenColor: ScreenColor get() = MapsColor.locationDetail
    override val binding: LocationDetailFragmentBinding by viewBinding(LocationDetailFragmentBinding::inflate)

    override fun createNavigationControlsHandler(): NavigationControlsHandler =
        DefaultBackCloseToolbarNavigationControlsHandler(
            this,
            binding.simpleToolbarLocationDetail,
            MapsColor.locationDetail.topBar,
            MapsTextStyle.locationDetail.topBar,
        )

    override val redirectionHash: RedirectionHash
        get() = data.redirectionHash

    private val viewModel: LocationDetailViewModel by viewModel()

    private val metricService: AggregateMetricsService by App.lazy()
    private val json: Json by App.lazy()

    private var shadowAdded = false
    private lateinit var animationRemoveShadow: StateListAnimator
    private lateinit var animationAddShadow: StateListAnimator
    private var parentFeatureInfo: FeatureInfo? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let { bundle ->
            parentFeatureInfo = bundle.getString(KEY_SAVED_PARENT_FEATURE_INFO)
                ?.let { json.decodeFromString(it) }
        }
        bindStyles()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getLocationDetails(data.locationId, data.displayableTags)
                .filterNotNull()
                .flowOn(Dispatchers.IO)
                .collectLatest { viewData ->
                    bindLocationViewData(viewData)
                }
        }
    }

    override fun onResume() {
        super.onResume()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getLocationDetails(data.locationId, data.displayableTags)
                .filterNotNull()
                .flowOn(Dispatchers.IO)
                .collectLatest { viewData ->
                    trackScreen(viewData)
                }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        parentFeatureInfo?.let { outState.putString(KEY_SAVED_PARENT_FEATURE_INFO, json.encodeToString(it)) }
    }

    override fun restoreData(encodedData: String): LocationDetailLayoutData =
        KiboSerializable.decodeFromString(encodedData)

    internal fun setParentFeatureInfo(parentFeatureInfo: FeatureInfo) {
        this.parentFeatureInfo = parentFeatureInfo
    }

    private fun bindStyles() {
        with(binding) {
            val colors = MapsColor.locationDetail
            val textStyles = MapsTextStyle.locationDetail
            clLocationDetailBackground.setBackgroundColor(colors.background)
            ablLocationDetailHeader.setBackgroundColor(colors.background)
            ablLocationDetailHeader.setShadowColor(colors.header.shadow)
            tvLocationDetailName.apply {
                setTextColor(colors.header.name)
                setFont(textStyles.header.name)
            }
            tvLocationDetailSubtitle.apply {
                setTextColor(colors.header.subtitle)
                setFont(textStyles.header.subtitle)
            }
            ivLocationDetailAddressIcon.setColorFilter(colors.addressIcon)
            tvLocationDetailAddress.apply {
                setTextColor(colors.address)
                setFont(textStyles.address)
            }
            tvLocationDetailDescriptionTitle.apply {
                setTextColor(colors.descriptionTitle)
                setFont(textStyles.description.title)
            }
            tvLocationDetailDescription.apply {
                setTextColor(colors.description)
                setFont(textStyles.description.text)
                movementMethod = ClickableLinkMovementMethod()
            }
            ivLocationDetailImage.strokeColor = ColorStateList.valueOf(colors.image.border)

            tagDisplayLocationDetail.setup(colors.tags)
        }
    }

    private fun bindLocationViewData(viewData: LocationDetailViewData) {
        with(binding) {
            vLocationDetailAddress.isVisible = !viewData.address.isNullOrEmpty()
            tvLocationDetailDescriptionTitle.isVisible = !viewData.description.isNullOrEmpty()
            wcvLocationDetail.isVisible = viewData.bottomWidgetCollection != null &&
                    viewData.bottomWidgetCollection.widgets.isNotEmpty()

            tvLocationDetailDescriptionTitle.text = viewData.descriptionTitle
            tvLocationDetailAddress.text = viewData.address
            tvLocationDetailName.text = viewData.name
            tvLocationDetailSubtitle.setTextOrGone(viewData.subtitle)
            tvLocationDetailDescription.setTextOrGone(viewData.description?.decodeHtmlString())

            if (!viewData.images.isNullOrEmpty()) {
                ivLocationDetailImage.isVisible = true
                ivLocationDetailImage.setImageFrom(
                    viewData.images[0],
                    viewLifecycleOwner.lifecycleScope,
                    hideIfUnknown = true,
                    hideIfLoading = true,
                )
            } else {
                ivLocationDetailImage.isVisible = false
            }

            tagDisplayLocationDetail.isVisible = viewData.tags.isNotEmpty()
            tagDisplayLocationDetail.setTags(viewData.tags)

            animationRemoveShadow =
                AnimatorInflater.loadStateListAnimator(context, R.animator.remove_shadow_animator)
            animationAddShadow = AnimatorInflater.loadStateListAnimator(context, R.animator.add_shadow_animator)
            ablLocationDetailHeader.stateListAnimator = animationRemoveShadow

            nsvLocationDetailContent.setOnScrollChangeListener { _, _, scrollY, _, _ ->
                if (scrollY == 0) {
                    ablLocationDetailHeader.stateListAnimator = animationRemoveShadow
                    shadowAdded = false
                } else if (!shadowAdded) {
                    ablLocationDetailHeader.stateListAnimator = animationAddShadow
                    shadowAdded = true
                }
            }
        }

        setupFavoriteIcon(viewData)
        viewData.bottomWidgetCollection?.let { setupWidgets(it) }
    }

    private fun trackScreen(viewData: LocationDetailViewData) {
        val screen = Screen.locationDetail(data.analytics.screenName)
        val parameters = mapOf(
            EventParameter.itemId to data.locationId,
            EventParameter.itemName to viewData.name,
        )
        metricService.track(ScreenViewEvent(screen, parameters))
    }

    private fun setupWidgets(config: WidgetCollectionConfiguration.Instance) =
        viewLifecycleOwner.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val conditionChecker: ConditionChecker = App.resolve()
                parentFeatureInfo?.let {
                    val metadata = mapOf("bottomSheetContainer" to parentFeatureInfo)
                    conditionChecker.metadata.value = json.encodeToJsonElement(metadata)
                }
                val widgetItems = viewModel.getWidgetItems(config.widgets)
                binding.wcvLocationDetail.bind(
                    widgetItems = widgetItems,
                    origin = this@LocationDetailFragment,
                    screenName = data.analytics.screenName,
                    conditionChecker = conditionChecker,
                    topMarginOverride = 16.dpToPx(),
                    bottomMarginOverride = 0,
                ).collect()
            }
        }

    private fun setupFavoriteIcon(details: LocationDetailViewData) {
        val favoritesEditing = data.favoritesEditing?.translate(App.resolve()) ?: return
        with(binding.locationDetailFavoriteAddRemove) {
            isVisible = true
            setColorFilter(MapsColor.locationDetail.header.myLocationIcon)
            val favoriteItem = data.locationId.toFavoriteable()
            if (details.isFavorite == true) {
                contentDescription = favoritesEditing.remove.accessibilityLabel
                setImageFrom(
                    favoritesEditing.remove.icon,
                    viewLifecycleOwner.lifecycleScope,
                    hideIfUnknown = true,
                    hideIfLoading = true,
                )
                setOnSafeClickListener {
                    viewModel.removeFromFavorite(favoriteItem)
                    App.track(
                        RemoveFromMyLocationsAnalytics(
                            screenName = data.analytics.screenName,
                            itemId = data.locationId,
                            itemName = details.name
                        )
                    )
                }
            } else {
                contentDescription = favoritesEditing.add.accessibilityLabel
                setImageFrom(
                    favoritesEditing.add.icon,
                    viewLifecycleOwner.lifecycleScope,
                    hideIfUnknown = true,
                    hideIfLoading = true,
                )
                setOnSafeClickListener {
                    viewModel.addToFavorite(favoriteItem)
                    App.track(
                        AddToMyLocationsAnalytics(
                            screenName = data.analytics.screenName,
                            itemId = data.locationId,
                            itemName = details.name
                        )
                    )
                }
            }
        }
    }

    private companion object {
        private const val KEY_SAVED_PARENT_FEATURE_INFO = "key.savedParentFeatureInfo"
    }
}
