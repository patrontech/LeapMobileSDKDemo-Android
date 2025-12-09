package com.greencopper.thuzi.badges.ui

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Rect
import android.graphics.drawable.*
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.core.metrics.labels.*
import com.greencopper.core.metrics.provider.MappedProvider
import com.greencopper.core.services.localizationService
import com.greencopper.core.services.track
import com.greencopper.interfacekit.color.ScreenColor
import com.greencopper.interfacekit.common.setOnSafeClickListener
import com.greencopper.interfacekit.navigation.layout.RedirectableLayout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.*
import com.greencopper.interfacekit.ui.fragment.ParameterizedFragment
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.DefaultButtonsNavigationControlsHandler
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.NavigationControlsHandler
import com.greencopper.interfacekit.viewModel
import com.greencopper.thuzi.R
import com.greencopper.thuzi.badges.BadgeViewData
import com.greencopper.thuzi.badges.BadgesViewModel
import com.greencopper.thuzi.badges.initializer.BadgesLayoutData
import com.greencopper.thuzi.databinding.BadgeDetailFragmentBinding
import com.greencopper.thuzi.databinding.BadgesFragmentBinding
import com.greencopper.thuzi.style.ThuziColor.badges
import com.greencopper.thuzi.style.ThuziTextStyle
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

internal class BadgesFragment : ParameterizedFragment<BadgesLayoutData>, RedirectableLayout {

    constructor(badgesData: BadgesLayoutData) : super(badgesData)

    @Deprecated("Only for system purpose not to be called")
    constructor() : super(null)

    private lateinit var errorDialog: AlertDialog

    override val screenColor: ScreenColor get() = badges

    override val binding: BadgesFragmentBinding by viewBinding(BadgesFragmentBinding::inflate)
    private val adapter: BadgesAdapter = BadgesAdapter(this::openDetailDialog)
    private val viewModel: BadgesViewModel by viewModel()
    private val localizationService: LocalizationService by App.lazy()

    private val Screen.Companion.badges: Screen
        get() = Screen(data.analytics.screenName, "thuzi_badges")

    override fun createNavigationControlsHandler(): NavigationControlsHandler =
        DefaultButtonsNavigationControlsHandler(
            this,
            binding.navigateBackButton,
            binding.navigateCloseButton,
            badges.topBar
        )

