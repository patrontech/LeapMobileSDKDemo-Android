package com.greencopper.interfacekit.ui.views.navigationcontrols

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.services.track
import com.greencopper.interfacekit.color.TopBarColor
import com.greencopper.interfacekit.common.setOnSafeClickListener
import com.greencopper.interfacekit.databinding.KibaToolbarBinding
import com.greencopper.interfacekit.imageservice.ImageService
import com.greencopper.interfacekit.metrics.TopBarTapEvent
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.textstyle.subsystem.TopBarTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.topbar.TopBarButton
import com.greencopper.interfacekit.topbar.TopBarData
import com.greencopper.interfacekit.ui.disableAccessibleClickAction
import com.greencopper.interfacekit.ui.dpToPx
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

public class KibaToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : ConstraintLayout(context, attrs) {

    private val binding = KibaToolbarBinding.inflate(LayoutInflater.from(context), this, true)

    private val imageService: ImageService by App.lazy()
    private val localizationService: LocalizationService by App.lazy()
    private val routeController: RouteController by App.lazy()

    private lateinit var topBarColor: TopBarColor
    private var topBarTextStyle: TopBarTextStyle? = null
    private lateinit var lifecycleScope: CoroutineScope

    init {
        elevation = 4.dpToPx().toFloat()
    }

    public fun setupToolbar(
        lifecycleScope: CoroutineScope,
        title: String? = null,
        topBarColor: TopBarColor,
        topBarTextStyle: TopBarTextStyle? = null,
        onBackClickListener: OnClickListener? = null,
        onCloseClickListener: OnClickListener? = null,
        customCloseIcon: Drawable? = null,
    ) {
        this.lifecycleScope = lifecycleScope
        this.topBarColor = topBarColor
        this.topBarTextStyle = topBarTextStyle

        with(binding) {
            root.setBackgroundColor(topBarColor.background)

            toolbarBack.apply {
                imageTintList = ColorStateList.valueOf(topBarColor.item)
                setOnClickListener(onBackClickListener)
            }

            toolbarClose.apply {
                imageTintList = ColorStateList.valueOf(topBarColor.item)
                setOnClickListener(onCloseClickListener)
                customCloseIcon?.let { setImageDrawable(it) }
            }

            toolbarTitle.apply {
                setTextColor(topBarColor.title)
                text = title
                topBarTextStyle?.title?.normal?.let { setFont(it) }
            }
        }
    }

    public fun setupTopBarData(barData: TopBarData, origin: Layout) {
        isVisible = true
        barData.rightButtons
            ?.forEachIndexed { index, button ->
                when (button) {
                    is TopBarButton.ImageButton -> {
                        insertImageButton(button, Side.RIGHT, index, origin)
                    }

                    is TopBarButton.TextButton ->
                        insertTextButton(button, Side.RIGHT, index, origin)
                }
            }

        barData.leftButtons
            ?.forEachIndexed { index, button ->
                when (button) {
                    is TopBarButton.ImageButton -> {
                        insertImageButton(button, Side.LEFT, index, origin)
                    }

                    is TopBarButton.TextButton ->
                        insertTextButton(button, Side.LEFT, index, origin)
                }
            }
    }

    private fun insertImageButton(button: TopBarButton.ImageButton, side: Side, index: Int, origin: Layout) {
        insertMenuOption(
            title = localizationService.getString(button.accessibilityLabel),
            iconName = button.imageName,
            shouldColor = button.shouldColor,
            side = side,
            index = index,
            onClick = button.onTap?.let { onTap ->
                {
                    App.track(TopBarTapEvent(onTap.analytics.itemName))
                    routeController.resolveRouteLink(onTap.routeLink, origin)
                }
            }

        )
    }

    private fun insertTextButton(button: TopBarButton.TextButton, side: Side, index: Int, origin: Layout) {
        insertMenuOption(
            title = localizationService.getString(button.text),
            shouldColor = false,
            side = side,
            index = index,
            onClick = {
                button.onTap?.let { onTap ->
                    App.track(TopBarTapEvent(onTap.analytics.itemName))
                    routeController.resolveRouteLink(onTap.routeLink, origin)
                }
            }
        )
    }

    public fun insertMenuOption(
        title: String?,
        iconName: String? = null,
        icon: Drawable? = null,
        index: Int,
        side: Side,
        shouldColor: Boolean = true,
        accessibilityLabel: String? = null,
        id: Int? = null,
        onClick: (() -> Unit)?,
    ) {
        val view = when {
            iconName != null || icon != null -> {
                val imageView = ImageView(context).apply {
                    contentDescription = accessibilityLabel ?: title
                    adjustViewBounds = true

                    if (shouldColor) {
                        imageTintList = ColorStateList.valueOf(topBarColor.item)
                    }

                    if (side == Side.RIGHT) {
                        scaleType = ImageView.ScaleType.CENTER_INSIDE
                        val padding = 12.dpToPx().toInt()
                        setPadding(padding, padding, padding, padding)
                    } else {
                        maxWidth = 128.dpToPx()
                    }
                }

                icon?.let {
                    imageView.setImageDrawable(it)
                } ?: imageService.getImageDrawable(
                    iconName,
                    hideIfUnknown = true,
                    hideIfLoading = true,
                ).onEach {
                    imageView.setImageDrawable(it.drawable)
                }.launchIn(lifecycleScope)

                imageView
            }

            else -> {
                TextView(context).apply {
                    topBarTextStyle?.let { setFont(it.title.normal) }
                    text = title

                    if (shouldColor) {
                        setTextColor(topBarColor.item)
                    }
                }
            }
        }

        val containerView = if (side == Side.RIGHT) binding.toolbarRightButtons else binding.toolbarLeftButtons
        with(view) {
            onClick?.let { setOnSafeClickListener { it() } } ?: run {
                disableAccessibleClickAction()
            }
            val size = if (side == Side.RIGHT) 48.dpToPx() else LayoutParams.WRAP_CONTENT
            layoutParams = LinearLayout.LayoutParams(size, size)

            id?.let { this.id = id }
        }

        try {
            containerView.addView(view, index)
        } catch (e: IndexOutOfBoundsException) {
            containerView.addView(view)
        }
        containerView.isVisible = true
        isVisible = true
    }

    public fun hideBackButton() {
        binding.toolbarBack.isVisible = false
    }

    public fun hideCloseButton() {
        binding.toolbarClose.isVisible = false
    }

    public fun setCloseButtonIcon(icon: Drawable) {
        binding.toolbarClose.setImageDrawable(icon)
    }

    public fun setMenuOptionVisibility(viewId: Int, isVisible: Boolean) {
        binding.toolbarLeftButtons.findViewById<View>(viewId)?.isVisible = isVisible
        binding.toolbarRightButtons.findViewById<View>(viewId)?.isVisible = isVisible
    }

    public fun updateMenuIcon(icon: Drawable, viewId: Int) {
        binding.toolbarLeftButtons.findViewById<ImageView>(viewId)?.setImageDrawable(icon)
        binding.toolbarRightButtons.findViewById<ImageView>(viewId)?.setImageDrawable(icon)
    }

    public enum class Side { LEFT, RIGHT }
}