    override val redirectionHash: RedirectionHash
        get() = data.redirectionHash

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val builder = AlertDialog.Builder(context)
        builder.setTitle(App.localizationService().getString("common.an_error_occured"))
        builder.setPositiveButton(App.localizationService().getString("common.ok")) { dialog, _ ->
            dialog.dismiss()
        }
        errorDialog = builder.create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        collectBadges()
    }

    override fun onResume() {
        super.onResume()
        App.track(ScreenViewEvent(Screen.badges))
    }

    override fun restoreData(encodedData: String): BadgesLayoutData =
        KiboSerializable.decodeFromString(encodedData)

    private fun initView() {
        binding.apply {
            root.setBackgroundColor(badges.background)

            val headerColors = badges.header
            val headerTextStyles = ThuziTextStyle.badges.header

            with(badgesTitle) {
                setTextColor(headerColors.title)
                setFont(headerTextStyles.title)
                setOtaText("thuzi.badges.title")
            }
            with(badgesDescription) {
                setTextColor(headerColors.description)
                setFont(headerTextStyles.description)
                setOtaText("thuzi.badges.description")
            }
            with(badgesCounter) {
                setTextColor(headerColors.number)
                setFont(headerTextStyles.number)
            }

            (badgesRv.layoutManager as? GridLayoutManager)?.spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val itemCount = adapter.itemCount
                    val itemsInLastRow = itemCount % 3
                    val isInLastRow = position >= itemCount - itemsInLastRow
                    return when {
                        isInLastRow && itemsInLastRow == 1 -> 12
                        isInLastRow && itemsInLastRow == 2 -> 6
                        else -> 4
                    }
                }
            }
            badgesRv.addItemDecoration(CenterLastRowItemDecoration(3))
            badgesRv.adapter = adapter
        }
    }

    private fun collectBadges() =
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getBadges(data.url)
                .flowOn(Dispatchers.IO)
                .catch {
                    binding.progressLoader.isVisible = false
                    if (!errorDialog.isShowing) {
                        errorDialog.show()
                    }
                }
                .collect { badgesViewData ->
                    with(binding) {
                        progressLoader.isVisible = false
                        badgesCounter.text = "${badgesViewData.count { it.isEarned }}/${badgesViewData.size}"
                        adapter.setBadges(badgesViewData)

                        rvBackground.post {
                            val backgroundHeight = badgesRv.top +
                                    badgesRv.getChildAt(0).measuredHeight / 2 +
                                    requireContext().resources.getDimension(R.dimen.badge_recycler_verticalMargin).toInt()
                            rvBackground.layoutParams = FrameLayout.LayoutParams(root.width, backgroundHeight)
                            rvBackground.imageTintList = ColorStateList.valueOf(badges.header.background)
                        }
                    }
                }
        }

    private fun openDetailDialog(badgeViewData: BadgeViewData) {
        App.track(BadgeClick(badgeViewData.id))

        val badgeDetailDialog = BottomSheetDialog(requireContext(), R.style.RoundedCornersDialog)
        val badgeDetailBinding = BadgeDetailFragmentBinding.inflate(layoutInflater)

        with(badgeDetailBinding) {

            val detailColors = badges.detail
            val detailTextStyles = ThuziTextStyle.badges.detail

            with(name) {
                text = badgeViewData.name
                setTextColor(detailColors.title)
                setFont(detailTextStyles.title)
            }

            with(description) {
                text = badgeViewData.description
                setTextColor(detailColors.description)
                setFont(detailTextStyles.description)
            }

            badgeViewData.image?.let {
                badgeImage.setImageDrawable(it)
            }
            // In this very specific case, we don't remove the alpha from the shadow, the painting is accounting for it.
            root.background =
                generateShadowDrawable(root, badges.detail.background, badges.detail.shadow)
            close.imageTintList = ColorStateList.valueOf(badges.detail.close)
            close.contentDescription = localizationService.getString("common.close")
            close.setOnSafeClickListener { badgeDetailDialog.dismiss() }

            badgeDetailDialog.setContentView(root)
        }

        badgeDetailDialog.show()
    }

    private fun generateShadowDrawable(
        view: View,
        backgroundColor: Int,
        shadowColor: Int,
    ): Drawable {
        val elevationValue = 8.dpToPx()
        val cornerRadiusValue = 20.dpToPx().toFloat()
        val outerRadius = floatArrayOf(
            cornerRadiusValue, cornerRadiusValue, // Top left
            cornerRadiusValue, cornerRadiusValue, // Top right
            0f, 0f, // Bottom right
            0f, 0f // Bottom left
        )
        val dy: Float = -elevationValue / 3f
        val shapeDrawable = ShapeDrawable().apply {
            paint.color = backgroundColor
            paint.setShadowLayer(cornerRadiusValue / 3, 0f, dy, shadowColor)
            shape = RoundRectShape(outerRadius, null, null)
        }
        view.setLayerType(View.LAYER_TYPE_SOFTWARE, shapeDrawable.paint)
        val layerDrawable = LayerDrawable(arrayOf(shapeDrawable))
        layerDrawable.setLayerInset(0, 0, elevationValue * 2, 0, 0)
        return layerDrawable
    }
}

private data class BadgeClick(
    private val badgeId: String,
) : MappedMetrics {
    override fun track(provider: MappedProvider) {
        val eventName = EventName("badges/badge_click")
        val parameters = mapOf(EventParameter.itemId to badgeId)
        provider.track(eventName, parameters)
    }
}

private class CenterLastRowItemDecoration(private val columnCount: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildAdapterPosition(view)
        val itemCount = state.itemCount

        if (isGridItemInLastRow(position, itemCount, columnCount) && !isLastRowFull(itemCount, columnCount)) {
            val lp = view.layoutParams as MarginLayoutParams
            val itemWidth = parent.measuredWidth / columnCount - lp.leftMargin - lp.rightMargin
            val itemsInLastRow = itemCount % columnCount

            if (position % columnCount == 0) { // First item in the row
                outRect.left = itemWidth / itemsInLastRow
            }
            if (position % columnCount == itemsInLastRow - 1) { // Last item in the row
                outRect.right = itemWidth / itemsInLastRow
            }
        }
    }

    private fun isGridItemInLastRow(position: Int, itemCount: Int, spanCount: Int): Boolean {
        return position >= itemCount - (itemCount % spanCount)
    }

    private fun isLastRowFull(itemCount: Int, spanCount: Int): Boolean {
        return itemCount % spanCount == 0
    }
}
